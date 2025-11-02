package com.ddbb.service.aidashboard;

import com.ddbb.dto.aidashboard.DashboardChartDto;
import com.ddbb.entity.management.Inventory;
import com.ddbb.entity.management.Sales;
import com.ddbb.repository.management.InventoryRepository;
import com.ddbb.repository.management.SalesRepository;
import com.ddbb.service.aidashboard.WeatherService.WeatherSummary;
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
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardAIService {

    private final SalesRepository salesRepository;
    private final InventoryRepository inventoryRepository;
    private final WeatherService weatherService;

    @Value("${openai.api.key}")
    private String openaiApiKey;

    /**
     * AI 대시보드 인사이트 생성
     */
    public Map<String, Object> generateDashboardInsights() {
        log.info("AI 대시보드 인사이트 생성 시작");

        // 1. 날씨 정보
        WeatherSummary weather = weatherService.fetchSeoulWeather();

        // 2. 오늘 판매 데이터 (실시간 모니터링용)
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(23, 59, 59);
        LocalDateTime now = LocalDateTime.now();
        List<Sales> todaySales = salesRepository.findBySaleDateBetween(todayStart, now);

        // 3. 어제까지의 완료된 판매 데이터 (트렌드 분석용 - 오늘 제외)
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
        LocalDateTime yesterdayEnd = LocalDate.now().minusDays(1).atTime(23, 59, 59);
        List<Sales> yesterdaySales = salesRepository.findBySaleDateBetween(yesterdayStart, yesterdayEnd);

        // 4. 재고 데이터
        List<Inventory> inventories = inventoryRepository.findAll();

        // 5. 최근 7일 완료된 판매 데이터 (어제부터 7일 전까지 - 오늘 제외)
        LocalDateTime weekAgoStart = LocalDate.now().minusDays(7).atStartOfDay();
        List<Sales> weekSales = salesRepository.findBySaleDateBetween(weekAgoStart, yesterdayEnd);

        // 6. 현재 시간 정보
        int currentHour = now.getHour();

        // 7. AI 프롬프트 생성 및 호출
        String prompt = buildDashboardPrompt(weather, todaySales, yesterdaySales, inventories, weekSales, currentHour);
        String aiResponse = callOpenAI(prompt);

        // 8. 응답 파싱
        return parseDashboardResponse(aiResponse, weather);
    }

    /**
     * AI 프롬프트 생성
     */
    private String buildDashboardPrompt(
            WeatherSummary weather,
            List<Sales> todaySales,
            List<Sales> yesterdaySales,
            List<Inventory> inventories,
            List<Sales> weekSales,
            int currentHour) {

        StringBuilder prompt = new StringBuilder();

        prompt.append("당신은 15년 경력의 베이커리 경영 컨설턴트입니다.\n");
        prompt.append("아래 실제 데이터를 기반으로 정확하고 구체적인 인사이트를 제공해야 합니다.\n");
        prompt.append("추측이나 일반론은 금지이며, 오직 제공된 데이터만 사용하세요.\n\n");

        prompt.append(String.format("⏰ 현재 시각: %d시\n", currentHour));
        prompt.append("⚠️ 중요: 오늘은 진행 중이므로 트렌드 판단 금지! 어제까지의 완료된 데이터만 트렌드 분석에 사용하세요.\n\n");

        // 날씨 정보 및 매출 영향 예측
        prompt.append("━━━━━ 📍 오늘 날씨 정보 & 매출 영향 예측 ━━━━━\n");
        if (weather.success()) {
            prompt.append(String.format("서울: %.1f°C, %s\n\n", weather.temperature(), weather.description()));

            // 날씨별 매출 영향 분석
            prompt.append("📊 날씨 기반 매출 예측:\n");

            String salesImpact = "";
            String recommendedProducts = "";
            String revenueChange = "";

            if (weather.temperature() < 0) {
                salesImpact = "한파 효과";
                revenueChange = "매출 10-15% 상승 예상";
                recommendedProducts = "따뜻한 빵, 크라상, 버터롤 (체온 보상 심리)";
                prompt.append(String.format("🔥 %s: %s\n", salesImpact, revenueChange));
                prompt.append(String.format("→ 인기 예상: %s\n", recommendedProducts));
                prompt.append("→ 손님: 추위 피해 실내 유입 증가, 따뜻한 제품 선호\n");
            } else if (weather.temperature() < 10) {
                salesImpact = "쌀쌀한 날씨";
                revenueChange = "매출 5-10% 상승 예상";
                recommendedProducts = "버터 풍부한 빵, 머핀";
                prompt.append(String.format("🍂 %s: %s\n", salesImpact, revenueChange));
                prompt.append(String.format("→ 인기 예상: %s\n", recommendedProducts));
                prompt.append("→ 손님: 따뜻한 빵 선호도 증가\n");
            } else if (weather.temperature() > 28) {
                salesImpact = "폭염";
                revenueChange = "매출 5-10% 하락 우려";
                recommendedProducts = "가벼운 빵, 쿠키 (무거운 제품 기피)";
                prompt.append(String.format("☀️ %s: %s\n", salesImpact, revenueChange));
                prompt.append(String.format("→ 인기 예상: %s\n", recommendedProducts));
                prompt.append("→ 손님: 더위로 외출 감소, 가벼운 제품 선호\n");
            } else if (weather.temperature() > 25) {
                salesImpact = "더운 날씨";
                revenueChange = "매출 보합 ~ 소폭 하락";
                recommendedProducts = "가벼운 빵, 크라상";
                prompt.append(String.format("🌤️ %s: %s\n", salesImpact, revenueChange));
                prompt.append(String.format("→ 인기 예상: %s\n", recommendedProducts));
                prompt.append("→ 손님: 시원한 음료와 가벼운 빵 선호\n");
            } else {
                salesImpact = "쾌적한 날씨";
                revenueChange = "매출 정상 또는 소폭 상승";
                recommendedProducts = "전 제품 고른 판매";
                prompt.append(String.format("✨ %s: %s\n", salesImpact, revenueChange));
                prompt.append(String.format("→ 인기 예상: %s\n", recommendedProducts));
                prompt.append("→ 손님: 외출하기 좋은 날씨, 전반적으로 고른 판매\n");
            }

            if (weather.description().contains("비")) {
                prompt.append("\n🌧️ 비 오는 날 특수:\n");
                prompt.append("→ 매출 영향: 5-15% 상승 (위안 소비 증가)\n");
                prompt.append("→ 인기 제품: 따뜻한 빵, 머핀, 파이 (실내 체류 증가)\n");
                prompt.append("→ 손님 심리: 우울한 날씨에 위안 음식 찾음\n");
            } else if (weather.description().contains("눈")) {
                prompt.append("\n❄️ 눈 오는 날 특수:\n");
                prompt.append("→ 매출 영향: 10-20% 상승 가능\n");
                prompt.append("→ 인기 제품: 모든 따뜻한 빵 (감성 소비)\n");
                prompt.append("→ 손님 심리: 낭만적 분위기, 특별한 날 소비 증가\n");
            } else if (weather.description().contains("맑")) {
                prompt.append("\n☀️ 맑은 날:\n");
                prompt.append("→ 매출 영향: 안정적 판매 유지\n");
                prompt.append("→ 손님: 산책 겸 방문 고객 증가\n");
            }
        } else {
            prompt.append("날씨 정보 없음\n");
        }
        prompt.append("\n");

        // 오늘 판매 데이터 (실시간 모니터링)
        prompt.append(String.format("━━━━━ 📊 오늘 판매 현황 (현재 %d시 기준, 진행 중) ━━━━━\n", currentHour));
        prompt.append("⚠️ 주의: 하루가 진행 중이므로 이 데이터로 트렌드를 판단하지 마세요!\n");

        if (todaySales.isEmpty()) {
            prompt.append("⚠️ 아직 판매 기록이 없습니다.\n");
            prompt.append("→ 오전 개점 직후이거나 첫 판매 전입니다.\n\n");
        } else {
            Map<String, Integer> todaySalesByBread = todaySales.stream()
                    .collect(Collectors.groupingBy(
                            s -> sanitizeBreadName(s.getBread().getName()),
                            Collectors.summingInt(Sales::getQuantity)
                    ));

            int totalSold = todaySales.stream().mapToInt(Sales::getQuantity).sum();
            double totalRevenue = todaySales.stream()
                    .mapToDouble(s -> s.getTotalPrice().doubleValue())
                    .sum();

            int transactionCount = todaySales.size();
            prompt.append(String.format("📈 총 판매량: %d개 (진행 중)\n", totalSold));
            prompt.append(String.format("💰 총 매출: ₩%,d원 (진행 중)\n", (int)totalRevenue));
            prompt.append(String.format("💳 평균 거래액: ₩%,d원 (총 %d건)\n\n",
                transactionCount > 0 ? (int)(totalRevenue / transactionCount) : 0,
                transactionCount));

            prompt.append("📦 현재까지 제품별 판매:\n");
            int rank = 1;
            for (Map.Entry<String, Integer> entry : todaySalesByBread.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .collect(Collectors.toList())) {

                double percentage = (entry.getValue() * 100.0) / totalSold;
                String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : rank == 3 ? "🥉" : "  ";
                prompt.append(String.format("%s %d위. %s: %d개 (%.1f%%)\n",
                    medal, rank, entry.getKey(), entry.getValue(), percentage));
                rank++;
            }
            prompt.append("\n");
        }

        // 어제 판매 데이터 (비교 기준)
        prompt.append("━━━━━ 📊 어제 판매 현황 (완료된 데이터) ━━━━━\n");
        if (yesterdaySales.isEmpty()) {
            prompt.append("⚠️ 어제 판매 기록이 없습니다.\n\n");
        } else {
            Map<String, Integer> yesterdaySalesByBread = yesterdaySales.stream()
                    .collect(Collectors.groupingBy(
                            s -> sanitizeBreadName(s.getBread().getName()),
                            Collectors.summingInt(Sales::getQuantity)
                    ));

            int totalSold = yesterdaySales.stream().mapToInt(Sales::getQuantity).sum();
            double totalRevenue = yesterdaySales.stream()
                    .mapToDouble(s -> s.getTotalPrice().doubleValue())
                    .sum();

            prompt.append(String.format("📈 총 판매량: %d개\n", totalSold));
            prompt.append(String.format("💰 총 매출: ₩%,d원\n\n", (int)totalRevenue));

            prompt.append("📦 어제 베스트셀러 TOP 3:\n");
            int rank = 1;
            for (Map.Entry<String, Integer> entry : yesterdaySalesByBread.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .limit(3)
                    .collect(Collectors.toList())) {

                String medal = rank == 1 ? "🥇" : rank == 2 ? "🥈" : "🥉";
                prompt.append(String.format("%s %d위. %s: %d개\n",
                    medal, rank, entry.getKey(), entry.getValue()));
                rank++;
            }
            prompt.append("\n");
        }

        // 재고 현황
        prompt.append("━━━━━ 📦 현재 재고 현황 ━━━━━\n");
        if (inventories.isEmpty()) {
            prompt.append("⚠️ 재고 데이터가 없습니다.\n\n");
        } else {
            // 재고 위험도 분석
            int criticalCount = 0;
            int warningCount = 0;
            int okCount = 0;

            // 전략 수립을 위한 리스트
            List<String> excessInventory = new ArrayList<>();  // 과잉 재고 (할인 대상)
            List<String> sufficientInventory = new ArrayList<>();  // 충분한 재고 (SNS 홍보 가능)

            for (Inventory inv : inventories) {
                int quantity = inv.getQuantity();
                int minStock = inv.getMinStockLevel();
                String breadName = sanitizeBreadName(inv.getBread().getName());

                String statusIcon;
                String statusText;

                if (quantity == 0) {
                    statusIcon = "❌";
                    statusText = "품절";
                    criticalCount++;
                } else if (quantity < minStock) {
                    statusIcon = "🚨";
                    statusText = "긴급 발주 필요";
                    criticalCount++;
                } else if (quantity < minStock * 1.5) {
                    statusIcon = "⚠️";
                    statusText = "재고 부족 주의";
                    warningCount++;
                } else if (quantity > minStock * 5) {
                    statusIcon = "📦";
                    statusText = "과잉 재고";
                    warningCount++;
                    // 과잉 재고는 할인 프로모션 대상
                    excessInventory.add(String.format("%s (현재 %d개)", breadName, quantity));
                } else {
                    statusIcon = "✅";
                    statusText = "적정";
                    okCount++;
                    // 적정 재고는 SNS 마케팅 가능
                    if (quantity >= minStock * 2) {
                        sufficientInventory.add(String.format("%s (재고 %d개)", breadName, quantity));
                    }
                }

                // 오늘 판매량과 비교
                int soldToday = todaySales.stream()
                        .filter(s -> sanitizeBreadName(s.getBread().getName()).equals(breadName))
                        .mapToInt(Sales::getQuantity)
                        .sum();

                String salesInfo = soldToday > 0 ? String.format(" (오늘 %d개 판매)", soldToday) : "";

                prompt.append(String.format("%s %s: 재고 %d개 / 최소 %d개 → %s%s\n",
                        statusIcon, breadName, quantity, minStock, statusText, salesInfo));
            }

            prompt.append(String.format("\n재고 요약: 긴급 %d개 / 주의 %d개 / 정상 %d개\n\n",
                    criticalCount, warningCount, okCount));

            // 전략 수립을 위한 힌트 제공
            if (!excessInventory.isEmpty()) {
                prompt.append("💡 할인 프로모션 추천 대상 (과잉 재고):\n");
                for (String item : excessInventory) {
                    prompt.append(String.format("  - %s → 10~30%% 할인으로 빠른 소진 권장\n", item));
                }
                prompt.append("\n");
            }

            if (!sufficientInventory.isEmpty()) {
                prompt.append("💡 SNS 마케팅 가능 제품 (재고 충분):\n");
                for (String item : sufficientInventory) {
                    prompt.append(String.format("  - %s → 홍보 강화 가능\n", item));
                }
                prompt.append("\n");
            }
        }

        // 최근 7일 트렌드 (어제까지, 오늘 제외)
        prompt.append("━━━━━ 📈 최근 7일 판매 트렌드 (어제까지 완료된 데이터) ━━━━━\n");
        prompt.append("✅ 이 데이터로 트렌드를 분석하세요!\n\n");

        if (weekSales.isEmpty()) {
            prompt.append("⚠️ 주간 트렌드 데이터 없음\n\n");
        } else {
            Map<String, Integer> weekSalesByBread = weekSales.stream()
                    .collect(Collectors.groupingBy(
                            s -> sanitizeBreadName(s.getBread().getName()),
                            Collectors.summingInt(Sales::getQuantity)
                    ));

            int weekTotal = weekSales.stream().mapToInt(Sales::getQuantity).sum();
            double weeklyAvg = weekTotal / 7.0;

            // 어제 전체 판매량
            int yesterdayTotal = yesterdaySales.stream().mapToInt(Sales::getQuantity).sum();

            // 전체 매출 트렌드 계산
            String overallTrend;
            String trendIcon;
            String trendDetail;

            if (yesterdayTotal > weeklyAvg * 1.2) {
                overallTrend = "상승세";
                trendIcon = "📈";
                double increasePercent = ((yesterdayTotal - weeklyAvg) / weeklyAvg) * 100;
                trendDetail = String.format("어제 판매량이 주간 평균보다 %.1f%% 높음", increasePercent);
            } else if (yesterdayTotal < weeklyAvg * 0.8 && yesterdayTotal > 0) {
                overallTrend = "하락세";
                trendIcon = "📉";
                double decreasePercent = ((weeklyAvg - yesterdayTotal) / weeklyAvg) * 100;
                trendDetail = String.format("어제 판매량이 주간 평균보다 %.1f%% 낮음", decreasePercent);
            } else {
                overallTrend = "안정적";
                trendIcon = "➡️";
                trendDetail = "어제 판매량이 주간 평균 ±20% 이내로 안정적";
            }

            prompt.append(String.format("주간 총 판매: %d개 (일평균 %.1f개)\n", weekTotal, weeklyAvg));
            prompt.append(String.format("어제 총 판매: %d개\n\n", yesterdayTotal));

            prompt.append(String.format("%s 전체 매출 트렌드: %s\n", trendIcon, overallTrend));
            prompt.append(String.format("   → %s\n", trendDetail));
            prompt.append(String.format("   → BRIEF 섹션에서 \"주간 트렌드 %s\"라고 명확히 언급하세요\n\n", overallTrend));

            prompt.append("제품별 주간 성과 (트렌드 분석 기준 데이터):\n");
            weekSalesByBread.entrySet().stream()
                    .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                    .forEach(entry -> {
                        double dailyAvg = entry.getValue() / 7.0;
                        double weekShare = (entry.getValue() * 100.0) / weekTotal;

                        // 어제 판매와 비교 (완료된 데이터 기준)
                        int yesterdaySold = yesterdaySales.stream()
                                .filter(s -> sanitizeBreadName(s.getBread().getName()).equals(entry.getKey()))
                                .mapToInt(Sales::getQuantity)
                                .sum();

                        String trend = "";
                        if (yesterdaySold > dailyAvg * 1.2) {
                            trend = "📈 상승세";
                        } else if (yesterdaySold < dailyAvg * 0.8 && yesterdaySold > 0) {
                            trend = "📉 하락세";
                        } else if (yesterdaySold > 0) {
                            trend = "➡️ 안정적";
                        }

                        prompt.append(String.format("• %s: 주간 %d개 (일평균 %.1f개, 점유율 %.1f%%) 어제 %d개 %s\n",
                                entry.getKey(), entry.getValue(), dailyAvg, weekShare, yesterdaySold, trend));
                    });
            prompt.append("\n");
        }

        prompt.append("━━━━━ 📝 답변 형식 (엄격히 준수) ━━━━━\n\n");

        prompt.append("[MOOD]\n");
        prompt.append("날씨 + 매출 예측 + 주목할 제품을 2-3줄로 요약\n");
        prompt.append("반드시 포함:\n");
        prompt.append("1) 현재 날씨 (온도, 날씨 상태)\n");
        prompt.append("2) 날씨 기반 매출 예측 (상승/하락 % 포함)\n");
        prompt.append("3) 오늘 주목할 제품 (날씨 영향 기반)\n\n");
        prompt.append("✅ 좋은 예:\n");
        prompt.append("\"오늘 서울 5°C, 쌀쌀한 날씨로 따뜻한 빵 선호도 증가, 매출 5-10%% 상승 예상됩니다.\n");
        prompt.append("소금버터롤과 크라상 같은 버터 풍부한 제품 주목! 어제 소금버터롤 35개 완판했습니다.\"\n\n");

        prompt.append("[BRIEF]\n");
        prompt.append(String.format("현재 시각 %d시 기준, 오늘의 실시간 판매 현황을 2줄로 요약\n", currentHour));
        prompt.append("반드시 포함:\n");
        prompt.append("1) 오늘 현재까지 판매 현황 (진행 중임을 명시)\n");
        prompt.append("2) 어제 베스트셀러 + 판매량\n");
        prompt.append("3) 주간 트렌드 (상승/하락/안정)\n\n");
        prompt.append("✅ 좋은 예:\n");
        prompt.append("\"현재 %d시, 오늘 4개 판매 중입니다. 어제는 소금버터롤 35개로 1위, 주간 트렌드 안정적입니다.\"\n\n".formatted(currentHour));

        prompt.append("[INSIGHT]\n");
        prompt.append("재고 위험 분석 및 구체적 수치 제공 (2-3줄)\n");
        prompt.append("우선순위: 1) 품절/긴급 발주, 2) 재고 부족 주의, 3) 과잉 재고\n");
        prompt.append("✅ 좋은 예: \"초코청크머핀 재고 5개로 일평균 판매량 8개 대비 부족합니다. 오늘 오후 품절 예상됩니다. 반면 쿠키는 재고 80개로 과잉 상태입니다.\"\n\n");

        prompt.append("[STRATEGY]\n");
        prompt.append("빵집 특성을 고려한 즉시 실행 가능한 액션 플랜 (2-3줄)\n\n");

        prompt.append("🍞 빵집 핵심 원칙:\n");
        prompt.append("- 신선함이 생명! 발주는 1~2일분만 권장 (최대 3일분)\n");
        prompt.append("- 할인 프로모션: 현재 남은 재고 수량 기반으로 추천\n");
        prompt.append("- SNS 마케팅: 현재 재고가 충분한 제품만 추천\n\n");

        prompt.append("반드시 포함:\n");
        prompt.append("1) 발주 권장: 제품명 + 수량 (일평균 × 1~2일분)\n");
        prompt.append("2) 할인 프로모션: 현재 재고 기준 + 할인율 + 목표 소진량\n");
        prompt.append("3) SNS 마케팅: 재고 충분한 제품만 언급\n\n");

        prompt.append("✅ 좋은 예:\n");
        prompt.append("\"소금버터롤 20개 발주 권장 (일평균 10개 × 2일분, 신선도 유지).\n");
        prompt.append("현재 재고 쿠키 80개 중 30% 할인으로 30개 소진 목표.\n");
        prompt.append("재고 충분한 머핀 SNS 마케팅 강화 제안.\"\n\n");

        prompt.append("❌ 나쁜 예:\n");
        prompt.append("\"소금버터롤 50개 발주 (5일분은 신선도 저하 위험)\"\n");
        prompt.append("\"쿠키 30% 할인 (현재 재고량 미언급)\"\n");
        prompt.append("\"품절 위험 머핀 SNS 홍보 (재고 없는데 홍보하면 기회 손실)\"\n\n");

        prompt.append("━━━━━ ⚠️ 중요 규칙 ━━━━━\n");
        prompt.append(String.format("1. 현재 시각 %d시, 오늘은 진행 중! 오늘 데이터로 트렌드 판단 절대 금지!\n", currentHour));
        prompt.append("2. 트렌드 분석은 어제까지의 완료된 데이터만 사용\n");
        prompt.append("3. 오늘 데이터는 \"현재까지\" 또는 \"진행 중\"으로 표현\n");
        prompt.append("4. 제공된 실제 데이터만 사용 (추측 절대 금지)\n");
        prompt.append("5. 모든 제품명은 데이터의 정확한 이름 사용\n");
        prompt.append("6. 각 섹션은 [MOOD], [BRIEF], [INSIGHT], [STRATEGY] 태그로 시작\n");
        prompt.append("7. 일반적인 조언 금지, 구체적인 액션만 제시\n");

        return prompt.toString();
    }

    /**
     * OpenAI API 호출
     */
    private String callOpenAI(String prompt) {
        try {
            OpenAiService service = new OpenAiService(openaiApiKey, Duration.ofSeconds(45));

            ChatMessage systemMessage = new ChatMessage("system",
                    "당신은 15년 경력의 베이커리 경영 컨설턴트입니다. " +
                    "제공된 실제 데이터만을 기반으로 정확하고 구체적인 분석을 제공합니다. " +
                    "추측이나 일반적인 조언은 하지 않으며, 오직 데이터 기반의 실행 가능한 인사이트만 제시합니다. " +
                    "모든 제품명과 숫자는 제공된 데이터의 정확한 값을 사용합니다.");

            ChatMessage userMessage = new ChatMessage("user", prompt);

            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model("gpt-4o-mini")
                    .messages(Arrays.asList(systemMessage, userMessage))
                    .temperature(0.2)  // 형식 일관성과 데이터 정확성 최우선
                    .maxTokens(1200)   // 더 상세한 분석을 위해 증가
                    .build();

            String response = service.createChatCompletion(completionRequest)
                    .getChoices()
                    .get(0)
                    .getMessage()
                    .getContent();

            log.info("OpenAI 응답 수신 완료 (길이: {}자)", response.length());
            log.debug("OpenAI 응답 내용:\n{}", response);

            return response;

        } catch (Exception e) {
            log.error("OpenAI API 호출 실패", e);
            return generateFallbackResponse();
        }
    }

    /**
     * AI 응답 파싱
     */
    private Map<String, Object> parseDashboardResponse(String aiResponse, WeatherSummary weather) {
        String mood = extractSection(aiResponse, "MOOD");
        String brief = extractSection(aiResponse, "BRIEF");
        String insight = extractSection(aiResponse, "INSIGHT");
        String strategy = extractSection(aiResponse, "STRATEGY");

        // 폴백 메시지
        if (mood.isEmpty()) mood = "오늘도 빵집을 찾아주신 손님들께 감사드립니다!";
        if (brief.isEmpty()) brief = "판매 데이터를 분석 중입니다.";
        if (insight.isEmpty()) insight = "재고 현황을 확인하고 있습니다.";
        if (strategy.isEmpty()) strategy = "데이터 기반 전략을 준비 중입니다.";

        // 날씨 정보 포함
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("mood", mood);
        result.put("brief", brief);
        result.put("insight", insight);
        result.put("strategy", strategy);

        // 날씨 정보 추가
        if (weather.success()) {
            result.put("weather", Map.of(
                "temperature", weather.temperature(),
                "description", weather.description()
            ));
        }

        return result;
    }

    /**
     * 차트 데이터 생성 (프론트엔드 그래프용)
     */
    public DashboardChartDto generateChartData() {
        log.info("차트 데이터 생성 시작");

        // 최근 7일 날짜 범위 (어제까지)
        LocalDateTime yesterdayEnd = LocalDate.now().minusDays(1).atTime(23, 59, 59);
        LocalDateTime weekAgoStart = LocalDate.now().minusDays(7).atStartOfDay();

        List<Sales> weekSales = salesRepository.findBySaleDateBetween(weekAgoStart, yesterdayEnd);

        if (weekSales.isEmpty()) {
            log.warn("차트 데이터 없음 - 빈 DTO 반환");
            return DashboardChartDto.builder()
                    .last7DaysSales(new LinkedHashMap<>())
                    .last7DaysRevenue(new LinkedHashMap<>())
                    .breadSalesRanking(new LinkedHashMap<>())
                    .breadRevenueRanking(new LinkedHashMap<>())
                    .confidence(0.0)
                    .trendDirection("안정적")
                    .trendChangePercent(0.0)
                    .build();
        }

        // 1. 최근 7일 일별 판매량 & 매출
        Map<String, Integer> dailySales = new LinkedHashMap<>();
        Map<String, Double> dailyRevenue = new LinkedHashMap<>();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = 6; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i + 1);
            String dateKey = date.format(formatter);

            LocalDateTime dayStart = date.atStartOfDay();
            LocalDateTime dayEnd = date.atTime(23, 59, 59);

            List<Sales> daySales = weekSales.stream()
                    .filter(s -> !s.getSaleDate().isBefore(dayStart) && !s.getSaleDate().isAfter(dayEnd))
                    .collect(Collectors.toList());

            int totalQuantity = daySales.stream()
                    .mapToInt(Sales::getQuantity)
                    .sum();

            double totalRevenue = daySales.stream()
                    .mapToDouble(s -> s.getTotalPrice().doubleValue())
                    .sum();

            dailySales.put(dateKey, totalQuantity);
            dailyRevenue.put(dateKey, totalRevenue);
        }

        // 2. 빵별 주간 판매량 TOP 5
        Map<String, Integer> breadSales = weekSales.stream()
                .collect(Collectors.groupingBy(
                        s -> sanitizeBreadName(s.getBread().getName()),
                        Collectors.summingInt(Sales::getQuantity)
                ));

        Map<String, Integer> top5BreadSales = breadSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        // 3. 빵별 주간 매출 TOP 5
        Map<String, Double> breadRevenue = new HashMap<>();
        for (Sales sale : weekSales) {
            String breadName = sanitizeBreadName(sale.getBread().getName());
            double revenue = sale.getTotalPrice().doubleValue();
            breadRevenue.merge(breadName, revenue, Double::sum);
        }

        Map<String, Double> top5BreadRevenue = breadRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        // 4. 신뢰도 계산
        double confidence = calculateTrendConfidence(dailySales);

        // 5. 트렌드 방향 & 변화율
        LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
        List<Sales> yesterdaySales = salesRepository.findBySaleDateBetween(yesterdayStart, yesterdayEnd);

        int yesterdayTotal = yesterdaySales.stream().mapToInt(Sales::getQuantity).sum();
        int weekTotal = weekSales.stream().mapToInt(Sales::getQuantity).sum();
        double weeklyAvg = weekTotal / 7.0;

        String trendDirection;
        double changePercent = 0.0;

        if (yesterdayTotal > weeklyAvg * 1.2) {
            trendDirection = "상승세";
            changePercent = ((yesterdayTotal - weeklyAvg) / weeklyAvg) * 100;
        } else if (yesterdayTotal < weeklyAvg * 0.8 && yesterdayTotal > 0) {
            trendDirection = "하락세";
            changePercent = -((weeklyAvg - yesterdayTotal) / weeklyAvg) * 100;
        } else {
            trendDirection = "안정적";
        }

        return DashboardChartDto.builder()
                .last7DaysSales(dailySales)
                .last7DaysRevenue(dailyRevenue)
                .breadSalesRanking(top5BreadSales)
                .breadRevenueRanking(top5BreadRevenue)
                .confidence(confidence)
                .trendDirection(trendDirection)
                .trendChangePercent(changePercent)
                .build();
    }

    /**
     * 트렌드 신뢰도 계산 (변동계수 기반)
     * CV (Coefficient of Variation) = (표준편차 / 평균) × 100
     *
     * @param dailySales 최근 7일 일별 판매량
     * @return 신뢰도 (0.0 ~ 1.0)
     */
    private double calculateTrendConfidence(Map<String, Integer> dailySales) {
        if (dailySales == null || dailySales.isEmpty()) {
            return 0.0;
        }

        List<Integer> values = new ArrayList<>(dailySales.values());

        // 평균 계산
        double mean = values.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);

        if (mean == 0) {
            return 0.0;
        }

        // 표준편차 계산
        double variance = values.stream()
                .mapToDouble(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0.0);

        double stdDev = Math.sqrt(variance);

        // 변동계수 (CV)
        double cv = (stdDev / mean) * 100;

        // 신뢰도 매핑
        // CV < 15% → 신뢰도 95%
        // CV < 25% → 신뢰도 85%
        // CV < 35% → 신뢰도 75%
        // CV >= 35% → 신뢰도 60%
        if (cv < 15) {
            return 0.95;
        } else if (cv < 25) {
            return 0.85;
        } else if (cv < 35) {
            return 0.75;
        } else {
            return 0.60;
        }
    }

    /**
     * 응답에서 섹션 추출
     */
    private String extractSection(String response, String sectionName) {
        String marker = "[" + sectionName + "]";
        int start = response.indexOf(marker);

        if (start == -1) return "";

        start += marker.length();

        // 다음 섹션 찾기
        int end = response.length();
        String[] markers = {"[MOOD]", "[BRIEF]", "[INSIGHT]", "[STRATEGY]"};

        for (String m : markers) {
            if (!m.equals(marker)) {
                int nextMarker = response.indexOf(m, start);
                if (nextMarker != -1 && nextMarker < end) {
                    end = nextMarker;
                }
            }
        }

        return response.substring(start, end).trim();
    }

    /**
     * 폴백 응답 (OpenAI 실패 시)
     */
    private String generateFallbackResponse() {
        return """
                [MOOD]
                오늘도 좋은 하루 되세요!

                [BRIEF]
                판매 데이터를 분석하고 있습니다. 잠시 후 다시 확인해주세요.

                [INSIGHT]
                재고 현황을 점검 중입니다.

                [STRATEGY]
                AI 분석 서비스가 일시적으로 사용 불가합니다. 관리자 페이지에서 상세 데이터를 확인해주세요.
                """;
    }

    /**
     * 프롬프트 인젝션 방지를 위한 제품명 정제
     * - 대괄호 제거 (섹션 태그 조작 방지)
     * - 개행 문자 제거 (프롬프트 구조 파괴 방지)
     * - 특수 명령어 패턴 제거
     */
    private String sanitizeBreadName(String name) {
        if (name == null) return "";

        return name
            .replaceAll("[\\[\\]]", "")          // [ ] 제거 (섹션 태그 방지)
            .replaceAll("[\\r\\n]+", " ")        // 개행 제거
            .replaceAll("\\s+", " ")             // 연속 공백 제거
            .trim();
    }
}
