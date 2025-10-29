package com.ddbb.dto.management;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChartDataDto {
    
    /**
     * 과거 판매 데이터 - 날짜별
     * key: 날짜 (YYYY-MM-DD), value: 판매량
     */
    private Map<String, Integer> historicalSales;
    
    /**
     * 예측 판매 데이터 - 날짜별
     * key: 날짜 (YYYY-MM-DD), value: 예측 판매량
     */
    private Map<String, Integer> predictedSales;
    
    /**
     * 과거 수익 데이터 - 날짜별
     * key: 날짜 (YYYY-MM-DD), value: 수익
     */
    private Map<String, Double> historicalRevenue;
    
    /**
     * 예측 수익 데이터 - 날짜별
     * key: 날짜 (YYYY-MM-DD), value: 예측 수익
     */
    private Map<String, Double> predictedRevenue;
    
    /**
     * 빵별 과거 판매량
     * key: 빵 이름, value: 총 판매량
     */
    private Map<String, Integer> breadHistoricalSales;
    
    /**
     * 빵별 예측 판매량
     * key: 빵 이름, value: 예측 판매량
     */
    private Map<String, Integer> breadPredictedSales;
    
    /**
     * 성장률 데이터 (%)
     */
    private List<GrowthRateDto> growthRates;
    
    /**
     * 예측 성장률 데이터 (%)
     */
    private List<GrowthRateDto> predictedGrowthRates;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GrowthRateDto {
        private String date;
        private Double growthRate;
    }
}

