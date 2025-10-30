package com.ddbb.service.ai;

import com.ddbb.client.GeminiClient;
import com.ddbb.entity.management.Inventory;
import com.ddbb.entity.management.Sales;
import com.ddbb.repository.management.InventoryRepository;
import com.ddbb.repository.management.SalesRepository;
import com.ddbb.service.aidashboard.WeatherService;
import com.ddbb.service.aidashboard.WeatherService.WeatherSummary;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiInsightService {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getInstance(Locale.KOREA);

    private final SalesRepository salesRepository;
    private final InventoryRepository inventoryRepository;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final WeatherService weatherService;

    public Map<String, Object> generateInsights() {
        LocalDate today = LocalDate.now();
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);

        List<Sales> sales = salesRepository.findSalesInPeriod(startOfDay, endOfDay);
        List<Inventory> inventories = inventoryRepository.findAllWithBread();

        List<String> salesSummaries = buildSalesSummaries(sales);
        List<String> inventorySummaries = buildInventorySummaries(inventories);
        String weatherSummary = buildWeatherSummary();

        String prompt = buildPrompt(weatherSummary, salesSummaries, inventorySummaries);
        String aiResponse = geminiClient.requestInsights(prompt);

        String cleanedResponse = cleanupJson(aiResponse);
        try {
            return objectMapper.readValue(cleanedResponse, new TypeReference<>() {});
        } catch (Exception e) {
            log.error("Failed to parse Gemini response: {}", cleanedResponse, e);
            throw new RuntimeException("Failed to parse Gemini response", e);
        }
    }

    private List<String> buildSalesSummaries(List<Sales> sales) {
        if (sales.isEmpty()) {
            return List.of("데이터 없음");
        }

        Map<Long, SalesAggregate> aggregateMap = new LinkedHashMap<>();
        for (Sales s : sales) {
            Long breadId = s.getBread().getId();
            SalesAggregate aggregate = aggregateMap.computeIfAbsent(breadId, ignored -> new SalesAggregate(
                    s.getBread().getName(),
                    s.getBread().getPrice(),
                    0,
                    BigDecimal.ZERO
            ));
            aggregate.addQuantity(s.getQuantity());
            aggregate.addRevenue(s.getTotalPrice());
        }

        List<String> summaries = new ArrayList<>();
        aggregateMap.values().forEach(aggregate -> summaries.add(String.format(
                "[상품명: %s, 판매수량: %d, 단가: %s, 매출합계: %s]",
                aggregate.name(),
                aggregate.totalQuantity(),
                NUMBER_FORMAT.format(aggregate.unitPrice()),
                NUMBER_FORMAT.format(aggregate.totalRevenue())
        )));
        return summaries;
    }

    private List<String> buildInventorySummaries(List<Inventory> inventories) {
        if (inventories.isEmpty()) {
            return List.of("데이터 없음");
        }

        List<String> summaries = new ArrayList<>();
        inventories.forEach(inventory -> summaries.add(String.format(
                "[상품명: %s, 재고수량: %d, 공급업체: %s]",
                inventory.getBread().getName(),
                inventory.getQuantity(),
                resolveProvider(inventory)
        )));
        return summaries;
    }

    private String resolveProvider(Inventory inventory) {
        // TODO: replace with real provider information when available
        return "미등록";
    }

    private String buildWeatherSummary() {
        WeatherSummary summary = weatherService.fetchSeoulWeather();
        if (summary.success()) {
            Double temperature = summary.temperature();
            return String.format(
                    "[현재 기온: %s, 하늘 상태: %s, 요약: %s]",
                    temperature != null ? String.format("%.1f°C", temperature) : "미확인",
                    summary.description() != null ? summary.description() : "미확인",
                    summary.summary() != null ? summary.summary() : "날씨 정보를 불러오지 못했습니다"
            );
        }
        return "[날씨 데이터를 불러오지 못했습니다]";
    }

    private String buildPrompt(String weatherSummary, List<String> salesSummary, List<String> inventorySummary) {
        return "오늘의 판매, 재고, 날씨 데이터입니다.\n" +
                "날씨 데이터: " + weatherSummary + "\n" +
                "판매 데이터: " + String.join(" | ", salesSummary) + "\n" +
                "재고 데이터: " + String.join(" | ", inventorySummary) + "\n" +
                "이 데이터를 기반으로 오늘의 인사이트 4개를 만들어줘:\n" +
                "1. ☀️ 오늘의 베이커리 무드 (날씨 + 판매 분위기 요약)\n" +
                "2. 🧠 AI 일일 브리핑 (판매 트렌드 요약)\n" +
                "3. 📦 재고 인사이트 (부족 or 과잉 품목 요약)\n" +
                "4. 🎯 전략 제안 / 프로모션 Insight (판매 + 재고 기반 마케팅 제안)\n" +
                "따뜻한 톤으로 써주고, 결과는 JSON으로 반환해줘:\n" +
                "{ \"mood\": \"...\", \"briefing\": \"...\", \"inventory\": \"...\", \"strategy\": \"...\" }";
    }

    private String cleanupJson(String aiResponse) {
        String trimmed = aiResponse == null ? "" : aiResponse.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(json)?", "").trim();
        }
        if (trimmed.endsWith("```")) {
            trimmed = trimmed.substring(0, trimmed.length() - 3).trim();
        }
        return trimmed;
    }

    private static class SalesAggregate {
        private final String name;
        private final BigDecimal unitPrice;
        private int totalQuantity;
        private BigDecimal totalRevenue;

        SalesAggregate(String name, BigDecimal unitPrice, int totalQuantity, BigDecimal totalRevenue) {
            this.name = name;
            this.unitPrice = unitPrice;
            this.totalQuantity = totalQuantity;
            this.totalRevenue = totalRevenue;
        }

        void addQuantity(int additionalQuantity) {
            this.totalQuantity += additionalQuantity;
        }

        void addRevenue(BigDecimal additionalRevenue) {
            this.totalRevenue = this.totalRevenue.add(additionalRevenue);
        }

        public String name() {
            return name;
        }

        public BigDecimal unitPrice() {
            return unitPrice;
        }

        public int totalQuantity() {
            return totalQuantity;
        }

        public BigDecimal totalRevenue() {
            return totalRevenue;
        }
    }
}
