package com.ddbb.service.management;

import com.ddbb.dto.management.*;
import com.ddbb.entity.management.Inventory;
import com.ddbb.entity.management.Sales;
import com.ddbb.repository.management.InventoryRepository;
import com.ddbb.repository.management.SalesRepository;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIAnalysisService {
    
    private final SalesRepository salesRepository;
    private final InventoryRepository inventoryRepository;
    private final SalesPredictionAI salesPredictionAI;  // LangChain4j AI Service
    
    @Value("${openai.api.key}")
    private String openaiApiKey;
    
    /**
     * AI 분석 수행
     */
    public AIAnalysisResponse performAnalysis(AIAnalysisRequest request) {
        try {
            // 데이터 수집
            List<InventoryAnalysisDto> inventoryData = collectInventoryData(request);
            
            // 차트용 데이터 생성
            ChartDataDto chartData = buildChartData(request, inventoryData);
            
            // OpenAI API 호출
            String prompt = buildPrompt(request, inventoryData);
            String aiResponse = callOpenAI(prompt);
            
            // 응답 파싱 및 구성
            AIAnalysisResponse response = parseAIResponse(aiResponse, request.getAnalysisType());
            response.setChartData(chartData);
            
            return response;
            
        } catch (Exception e) {
            log.error("AI 분석 중 오류 발생", e);
            return AIAnalysisResponse.builder()
                    .summary("분석 중 오류가 발생했습니다.")
                    .detailedAnalysis("OpenAI API 호출 실패: " + e.getMessage())
                    .recommendations(Collections.emptyList())
                    .warnings(Collections.emptyList())
                    .analysisType(request.getAnalysisType())
                    .generatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                    .build();
        }
    }
    
    /**
     * 차트용 데이터 생성
     */
    private ChartDataDto buildChartData(AIAnalysisRequest request, List<InventoryAnalysisDto> inventoryData) {
        // 과거 데이터 수집
        Map<String, Integer> historicalSales = new LinkedHashMap<>();
        Map<String, Double> historicalRevenue = new LinkedHashMap<>();
        Map<String, Integer> breadHistoricalSales = new LinkedHashMap<>();
        
        // 날짜별 과거 데이터
        LocalDate currentDate = request.getStartDate();
        while (!currentDate.isAfter(request.getEndDate())) {
            LocalDateTime dayStart = currentDate.atStartOfDay();
            LocalDateTime dayEnd = currentDate.atTime(23, 59, 59);
            
            List<Sales> daySales = salesRepository.findBySaleDateBetween(dayStart, dayEnd);
            
            int totalQuantity = daySales.stream().mapToInt(Sales::getQuantity).sum();
            double totalRevenue = daySales.stream().mapToDouble(s -> s.getTotalPrice().doubleValue()).sum();
            
            historicalSales.put(currentDate.toString(), totalQuantity);
            historicalRevenue.put(currentDate.toString(), totalRevenue);
            
            currentDate = currentDate.plusDays(1);
        }
        
        // 빵별 과거 판매량
        for (InventoryAnalysisDto item : inventoryData) {
            breadHistoricalSales.put(item.getBreadName(), item.getTotalSold());
        }
        
        // 예측 데이터 생성 (LangChain4j 기반 AI 예측)
        log.info("🤖 === AI 예측 데이터 생성 시작 ===");
        log.info("📈 Step 1/3: 판매량 예측 호출...");
        Map<String, Integer> predictedSales = generateAIPredictions(historicalSales, 7); // 7일 예측
        log.info("📈 Step 2/3: 수익 예측 호출...");
        Map<String, Double> predictedRevenue = generateAIRevenuePredictions(historicalRevenue, 7);
        log.info("📈 Step 3/3: 빵별 판매량 예측 호출...");
        Map<String, Integer> breadPredictedSales = generateAIBreadPredictions(inventoryData);
        log.info("✅ === AI 예측 데이터 생성 완료 ===");
        
        // 성장률 계산
        List<ChartDataDto.GrowthRateDto> growthRates = calculateGrowthRates(historicalSales);
        
        // 예측 성장률 계산
        List<ChartDataDto.GrowthRateDto> predictedGrowthRates = calculatePredictedGrowthRates(
                historicalSales, predictedSales);
        
        return ChartDataDto.builder()
                .historicalSales(historicalSales)
                .predictedSales(predictedSales)
                .historicalRevenue(historicalRevenue)
                .predictedRevenue(predictedRevenue)
                .breadHistoricalSales(breadHistoricalSales)
                .breadPredictedSales(breadPredictedSales)
                .growthRates(growthRates)
                .predictedGrowthRates(predictedGrowthRates)
                .build();
    }
    
    /**
     * LangChain4j 기반 판매량 예측 (구조화된 출력으로 일관성 보장)
     */
    private Map<String, Integer> generateAIPredictions(Map<String, Integer> historical, int days) {
        try {
            // 과거 데이터를 문자열로 포맷팅
            StringBuilder historicalData = new StringBuilder();
            historical.forEach((date, quantity) -> {
                historicalData.append(String.format("%s: %d개\n", date, quantity));
            });
            
            // 마지막 날짜 계산
            String lastDateStr = historical.keySet().stream()
                    .reduce((a, b) -> b)
                    .orElse(LocalDate.now().toString());
            
            log.info("🔥 LangChain4j 판매량 예측 시작 - 예측 일수: {}일", days);
            log.info("📊 과거 데이터 포인트: {}개, 마지막 날짜: {}", historical.size(), lastDateStr);
            
            // LangChain4j AI Service 호출
            PredictionResult result = salesPredictionAI.predictSales(
                    historicalData.toString(),
                    days,
                    lastDateStr
            );
            
            // 통계적 신뢰도 계산 (AI가 반환한 값 대신 통계로 재계산)
            double calculatedConfidence = calculateSalesConfidence(historical, result.getPredictions());
            result.setConfidence(calculatedConfidence);
            
            log.info("✅ 판매량 예측 완료");
            log.info("   📈 예측 개수: {}개", result.getPredictions().size());
            log.info("   🎯 통계적 신뢰도: {}% ({})", 
                    String.format("%.1f", calculatedConfidence * 100),
                    String.format("%.3f", calculatedConfidence));
            log.info("   💡 예측 근거: {}", result.getReasoning());
            
            return result.getPredictions();
            
        } catch (Exception e) {
            log.error("❌ LangChain4j 예측 실패, 백업 예측 방식 사용: {}", e.getMessage());
            return generatePredictions(historical, days);
        }
    }
    
    /**
     * LangChain4j 기반 수익 예측
     */
    private Map<String, Double> generateAIRevenuePredictions(Map<String, Double> historical, int days) {
        try {
            StringBuilder historicalData = new StringBuilder();
            historical.forEach((date, revenue) -> {
                historicalData.append(String.format("%s: %.0f원\n", date, revenue));
            });
            
            String lastDateStr = historical.keySet().stream()
                    .reduce((a, b) -> b)
                    .orElse(LocalDate.now().toString());
            
            log.info("🔥 LangChain4j 수익 예측 시작 - 예측 일수: {}일", days);
            log.info("📊 과거 수익 데이터 포인트: {}개, 마지막 날짜: {}", historical.size(), lastDateStr);
            
            RevenuePredictionResult result = salesPredictionAI.predictRevenue(
                    historicalData.toString(),
                    days,
                    lastDateStr
            );
            
            // 통계적 신뢰도 계산
            double calculatedConfidence = calculateRevenueConfidence(historical, result.getPredictions());
            result.setConfidence(calculatedConfidence);
            
            log.info("✅ 수익 예측 완료");
            log.info("   📈 예측 개수: {}개", result.getPredictions().size());
            log.info("   🎯 통계적 신뢰도: {}% ({})", 
                    String.format("%.1f", calculatedConfidence * 100),
                    String.format("%.3f", calculatedConfidence));
            log.info("   💡 예측 근거: {}", result.getReasoning());
            
            return result.getPredictions();
            
        } catch (Exception e) {
            log.error("❌ LangChain4j 수익 예측 실패, 백업 예측 방식 사용: {}", e.getMessage());
            return generateRevenuePredictions(historical, days);
        }
    }
    
    /**
     * LangChain4j 기반 빵별 판매량 예측
     */
    private Map<String, Integer> generateAIBreadPredictions(List<InventoryAnalysisDto> inventoryData) {
        try {
            StringBuilder inventoryInfo = new StringBuilder();
            for (InventoryAnalysisDto item : inventoryData) {
                inventoryInfo.append(String.format("- %s: 일평균 %.1f개 판매, 현재 재고 %d개, 총 판매 %d개\n",
                    item.getBreadName(),
                    item.getAverageDailySales(),
                    item.getCurrentStock(),
                    item.getTotalSold()));
            }
            
            log.info("🔥 LangChain4j 빵별 판매량 예측 시작 - 빵 종류: {}개", inventoryData.size());
            
            BreadPredictionResult result = salesPredictionAI.predictBreadSales(
                    inventoryInfo.toString()
            );
            
            // 통계적 신뢰도 계산
            double calculatedConfidence = calculateBreadConfidence(inventoryData, result.getPredictions());
            result.setConfidence(calculatedConfidence);
            
            log.info("✅ 빵별 판매량 예측 완료");
            log.info("   📈 예측 개수: {}개", result.getPredictions().size());
            log.info("   🎯 통계적 신뢰도: {}% ({})", 
                    String.format("%.1f", calculatedConfidence * 100),
                    String.format("%.3f", calculatedConfidence));
            log.info("   💡 예측 근거: {}", result.getReasoning());
            
            return result.getPredictions();
            
        } catch (Exception e) {
            log.error("❌ LangChain4j 빵별 예측 실패, 백업 예측 방식 사용: {}", e.getMessage());
            return generateBreadPredictions(inventoryData);
        }
    }
    
    /**
     * 판매량 예측 (이동 평균 기반) - 백업용
     */
    private Map<String, Integer> generatePredictions(Map<String, Integer> historical, int days) {
        Map<String, Integer> predictions = new LinkedHashMap<>();
        
        if (historical.isEmpty()) return predictions;
        
        // 최근 7일 평균 계산
        List<Integer> recentSales = new ArrayList<>(historical.values());
        int lookback = Math.min(7, recentSales.size());
        double avg = recentSales.subList(Math.max(0, recentSales.size() - lookback), recentSales.size())
                .stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);
        
        // 추세 계산 (간단한 선형 회귀)
        double trend = 0;
        if (recentSales.size() >= 2) {
            int lastIndex = recentSales.size() - 1;
            trend = (recentSales.get(lastIndex) - recentSales.get(Math.max(0, lastIndex - 7))) / 7.0;
        }
        
        // 마지막 날짜 가져오기
        String lastDateStr = historical.keySet().stream().reduce((a, b) -> b).orElse(LocalDate.now().toString());
        LocalDate lastDate = LocalDate.parse(lastDateStr);
        
        // 예측 생성
        for (int i = 1; i <= days; i++) {
            LocalDate futureDate = lastDate.plusDays(i);
            int predicted = (int) Math.max(0, avg + (trend * i));
            predictions.put(futureDate.toString(), predicted);
        }
        
        return predictions;
    }
    
    /**
     * 수익 예측
     */
    private Map<String, Double> generateRevenuePredictions(Map<String, Double> historical, int days) {
        Map<String, Double> predictions = new LinkedHashMap<>();
        
        if (historical.isEmpty()) return predictions;
        
        // 최근 7일 평균 계산
        List<Double> recentRevenue = new ArrayList<>(historical.values());
        int lookback = Math.min(7, recentRevenue.size());
        double avg = recentRevenue.subList(Math.max(0, recentRevenue.size() - lookback), recentRevenue.size())
                .stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);
        
        // 추세 계산
        double trend = 0;
        if (recentRevenue.size() >= 2) {
            int lastIndex = recentRevenue.size() - 1;
            trend = (recentRevenue.get(lastIndex) - recentRevenue.get(Math.max(0, lastIndex - 7))) / 7.0;
        }
        
        // 마지막 날짜
        String lastDateStr = historical.keySet().stream().reduce((a, b) -> b).orElse(LocalDate.now().toString());
        LocalDate lastDate = LocalDate.parse(lastDateStr);
        
        // 예측 생성
        for (int i = 1; i <= days; i++) {
            LocalDate futureDate = lastDate.plusDays(i);
            double predicted = Math.max(0, avg + (trend * i));
            predictions.put(futureDate.toString(), predicted);
        }
        
        return predictions;
    }
    
    /**
     * 빵별 판매량 예측
     */
    private Map<String, Integer> generateBreadPredictions(List<InventoryAnalysisDto> inventoryData) {
        Map<String, Integer> predictions = new LinkedHashMap<>();
        
        for (InventoryAnalysisDto item : inventoryData) {
            // 다음 주 예측 판매량 = 일평균 * 7
            int weeklyPrediction = (int) (item.getAverageDailySales() * 7);
            predictions.put(item.getBreadName(), weeklyPrediction);
        }
        
        return predictions;
    }
    
    /**
     * 성장률 계산 (전일 대비)
     */
    private List<ChartDataDto.GrowthRateDto> calculateGrowthRates(Map<String, Integer> sales) {
        List<ChartDataDto.GrowthRateDto> growthRates = new ArrayList<>();
        
        List<Map.Entry<String, Integer>> entries = new ArrayList<>(sales.entrySet());
        
        for (int i = 1; i < entries.size(); i++) {
            int previous = entries.get(i - 1).getValue();
            int current = entries.get(i).getValue();
            
            double growthRate = previous > 0 ? ((current - previous) * 100.0 / previous) : 0;
            
            growthRates.add(ChartDataDto.GrowthRateDto.builder()
                    .date(entries.get(i).getKey())
                    .growthRate(growthRate)
                    .build());
        }
        
        return growthRates;
    }
    
    /**
     * 예측 성장률 계산 (예측 데이터 기반)
     */
    private List<ChartDataDto.GrowthRateDto> calculatePredictedGrowthRates(
            Map<String, Integer> historicalSales, 
            Map<String, Integer> predictedSales) {
        
        List<ChartDataDto.GrowthRateDto> predictedGrowthRates = new ArrayList<>();
        
        log.info("계산 시작 - 과거 판매 데이터 수: {}, 예측 판매 데이터 수: {}", 
                historicalSales.size(), predictedSales.size());
        
        if (predictedSales.isEmpty()) {
            log.warn("예측 판매 데이터가 비어있어 예측 성장률을 계산할 수 없습니다.");
            return predictedGrowthRates;
        }
        
        // 과거 데이터의 최근 성장률 평균 계산 (최근 7일)
        List<Map.Entry<String, Integer>> historicalEntries = new ArrayList<>(historicalSales.entrySet());
        double avgHistoricalGrowth = 0;
        int count = 0;
        
        for (int i = Math.max(1, historicalEntries.size() - 7); i < historicalEntries.size(); i++) {
            int previous = historicalEntries.get(i - 1).getValue();
            int current = historicalEntries.get(i).getValue();
            
            if (previous > 0) {
                double growthRate = ((current - previous) * 100.0 / previous);
                avgHistoricalGrowth += growthRate;
                count++;
            }
        }
        
        avgHistoricalGrowth = count > 0 ? avgHistoricalGrowth / count : 0;
        
        // 예측 데이터의 성장률 계산
        List<Map.Entry<String, Integer>> predictedEntries = new ArrayList<>(predictedSales.entrySet());
        
        // 첫 예측일은 과거 마지막 날 대비 성장률 계산
        if (!historicalEntries.isEmpty() && !predictedEntries.isEmpty()) {
            int lastHistorical = historicalEntries.get(historicalEntries.size() - 1).getValue();
            int firstPredicted = predictedEntries.get(0).getValue();
            
            double growthRate = lastHistorical > 0 
                    ? ((firstPredicted - lastHistorical) * 100.0 / lastHistorical) 
                    : avgHistoricalGrowth;
            
            predictedGrowthRates.add(ChartDataDto.GrowthRateDto.builder()
                    .date(predictedEntries.get(0).getKey())
                    .growthRate(growthRate)
                    .build());
        }
        
        // 나머지 예측일들의 성장률 계산
        for (int i = 1; i < predictedEntries.size(); i++) {
            int previous = predictedEntries.get(i - 1).getValue();
            int current = predictedEntries.get(i).getValue();
            
            double growthRate = previous > 0 
                    ? ((current - previous) * 100.0 / previous) 
                    : avgHistoricalGrowth;
            
            predictedGrowthRates.add(ChartDataDto.GrowthRateDto.builder()
                    .date(predictedEntries.get(i).getKey())
                    .growthRate(growthRate)
                    .build());
        }
        
        log.info("예측 성장률 계산 완료 - 총 {}개의 예측 성장률 생성", predictedGrowthRates.size());
        
        return predictedGrowthRates;
    }
    
    /**
     * 재고 및 판매 데이터 수집
     */
    private List<InventoryAnalysisDto> collectInventoryData(AIAnalysisRequest request) {
        LocalDateTime startDateTime = request.getStartDate().atStartOfDay();
        LocalDateTime endDateTime = request.getEndDate().atTime(23, 59, 59);
        
        List<Inventory> inventories = inventoryRepository.findAll();
        long daysBetween = ChronoUnit.DAYS.between(request.getStartDate(), request.getEndDate()) + 1;
        
        return inventories.stream().map(inventory -> {
            // 해당 기간의 판매 데이터 조회
            List<Sales> sales = salesRepository.findByBread_IdAndSaleDateBetween(
                    inventory.getBread().getId(), 
                    startDateTime, 
                    endDateTime
            );
            
            int totalSold = sales.stream()
                    .mapToInt(Sales::getQuantity)
                    .sum();
            
            double avgDailySales = daysBetween > 0 ? (double) totalSold / daysBetween : 0;
            int daysOfStock = avgDailySales > 0 ? (int) (inventory.getQuantity() / avgDailySales) : 999;
            
            return InventoryAnalysisDto.builder()
                    .breadId(inventory.getBread().getId())
                    .breadName(inventory.getBread().getName())
                    .currentStock(inventory.getQuantity())
                    .minStockLevel(inventory.getMinStockLevel())
                    .totalSold(totalSold)
                    .averageDailySales(avgDailySales)
                    .daysOfStock(daysOfStock)
                    .build();
        }).collect(Collectors.toList());
    }
    
    /**
     * OpenAI 프롬프트 생성
     */
    private String buildPrompt(AIAnalysisRequest request, List<InventoryAnalysisDto> data) {
        StringBuilder prompt = new StringBuilder();
        
        prompt.append("당신은 빵집 재고 관리 전문가입니다. 다음 데이터를 분석하여 인사이트를 제공해주세요.\n\n");
        prompt.append("분석 기간: ").append(request.getStartDate()).append(" ~ ").append(request.getEndDate()).append("\n\n");
        
        prompt.append("현재 재고 및 판매 데이터:\n");
        for (InventoryAnalysisDto item : data) {
            prompt.append(String.format("- %s: 현재재고 %d개, 최소재고 %d개, 총판매 %d개, 일평균판매 %.1f개, 재고소진일 %d일\n",
                    item.getBreadName(),
                    item.getCurrentStock(),
                    item.getMinStockLevel(),
                    item.getTotalSold(),
                    item.getAverageDailySales(),
                    item.getDaysOfStock()
            ));
        }
        
        prompt.append("\n분석 요청 타입: ").append(getAnalysisTypeDescription(request.getAnalysisType())).append("\n\n");
        
        prompt.append("다음 형식으로 답변해주세요:\n");
        prompt.append("1. 요약 (2-3줄로 핵심 내용 정리)\n");
        prompt.append("2. 상세 분석 (각 제품별 상태 및 트렌드 분석)\n");
        prompt.append("3. 추천 사항 (3-5개의 구체적인 액션 아이템, 각각 '- '로 시작)\n");
        prompt.append("4. 경고 사항 (긴급하게 처리해야 할 항목들, 각각 '- '로 시작)\n");
        prompt.append("5. 예측 (다음 주 예상 판매량을 JSON 형식으로: {\"breadName\": predictedQuantity})\n\n");
        prompt.append("한국어로 답변해주세요.");
        
        return prompt.toString();
    }
    
    /**
     * 분석 타입 설명
     */
    private String getAnalysisTypeDescription(String type) {
        return switch (type) {
            case "SALES_PREDICTION" -> "다음 주/월 판매량 예측에 집중";
            case "INVENTORY_RECOMMENDATION" -> "적정 재고 수준 추천에 집중";
            case "OVERSTOCK_WARNING" -> "과잉 재고 경고 및 처리 방안 제시";
            case "SHORTAGE_WARNING" -> "재고 부족 경고 및 발주 추천";
            case "COMPREHENSIVE" -> "종합적인 재고 관리 분석";
            default -> "일반 분석";
        };
    }
    
    /**
     * OpenAI API 호출
     */
    private String callOpenAI(String prompt) {
        try {
            OpenAiService service = new OpenAiService(openaiApiKey, Duration.ofSeconds(60));
            
            ChatMessage systemMessage = new ChatMessage("system", 
                    "당신은 빵집 재고 관리 전문 AI 어시스턴트입니다. 데이터를 분석하고 실용적인 조언을 제공합니다.");
            ChatMessage userMessage = new ChatMessage("user", prompt);
            
            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model("gpt-4o-mini")
                    .messages(Arrays.asList(systemMessage, userMessage))
                    .temperature(0.7)
                    .maxTokens(2000)
                    .build();
            
            return service.createChatCompletion(completionRequest)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();
                    
        } catch (Exception e) {
            log.error("OpenAI API 호출 실패", e);
            throw new RuntimeException("AI 분석 실패: " + e.getMessage());
        }
    }
    
    /**
     * AI 응답 파싱
     */
    private AIAnalysisResponse parseAIResponse(String aiResponse, String analysisType) {
        String[] sections = aiResponse.split("\n\n");
        
        String summary = "";
        String detailedAnalysis = "";
        List<String> recommendations = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        String predictionData = "{}";
        
        for (String section : sections) {
            String lowerSection = section.toLowerCase();
            
            if (lowerSection.contains("요약") || lowerSection.startsWith("1.")) {
                summary = section.replaceAll("^[0-9]+\\.\\s*요약[:\\s]*", "").trim();
            } else if (lowerSection.contains("상세") || lowerSection.startsWith("2.")) {
                detailedAnalysis = section.replaceAll("^[0-9]+\\.\\s*상세.*?[:\\s]*", "").trim();
            } else if (lowerSection.contains("추천") || lowerSection.startsWith("3.")) {
                String content = section.replaceAll("^[0-9]+\\.\\s*추천.*?[:\\s]*", "");
                recommendations = Arrays.stream(content.split("\n"))
                        .map(String::trim)
                        .filter(s -> s.startsWith("-") || s.startsWith("•"))
                        .map(s -> s.replaceAll("^[-•]\\s*", ""))
                        .collect(Collectors.toList());
            } else if (lowerSection.contains("경고") || lowerSection.startsWith("4.")) {
                String content = section.replaceAll("^[0-9]+\\.\\s*경고.*?[:\\s]*", "");
                warnings = Arrays.stream(content.split("\n"))
                        .map(String::trim)
                        .filter(s -> s.startsWith("-") || s.startsWith("•"))
                        .map(s -> s.replaceAll("^[-•]\\s*", ""))
                        .collect(Collectors.toList());
            } else if (lowerSection.contains("예측") || lowerSection.contains("{")) {
                int jsonStart = section.indexOf("{");
                int jsonEnd = section.lastIndexOf("}");
                if (jsonStart != -1 && jsonEnd != -1) {
                    predictionData = section.substring(jsonStart, jsonEnd + 1);
                }
            }
        }
        
        return AIAnalysisResponse.builder()
                .summary(summary)
                .detailedAnalysis(detailedAnalysis)
                .recommendations(recommendations)
                .warnings(warnings)
                .predictionData(predictionData)
                .analysisType(analysisType)
                .generatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
                .build();
    }
    
    /**
     * 판매량 예측 신뢰도 계산 (통계적 방법)
     */
    private double calculateSalesConfidence(Map<String, Integer> historical, Map<String, Integer> predicted) {
        if (historical.isEmpty()) {
            return 0.75; // 데이터 부족 시 기본값
        }
        
        double confidence = 1.0; // 최대 신뢰도에서 시작
        
        // 1. 데이터 양 평가 (30% 가중치)
        int dataPoints = historical.size();
        double dataQualityScore;
        if (dataPoints >= 30) {
            dataQualityScore = 1.0;
        } else if (dataPoints >= 14) {
            dataQualityScore = 0.95;
        } else if (dataPoints >= 7) {
            dataQualityScore = 0.85;
        } else {
            dataQualityScore = 0.70;
        }
        
        // 2. 데이터 변동성 평가 (40% 가중치)
        List<Integer> values = new ArrayList<>(historical.values());
        double mean = values.stream().mapToInt(Integer::intValue).average().orElse(0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);
        double stdDev = Math.sqrt(variance);
        double cv = mean > 0 ? (stdDev / mean) : 0; // 변동계수 (Coefficient of Variation)
        
        double volatilityScore;
        if (cv <= 0.15) {
            volatilityScore = 1.0;  // 변동성 매우 낮음
        } else if (cv <= 0.25) {
            volatilityScore = 0.95; // 변동성 낮음
        } else if (cv <= 0.35) {
            volatilityScore = 0.90; // 변동성 보통
        } else if (cv <= 0.50) {
            volatilityScore = 0.85; // 변동성 높음
        } else {
            volatilityScore = 0.75; // 변동성 매우 높음
        }
        
        // 3. 추세 안정성 평가 (30% 가중치)
        double trendScore = 1.0;
        if (values.size() >= 3) {
            // 최근 데이터의 급격한 변화 체크
            int lastIndex = values.size() - 1;
            int recentChanges = 0;
            int significantChanges = 0;
            
            for (int i = Math.max(0, lastIndex - 6); i < lastIndex; i++) {
                double changeRate = values.get(i) > 0 
                        ? Math.abs((values.get(i + 1) - values.get(i)) * 100.0 / values.get(i))
                        : 0;
                recentChanges++;
                if (changeRate > 30) {
                    significantChanges++;
                }
            }
            
            if (recentChanges > 0) {
                double instabilityRatio = (double) significantChanges / recentChanges;
                if (instabilityRatio <= 0.1) {
                    trendScore = 1.0;
                } else if (instabilityRatio <= 0.3) {
                    trendScore = 0.95;
                } else if (instabilityRatio <= 0.5) {
                    trendScore = 0.85;
                } else {
                    trendScore = 0.75;
                }
            }
        }
        
        // 가중 평균 계산
        confidence = (dataQualityScore * 0.30) + (volatilityScore * 0.40) + (trendScore * 0.30);
        
        // 최소 75%, 최대 99%로 제한
        confidence = Math.max(0.75, Math.min(0.99, confidence));
        
        log.info("   📊 [판매량] 신뢰도 상세 - 데이터품질: {}, 변동성: {}, 추세안정성: {}, 최종: {}", 
                String.format("%.2f", dataQualityScore), 
                String.format("%.2f", volatilityScore), 
                String.format("%.2f", trendScore), 
                String.format("%.3f", confidence));
        
        return confidence;
    }
    
    /**
     * 수익 예측 신뢰도 계산 (통계적 방법 - 수익 특화)
     */
    private double calculateRevenueConfidence(Map<String, Double> historical, Map<String, Double> predicted) {
        if (historical.isEmpty()) {
            return 0.75;
        }
        
        double confidence = 1.0;
        
        // 1. 데이터 양 평가 (25% 가중치)
        int dataPoints = historical.size();
        double dataQualityScore;
        if (dataPoints >= 30) {
            dataQualityScore = 1.0;
        } else if (dataPoints >= 14) {
            dataQualityScore = 0.95;
        } else if (dataPoints >= 7) {
            dataQualityScore = 0.85;
        } else {
            dataQualityScore = 0.70;
        }
        
        // 2. 데이터 변동성 평가 (45% 가중치 - 수익은 변동성에 더 민감)
        List<Double> values = new ArrayList<>(historical.values());
        double mean = values.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);
        double stdDev = Math.sqrt(variance);
        double cv = mean > 0 ? (stdDev / mean) : 0;
        
        double volatilityScore;
        if (cv <= 0.12) {           // 수익은 더 엄격한 기준
            volatilityScore = 1.0;
        } else if (cv <= 0.20) {
            volatilityScore = 0.93;
        } else if (cv <= 0.30) {
            volatilityScore = 0.88;
        } else if (cv <= 0.45) {
            volatilityScore = 0.82;
        } else {
            volatilityScore = 0.75;
        }
        
        // 3. 추세 안정성 평가 (20% 가중치)
        double trendScore = 1.0;
        if (values.size() >= 3) {
            int lastIndex = values.size() - 1;
            int recentChanges = 0;
            int significantChanges = 0;
            
            for (int i = Math.max(0, lastIndex - 6); i < lastIndex; i++) {
                double changeRate = values.get(i) > 0 
                        ? Math.abs((values.get(i + 1) - values.get(i)) * 100.0 / values.get(i))
                        : 0;
                recentChanges++;
                if (changeRate > 25) {  // 수익은 25% 이상 변화를 중요시
                    significantChanges++;
                }
            }
            
            if (recentChanges > 0) {
                double instabilityRatio = (double) significantChanges / recentChanges;
                if (instabilityRatio <= 0.1) {
                    trendScore = 1.0;
                } else if (instabilityRatio <= 0.3) {
                    trendScore = 0.93;
                } else if (instabilityRatio <= 0.5) {
                    trendScore = 0.82;
                } else {
                    trendScore = 0.75;
                }
            }
        }
        
        // 4. 수익 규모 평가 (10% 가중치 - 수익 특화 요소)
        double revenueScaleScore = 1.0;
        if (mean >= 500000) {        // 50만원 이상 - 안정적인 비즈니스
            revenueScaleScore = 1.0;
        } else if (mean >= 300000) { // 30만원 이상
            revenueScaleScore = 0.95;
        } else if (mean >= 100000) { // 10만원 이상
            revenueScaleScore = 0.90;
        } else if (mean >= 50000) {  // 5만원 이상
            revenueScaleScore = 0.85;
        } else {
            revenueScaleScore = 0.80; // 낮은 수익은 예측 어려움
        }
        
        // 가중 평균 계산 (수익 예측 특화 가중치)
        confidence = (dataQualityScore * 0.25) + (volatilityScore * 0.45) + (trendScore * 0.20) + (revenueScaleScore * 0.10);
        confidence = Math.max(0.75, Math.min(0.99, confidence));
        
        log.info("   📊 [수익] 신뢰도 상세 - 데이터품질: {}, 변동성: {}, 추세안정성: {}, 수익규모: {}, 최종: {}", 
                String.format("%.2f", dataQualityScore), 
                String.format("%.2f", volatilityScore), 
                String.format("%.2f", trendScore),
                String.format("%.2f", revenueScaleScore),
                String.format("%.3f", confidence));
        
        return confidence;
    }
    
    /**
     * 빵별 판매량 예측 신뢰도 계산 (통계적 방법)
     */
    private double calculateBreadConfidence(List<InventoryAnalysisDto> inventoryData, Map<String, Integer> predicted) {
        if (inventoryData.isEmpty()) {
            return 0.75;
        }
        
        double totalConfidence = 0.0;
        int validItems = 0;
        
        for (InventoryAnalysisDto item : inventoryData) {
            double itemConfidence = 1.0;
            
            // 1. 판매 데이터 충분성 평가 (40% 가중치)
            int totalSold = item.getTotalSold();
            double avgDailySales = item.getAverageDailySales();
            
            double dataQualityScore;
            if (totalSold >= 100 && avgDailySales >= 5) {
                dataQualityScore = 1.0;
            } else if (totalSold >= 50 && avgDailySales >= 3) {
                dataQualityScore = 0.95;
            } else if (totalSold >= 20 && avgDailySales >= 1) {
                dataQualityScore = 0.85;
            } else {
                dataQualityScore = 0.75;
            }
            
            // 2. 일평균 판매량의 안정성 평가 (30% 가중치)
            double stabilityScore;
            if (avgDailySales >= 10) {
                stabilityScore = 1.0;  // 판매량 많고 안정적
            } else if (avgDailySales >= 5) {
                stabilityScore = 0.95;
            } else if (avgDailySales >= 2) {
                stabilityScore = 0.90;
            } else if (avgDailySales >= 1) {
                stabilityScore = 0.85;
            } else {
                stabilityScore = 0.75; // 판매량 적어 불안정
            }
            
            // 3. 재고 대비 판매 비율 평가 (30% 가중치)
            int currentStock = item.getCurrentStock();
            double turnoverScore = 1.0;
            if (avgDailySales > 0) {
                double daysOfStock = currentStock / avgDailySales;
                if (daysOfStock >= 3 && daysOfStock <= 14) {
                    turnoverScore = 1.0; // 적정 재고 수준
                } else if (daysOfStock >= 1 && daysOfStock < 3) {
                    turnoverScore = 0.90; // 재고 부족 가능
                } else if (daysOfStock > 14 && daysOfStock <= 30) {
                    turnoverScore = 0.85; // 과잉 재고 가능
                } else {
                    turnoverScore = 0.75; // 재고 불균형
                }
            }
            
            itemConfidence = (dataQualityScore * 0.40) + (stabilityScore * 0.30) + (turnoverScore * 0.30);
            totalConfidence += itemConfidence;
            validItems++;
        }
        
        double confidence = validItems > 0 ? totalConfidence / validItems : 0.75;
        confidence = Math.max(0.75, Math.min(0.99, confidence));
        
        log.info("   📊 [빵별] 신뢰도 상세 - 평균 신뢰도: {}, 분석 항목 수: {}", 
                String.format("%.3f", confidence), validItems);
        
        return confidence;
    }
}

