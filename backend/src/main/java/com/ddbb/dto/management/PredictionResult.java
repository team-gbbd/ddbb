package com.ddbb.dto.management;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * LangChain4j 구조화된 출력을 위한 예측 결과 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PredictionResult {
    
    /**
     * 날짜별 예측 판매량
     * 예: {"2025-11-01": 85, "2025-11-02": 90}
     */
    private Map<String, Integer> predictions;
    
    /**
     * 예측에 대한 신뢰도 (0.0 ~ 1.0)
     */
    private Double confidence;
    
    /**
     * 예측 근거
     */
    private String reasoning;
}

