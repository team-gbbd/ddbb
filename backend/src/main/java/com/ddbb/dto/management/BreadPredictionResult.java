package com.ddbb.dto.management;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * LangChain4j 구조화된 출력을 위한 빵별 판매 예측 결과 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BreadPredictionResult {
    
    /**
     * 빵 종류별 예측 판매량 (7일 기준)
     * 예: {"크로와상": 150, "바게트": 200}
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

