package com.ddbb.dto.management;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesStatisticsResponse {
    private BigDecimal totalSales;           // 총 매출액
    private Long totalQuantity;              // 총 판매량
    private BigDecimal averagePrice;         // 평균 단가
    private List<BreadSalesInfo> breadSales; // 빵별 판매 정보
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class BreadSalesInfo {
        private Long breadId;
        private String breadName;
        private Long totalQuantity;
        private BigDecimal totalSales;
    }
}

