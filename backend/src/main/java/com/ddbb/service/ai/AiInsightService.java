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
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiInsightService {

    private static final NumberFormat CURRENCY_FORMAT = NumberFormat.getInstance(Locale.KOREA);
    private static final int LOW_STOCK_DEFAULT_THRESHOLD = 5;
    private static final int AMPLE_STOCK_MULTIPLIER = 2;
    private static final int MAX_LIST_SIZE = 3;

    private final SalesRepository salesRepository;
    private final InventoryRepository inventoryRepository;
    private final GeminiClient geminiClient;
    private final ObjectMapper objectMapper;
    private final WeatherService weatherService;

    public Map<String, Object> generateInsights() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        LocalDateTime oneHourAgo = now.minusHours(1);

        List<Sales> todaysSales = salesRepository.findSalesInPeriod(startOfDay, endOfDay);
        List<Inventory> inventories = inventoryRepository.findAllWithBread();

        SalesAnalytics salesAnalytics = analyseSales(todaysSales, oneHourAgo);
        InventoryAnalytics inventoryAnalytics = analyseInventory(inventories);
        WeatherContext weatherContext = buildWeatherContext();

        GeminiResult geminiResult = requestGeminiInsights(salesAnalytics, inventoryAnalytics, weatherContext);

        String moodMessage = buildMoodMessage(weatherContext, salesAnalytics);
        String briefing = resolveBriefing(geminiResult.brief(), salesAnalytics, inventoryAnalytics);
        String inventoryInsight = resolveInventoryInsight(geminiResult.inventory(), salesAnalytics, inventoryAnalytics);
        String strategy = resolveStrategy(geminiResult.strategy(), salesAnalytics, inventoryAnalytics, weatherContext);

        Map<String, Object> result = new HashMap<>();
        result.put("mood", moodMessage);
        result.put("brief", briefing);
        result.put("insight", inventoryInsight);
        result.put("strategy", strategy);
        result.put("generatedAt", now.toString());
        return result;
    }

    private GeminiResult requestGeminiInsights(SalesAnalytics salesAnalytics,
                                               InventoryAnalytics inventoryAnalytics,
                                               WeatherContext weatherContext) {
        String prompt = buildPrompt(salesAnalytics, inventoryAnalytics, weatherContext);

        try {
            String rawResponse = geminiClient.requestInsights(prompt);
            String cleaned = cleanupJson(rawResponse);
            if (cleaned.isBlank()) {
                return GeminiResult.empty();
            }
            Map<String, String> map = objectMapper.readValue(cleaned, new TypeReference<>() {});
            return new GeminiResult(
                    normalize(map.get("mood")),
                    normalize(Optional.ofNullable(map.get("brief")).orElse(map.get("briefing"))),
                    normalize(map.get("inventory")),
                    normalize(map.get("strategy"))
            );
        } catch (Exception e) {
            log.warn("Gemini 분석 응답을 사용하지 못했습니다. Fallback 로직으로 대체합니다.", e);
            return GeminiResult.empty();
        }
    }

    private String buildPrompt(SalesAnalytics salesAnalytics,
                               InventoryAnalytics inventoryAnalytics,
                               WeatherContext weatherContext) {
        StringBuilder builder = new StringBuilder();
        builder.append("너는 제과점 데이터를 분석해 간결한 보고서를 작성하는 AI야. ")
                .append("문체는 요약 중심, 간단명료, 사실 전달형으로 써.\n")
                .append("오늘 날짜: ").append(LocalDate.now()).append("\n\n");

        builder.append("=== 판매 요약 ===\n");
        if (!salesAnalytics.hasSales()) {
            builder.append("금일 판매 데이터가 아직 없습니다.\n");
        } else {
            builder.append("총 판매 수량: ").append(salesAnalytics.totalQuantity()).append("개, ")
                    .append("총 매출: ").append(formatCurrency(salesAnalytics.totalRevenue())).append("\n");
            builder.append("품목별 요약:\n");
            for (SalesAggregate aggregate : salesAnalytics.aggregates()) {
                builder.append("- ").append(aggregate.name())
                        .append(": ").append(aggregate.totalQuantity()).append("개, ")
                        .append("매출 ").append(formatCurrency(aggregate.totalRevenue()));
                if (aggregate.latestSaleAt() != null) {
                    builder.append(", 마지막 판매 ").append(formatTimeAgo(aggregate.latestSaleAt()));
                }
                builder.append("\n");
            }
        }

        builder.append("\n=== 재고 요약 ===\n");
        if (!inventoryAnalytics.hasInventory()) {
            builder.append("재고 데이터가 없습니다.\n");
        } else {
            builder.append("총 등록 품목: ").append(inventoryAnalytics.totalItems()).append("개\n");
            if (!inventoryAnalytics.lowStock().isEmpty()) {
                builder.append("부족 재고 Top3: ")
                        .append(String.join(", ", inventoryAnalytics.lowStockTopN(MAX_LIST_SIZE)))
                        .append("\n");
            } else {
                builder.append("부족 재고 품목 없음.\n");
            }
            if (!inventoryAnalytics.ampleStock().isEmpty()) {
                builder.append("과잉 재고 Top3: ")
                        .append(String.join(", ", inventoryAnalytics.ampleStockTopN(MAX_LIST_SIZE)))
                        .append("\n");
            } else {
                builder.append("과잉 재고 품목 없음.\n");
            }
        }

        builder.append("\n=== 날씨 요약 ===\n");
        if (!weatherContext.available()) {
            builder.append("날씨 데이터를 불러오지 못했습니다.\n");
        } else {
            builder.append("현재 기온 ").append(weatherContext.temperatureText())
                    .append(", 상태: ").append(weatherContext.description())
                    .append(". 요약: ").append(weatherContext.summary()).append("\n");
        }

        builder.append("\nJSON 형식으로 아래 세 필드를 작성해:\n")
                .append("{\n")
                .append("  \"brief\": \"금일 판매 트렌드에 대한 보고\",\n")
                .append("  \"inventory\": \"재고 상태 평가\",\n")
                .append("  \"strategy\": \"프로모션 또는 운영 전략 제안\"\n")
                .append("}\n")
                .append("각 필드는 두 문장 이내, 보고서 톤, 이모지나 불필요한 감탄사 없이 작성해.");

        return builder.toString();
    }

    private String resolveBriefing(String aiBrief,
                                   SalesAnalytics salesAnalytics,
                                   InventoryAnalytics inventoryAnalytics) {
        if (salesAnalytics.hasSales() && !aiBrief.isBlank()) {
            return aiBrief;
        }

        List<String> lowStock = inventoryAnalytics.lowStockTopN(MAX_LIST_SIZE);
        if (!salesAnalytics.hasSales()) {
            StringBuilder builder = new StringBuilder("오늘은 아직 판매 내역이 없습니다.");
            if (!lowStock.isEmpty()) {
                builder.append(" 부족 재고 Top3: ").append(String.join(", ", lowStock)).append(".");
            }
            return builder.toString();
        }

        if (!salesAnalytics.hasRecentSales()) {
            StringBuilder builder = new StringBuilder("지난 한 시간 동안 새로운 판매가 없었습니다.");
            if (!lowStock.isEmpty()) {
                builder.append(" 부족 재고 Top3: ").append(String.join(", ", lowStock)).append(".");
            }
            return builder.toString();
        }

        return buildBriefingFromSales(salesAnalytics);
    }

    private String buildBriefingFromSales(SalesAnalytics salesAnalytics) {
        StringBuilder builder = new StringBuilder();
        builder.append("금일 판매 ").append(salesAnalytics.totalQuantity()).append("개, 총 매출 ")
                .append(formatCurrency(salesAnalytics.totalRevenue())).append(".");

        List<SalesAggregate> topSellers = salesAnalytics.topAggregates(2);
        if (!topSellers.isEmpty()) {
            SalesAggregate top = topSellers.get(0);
            builder.append(" 베스트셀러 ").append(top.name())
                    .append(" ").append(top.totalQuantity()).append("개(")
                    .append(formatCurrency(top.totalRevenue())).append(") 판매.");
            if (topSellers.size() > 1) {
                SalesAggregate runnerUp = topSellers.get(1);
                builder.append(" 차점은 ").append(runnerUp.name()).append(" ")
                        .append(runnerUp.totalQuantity()).append("개.");
            }
        }

        if (salesAnalytics.latestSaleAt() != null) {
            builder.append(" 마지막 거래 ").append(formatTimeAgo(salesAnalytics.latestSaleAt())).append(".");
        }
        return builder.toString();
    }

    private String resolveInventoryInsight(String aiInventory,
                                           SalesAnalytics salesAnalytics,
                                           InventoryAnalytics inventoryAnalytics) {
        if (!aiInventory.isBlank()) {
            return aiInventory;
        }

        if (!inventoryAnalytics.hasInventory()) {
            return "등록된 재고 데이터가 없습니다. 재고를 먼저 입력해 주세요.";
        }

        List<String> lowStock = inventoryAnalytics.lowStockTopN(MAX_LIST_SIZE);
        if (!salesAnalytics.hasSales()) {
            if (lowStock.isEmpty()) {
                return "금일 판매 데이터가 없어 재고 추세 분석이 제한됩니다. 현재는 관찰 위주로 운영하세요.";
            }
            return "금일 판매 데이터가 없어 추가 분석이 어렵습니다. 부족 재고 Top3: " + String.join(", ", lowStock) + ".";
        }

        String tone = determineStockTone(inventoryAnalytics);
        StringBuilder builder = new StringBuilder("재고 진단: ").append(tone).append(".");

        if (!lowStock.isEmpty()) {
            builder.append(" 부족 품목: ").append(String.join(", ", lowStock)).append(".");
        }

        List<String> ample = inventoryAnalytics.ampleStockTopN(MAX_LIST_SIZE);
        if (!ample.isEmpty()) {
            builder.append(" 과잉 품목: ").append(String.join(", ", ample)).append(".");
        }

        return builder.toString();
    }

    private String resolveStrategy(String aiStrategy,
                                   SalesAnalytics salesAnalytics,
                                   InventoryAnalytics inventoryAnalytics,
                                   WeatherContext weatherContext) {
        if (!aiStrategy.isBlank()) {
            return aiStrategy;
        }

        boolean hasSales = salesAnalytics.hasSales();
        boolean hasInventory = inventoryAnalytics.hasInventory();

        if (!hasSales && !hasInventory) {
            return "판매와 재고 데이터가 모두 없어 전략 제안을 생성할 수 없습니다. POS와 재고 입력을 먼저 완료해 주세요.";
        }

        if (!hasSales) {
            List<String> lowStock = inventoryAnalytics.lowStockTopN(MAX_LIST_SIZE);
            if (lowStock.isEmpty()) {
                return "판매 데이터가 없어 수요 예측이 어렵습니다. 재고 수준을 모니터링하면서 기본 운영을 유지하세요.";
            }
            return "판매 데이터가 부족합니다. 우선 보충할 품목: " + String.join(", ", lowStock) + ".";
        }

        if (!hasInventory) {
            List<SalesAggregate> top = salesAnalytics.topAggregates(2);
            if (top.isEmpty()) {
                return "재고 데이터가 없어 생산 계획을 확정할 수 없습니다. 판매 시스템만으로는 추가 분석이 제한됩니다.";
            }
            SalesAggregate best = top.get(0);
            StringBuilder builder = new StringBuilder("재고 데이터가 없어 정확한 배분이 어렵습니다.");
            builder.append(" 판매 기준 베스트셀러 ").append(best.name())
                    .append(" ").append(best.totalQuantity()).append("개를 기준 생산량으로 유지하세요.");
            if (top.size() > 1) {
                SalesAggregate runnerUp = top.get(1);
                builder.append(" 차점 ").append(runnerUp.name())
                        .append("도 재고 확인 후 추가 생산을 검토하세요.");
            }
            return builder.toString();
        }

        return buildStrategyFallback(salesAnalytics, inventoryAnalytics, weatherContext);
    }

    private String buildStrategyFallback(SalesAnalytics salesAnalytics,
                                         InventoryAnalytics inventoryAnalytics,
                                         WeatherContext weatherContext) {
        String base = buildStrategyBaseSentence(salesAnalytics, inventoryAnalytics);

        if (!weatherContext.available()) {
            return base;
        }

        String visitorForecast = buildVisitorForecast(weatherContext);
        String stockAction = buildStockAction(inventoryAnalytics, salesAnalytics);

        if (visitorForecast.isBlank() || stockAction.isBlank()) {
            return base;
        }

        return base + " " + visitorForecast + " " + stockAction;
    }

    private String buildStrategyBaseSentence(SalesAnalytics salesAnalytics,
                                             InventoryAnalytics inventoryAnalytics) {
        StringBuilder builder = new StringBuilder("전략 요약: ");
        builder.append("금일 매출 ").append(formatCurrency(salesAnalytics.totalRevenue()))
                .append(", 판매 ").append(salesAnalytics.totalQuantity()).append("개.");

        List<SalesAggregate> top = salesAnalytics.topAggregates(2);
        if (!top.isEmpty()) {
            SalesAggregate best = top.get(0);
            builder.append(" 주력 ").append(best.name())
                    .append(" ").append(best.totalQuantity()).append("개 판매.");
            if (top.size() > 1) {
                SalesAggregate runnerUp = top.get(1);
                builder.append(" 차점 ").append(runnerUp.name())
                        .append(" ").append(runnerUp.totalQuantity()).append("개.");
            }
        }

        List<String> lowStock = inventoryAnalytics.lowStockTopN(MAX_LIST_SIZE);
        if (!lowStock.isEmpty()) {
            builder.append(" 부족 재고: ").append(String.join(", ", lowStock)).append(".");
        }

        List<String> ample = inventoryAnalytics.ampleStockTopN(MAX_LIST_SIZE);
        if (!ample.isEmpty()) {
            builder.append(" 과잉 재고: ").append(String.join(", ", ample)).append(".");
        }

        return builder.toString();
    }

    private String buildVisitorForecast(WeatherContext weatherContext) {
        String description = Optional.ofNullable(weatherContext.description()).orElse("").toLowerCase(Locale.ROOT);
        Double temperature = weatherContext.temperature();

        if (description.contains("비") || description.contains("눈") || description.contains("소나기")) {
            return "강수 예보로 방문객 감소 가능성이 높습니다. 우천 대비 안내문을 준비하세요.";
        }

        if (temperature != null) {
            if (temperature >= 28) {
                return "기온이 높아 오후 방문이 줄 수 있습니다. 시원한 음료와 포장 프로모션을 강조하세요.";
            }
            if (temperature <= 2) {
                return "기온이 낮아 체류 시간이 짧아질 수 있습니다. 따뜻한 제품을 전면 배치하세요.";
            }
        }

        return "날씨가 안정적이라 평소 수준의 방문을 예상할 수 있습니다.";
    }

    private String buildStockAction(InventoryAnalytics inventoryAnalytics, SalesAnalytics salesAnalytics) {
        if (!inventoryAnalytics.lowStock().isEmpty()) {
            Inventory first = inventoryAnalytics.lowStock().get(0);
            return first.getBread().getName() + " 재고를 즉시 보충하세요.";
        }

        if (!inventoryAnalytics.ampleStock().isEmpty()) {
            Inventory ample = inventoryAnalytics.ampleStock().get(0);
            return ample.getBread().getName() + " 중심의 타임세일을 진행해 재고를 소진하세요.";
        }

        List<SalesAggregate> top = salesAnalytics.topAggregates(1);
        if (!top.isEmpty()) {
            SalesAggregate aggregate = top.get(0);
            return aggregate.name() + " 생산량을 저녁 교대에 한 번 더 확보하세요.";
        }

        return "";
    }

    private SalesAnalytics analyseSales(List<Sales> sales, LocalDateTime thresholdForRecent) {
        if (sales.isEmpty()) {
            return SalesAnalytics.empty();
        }

        Map<Long, SalesAggregate> aggregateMap = new LinkedHashMap<>();
        LocalDateTime latestSale = null;
        BigDecimal totalRevenue = BigDecimal.ZERO;
        int totalQuantity = 0;

        for (Sales sale : sales) {
            Long breadId = sale.getBread().getId();
            SalesAggregate aggregate = aggregateMap.computeIfAbsent(
                    breadId,
                    ignored -> new SalesAggregate(
                            breadId,
                            sale.getBread().getName(),
                            sale.getBread().getPrice(),
                            0,
                            BigDecimal.ZERO,
                            null
                    )
            );

            int quantity = Optional.ofNullable(sale.getQuantity()).orElse(0);
            BigDecimal amount = Optional.ofNullable(sale.getTotalPrice()).orElse(BigDecimal.ZERO);
            LocalDateTime saleDate = Optional.ofNullable(sale.getSaleDate()).orElse(sale.getCreatedAt());

            aggregate.addQuantity(quantity);
            aggregate.addRevenue(amount);
            aggregate.updateLatestSale(saleDate);

            totalQuantity += quantity;
            totalRevenue = totalRevenue.add(amount);

            if (saleDate != null && (latestSale == null || saleDate.isAfter(latestSale))) {
                latestSale = saleDate;
            }
        }

        List<SalesAggregate> sorted = aggregateMap.values().stream()
                .sorted(Comparator.comparingInt(SalesAggregate::totalQuantity).reversed())
                .collect(Collectors.toList());

        boolean hasRecent = latestSale != null && !latestSale.isBefore(thresholdForRecent);

        return new SalesAnalytics(true, hasRecent, latestSale, totalQuantity, totalRevenue, sorted);
    }

    private InventoryAnalytics analyseInventory(List<Inventory> inventories) {
        if (inventories.isEmpty()) {
            return InventoryAnalytics.empty();
        }

        List<Inventory> lowStock = inventories.stream()
                .filter(this::isLowStock)
                .sorted(Comparator
                        .comparingInt((Inventory inv) -> safeQuantity(inv))
                        .thenComparing(inv -> inv.getBread().getName()))
                .collect(Collectors.toList());

        List<Inventory> ampleStock = inventories.stream()
                .filter(this::isAmpleStock)
                .sorted(Comparator
                        .comparingInt((Inventory inv) -> safeQuantity(inv))
                        .reversed()
                        .thenComparing(inv -> inv.getBread().getName()))
                .collect(Collectors.toList());

        List<Inventory> critical = inventories.stream()
                .filter(this::isCriticalStock)
                .sorted(Comparator.comparingInt(this::safeQuantity))
                .collect(Collectors.toList());

        return new InventoryAnalytics(true, inventories, lowStock, ampleStock, critical);
    }

    private WeatherContext buildWeatherContext() {
        try {
            WeatherSummary summary = weatherService.fetchSeoulWeather();
            if (!summary.success()) {
                return WeatherContext.unavailable(summary.summary());
            }
            return WeatherContext.available(summary.summary(), summary.description(), summary.temperature());
        } catch (Exception e) {
            log.warn("날씨 정보를 불러오지 못했습니다.", e);
            return WeatherContext.unavailable("날씨 데이터를 불러오지 못했습니다.");
        }
    }

    private String buildMoodMessage(WeatherContext weatherContext, SalesAnalytics salesAnalytics) {
        if (!weatherContext.available()) {
            if (salesAnalytics.hasSales()) {
                return "날씨 정보를 불러오지 못했습니다. 판매 동향만으로 운영을 이어가세요.";
            }
            return "날씨와 판매 데이터가 제한적입니다. 기본 준비 상태를 유지하세요.";
        }

        String temperatureText = weatherContext.temperatureText();
        String description = Optional.ofNullable(weatherContext.description()).orElse("미확인");

        if (salesAnalytics.hasRecentSales()) {
            return "현재 " + description + " / " + temperatureText + ". 판매 흐름이 안정적으로 이어지고 있습니다.";
        }

        if (salesAnalytics.hasSales()) {
            return "현재 " + description + " / " + temperatureText + ". 판매는 잠시 소강상태입니다.";
        }

        return "현재 " + description + " / " + temperatureText + ". 초기 세팅에 집중하세요.";
    }

    private String determineStockTone(InventoryAnalytics analytics) {
        if (!analytics.criticalStock().isEmpty()) {
            return "즉시 보충 필요한 품목 " + analytics.criticalStock().size() + "개";
        }
        if (!analytics.lowStock().isEmpty()) {
            return "재고가 낮은 품목 " + analytics.lowStock().size() + "개";
        }
        if (!analytics.ampleStock().isEmpty()) {
            return "재고가 넉넉한 품목 " + analytics.ampleStock().size() + "개";
        }
        return "전반적으로 안정적인 재고 수준";
    }

    private boolean isCriticalStock(Inventory inventory) {
        return safeQuantity(inventory) <= 0;
    }

    private boolean isLowStock(Inventory inventory) {
        int quantity = safeQuantity(inventory);
        Integer minLevel = inventory.getMinStockLevel();
        int threshold = minLevel != null && minLevel > 0 ? minLevel : LOW_STOCK_DEFAULT_THRESHOLD;
        return quantity <= threshold;
    }

    private boolean isAmpleStock(Inventory inventory) {
        int quantity = safeQuantity(inventory);
        Integer minLevel = inventory.getMinStockLevel();
        int threshold = minLevel != null && minLevel > 0 ? minLevel : LOW_STOCK_DEFAULT_THRESHOLD;
        return quantity >= threshold * AMPLE_STOCK_MULTIPLIER;
    }

    private int safeQuantity(Inventory inventory) {
        return Optional.ofNullable(inventory.getQuantity()).orElse(0);
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

    private String normalize(String text) {
        return text == null ? "" : text.trim();
    }

    private String formatCurrency(BigDecimal amount) {
        BigDecimal value = amount == null ? BigDecimal.ZERO : amount.setScale(0, RoundingMode.HALF_UP);
        return CURRENCY_FORMAT.format(value) + "원";
    }

    private String formatTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "미확인";
        }
        Duration duration = Duration.between(dateTime, LocalDateTime.now());
        long minutes = Math.max(duration.toMinutes(), 0);
        if (minutes < 1) {
            return "방금";
        }
        if (minutes < 60) {
            return minutes + "분 전";
        }
        long hours = minutes / 60;
        if (hours < 24) {
            return hours + "시간 전";
        }
        long days = hours / 24;
        return days + "일 전";
    }

    private static class SalesAggregate {
        private final Long breadId;
        private final String name;
        private final BigDecimal unitPrice;
        private int totalQuantity;
        private BigDecimal totalRevenue;
        private LocalDateTime latestSaleAt;

        SalesAggregate(Long breadId, String name, BigDecimal unitPrice, int totalQuantity,
                       BigDecimal totalRevenue, LocalDateTime latestSaleAt) {
            this.breadId = breadId;
            this.name = name;
            this.unitPrice = unitPrice;
            this.totalQuantity = totalQuantity;
            this.totalRevenue = totalRevenue;
            this.latestSaleAt = latestSaleAt;
        }

        void addQuantity(int additionalQuantity) {
            this.totalQuantity += additionalQuantity;
        }

        void addRevenue(BigDecimal additionalRevenue) {
            this.totalRevenue = this.totalRevenue.add(additionalRevenue);
        }

        void updateLatestSale(LocalDateTime saleAt) {
            if (saleAt == null) {
                return;
            }
            if (this.latestSaleAt == null || saleAt.isAfter(this.latestSaleAt)) {
                this.latestSaleAt = saleAt;
            }
        }

        public Long breadId() {
            return breadId;
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

        public LocalDateTime latestSaleAt() {
            return latestSaleAt;
        }
    }

    private record SalesAnalytics(boolean hasSales,
                                  boolean hasRecentSales,
                                  LocalDateTime latestSaleAt,
                                  int totalQuantity,
                                  BigDecimal totalRevenue,
                                  List<SalesAggregate> aggregates) {

        static SalesAnalytics empty() {
            return new SalesAnalytics(false, false, null, 0, BigDecimal.ZERO, List.of());
        }

        List<SalesAggregate> topAggregates(int limit) {
            return aggregates.stream().limit(limit).collect(Collectors.toList());
        }
    }

    private record InventoryAnalytics(boolean hasInventory,
                                      List<Inventory> allInventories,
                                      List<Inventory> lowStock,
                                      List<Inventory> ampleStock,
                                      List<Inventory> criticalStock) {

        static InventoryAnalytics empty() {
            return new InventoryAnalytics(false, List.of(), List.of(), List.of(), List.of());
        }

        int totalItems() {
            return allInventories.size();
        }

        List<String> lowStockTopN(int limit) {
            return lowStock.stream().limit(limit)
                    .map(inv -> inv.getBread().getName() + " " + safeQuantityStatic(inv) + "개")
                    .collect(Collectors.toList());
        }

        List<String> ampleStockTopN(int limit) {
            return ampleStock.stream().limit(limit)
                    .map(inv -> inv.getBread().getName() + " " + safeQuantityStatic(inv) + "개")
                    .collect(Collectors.toList());
        }

        private static int safeQuantityStatic(Inventory inventory) {
            return Optional.ofNullable(inventory.getQuantity()).orElse(0);
        }
    }

    private record WeatherContext(boolean available,
                                  String summary,
                                  String description,
                                  Double temperature) {

        static WeatherContext available(String summary, String description, Double temperature) {
            return new WeatherContext(true, summary, description, temperature);
        }

        static WeatherContext unavailable(String reason) {
            return new WeatherContext(false, reason, null, null);
        }

        String temperatureText() {
            return temperature == null ? "미확인" : String.format("%.1f°C", temperature);
        }
    }

    private record GeminiResult(String mood, String brief, String inventory, String strategy) {
        static GeminiResult empty() {
            return new GeminiResult("", "", "", "");
        }
    }
}
