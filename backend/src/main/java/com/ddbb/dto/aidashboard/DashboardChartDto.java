package com.ddbb.dto.aidashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * AI 대시보드 차트 데이터 DTO
 * 프론트엔드에서 그래프 표시를 위한 데이터 제공
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardChartDto {

    /**
     * 최근 7일 일별 총 판매량
     * Key: 날짜 (YYYY-MM-DD), Value: 판매량
     * 예: {"2025-01-25": 150, "2025-01-26": 180, ...}
     */
    private Map<String, Integer> last7DaysSales;

    /**
     * 최근 7일 일별 총 매출
     * Key: 날짜 (YYYY-MM-DD), Value: 매출액 (원)
     * 예: {"2025-01-25": 250000, "2025-01-26": 320000, ...}
     */
    private Map<String, Double> last7DaysRevenue;

    /**
     * 주간 빵별 판매량 순위 (TOP 5)
     * Key: 빵 이름, Value: 판매량
     * 예: {"소금버터롤": 210, "크라상": 150, "쿠키": 120, ...}
     */
    private Map<String, Integer> breadSalesRanking;

    /**
     * 주간 빵별 매출 순위 (TOP 5)
     * Key: 빵 이름, Value: 매출액 (원)
     * 예: {"소금버터롤": 630000, "크라상": 450000, ...}
     */
    private Map<String, Double> breadRevenueRanking;

    /**
     * 트렌드 신뢰도 (0.0 ~ 1.0)
     * 변동계수(CV) 기반 통계적 신뢰도
     * - 0.95 이상: 매우 높음 (CV < 15%)
     * - 0.85 이상: 높음 (CV < 25%)
     * - 0.75 이상: 보통 (CV < 35%)
     * - 0.75 미만: 낮음 (CV >= 35%)
     */
    private Double confidence;

    /**
     * 전체 매출 트렌드 방향
     * "상승세", "하락세", "안정적"
     */
    private String trendDirection;

    /**
     * 트렌드 변화율 (%)
     * 어제 대비 주간 평균 증감률
     * 예: 15.5 (15.5% 증가), -8.2 (8.2% 감소)
     */
    private Double trendChangePercent;
}
