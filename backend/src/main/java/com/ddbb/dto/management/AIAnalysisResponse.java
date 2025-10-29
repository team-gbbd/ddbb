package com.ddbb.dto.management;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AIAnalysisResponse {
    
    /**
     * 분석 결과 요약
     */
    private String summary;
    
    /**
     * 상세 분석 내용
     */
    private String detailedAnalysis;
    
    /**
     * 추천 사항들
     */
    private List<String> recommendations;
    
    /**
     * 경고 사항들
     */
    private List<String> warnings;
    
    /**
     * 예측 데이터 (JSON 형식)
     */
    private String predictionData;
    
    /**
     * 그래프용 차트 데이터
     */
    private ChartDataDto chartData;
    
    /**
     * 분석 타입
     */
    private String analysisType;
    
    /**
     * 생성 시각
     */
    private String generatedAt;
}

