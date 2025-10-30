package com.ddbb.dto.management;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisRequest {
    
    /**
     * 분석 시작일
     */
    private LocalDate startDate;
    
    /**
     * 분석 종료일
     */
    private LocalDate endDate;
    
    /**
     * 분석 타입
     * SALES_PREDICTION: 판매량 예측
     * INVENTORY_RECOMMENDATION: 재고 수준 추천
     * OVERSTOCK_WARNING: 과잉 재고 경고
     * SHORTAGE_WARNING: 재고 부족 경고
     * COMPREHENSIVE: 종합 분석
     */
    private String analysisType;
}

