package com.ddbb.controller.aidashboard;

import com.ddbb.dto.aidashboard.DashboardChartDto;
import com.ddbb.service.aidashboard.WeatherService;
import com.ddbb.service.aidashboard.WeatherService.WeatherSummary;
import com.ddbb.service.aidashboard.DashboardAIService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final WeatherService weatherService;
    private final DashboardAIService dashboardAIService;

    @GetMapping("/insights")
    public Map<String, Object> getDashboardInsights() {
        log.info("AI 대시보드 인사이트 요청");

        try {
            // 실시간 데이터 기반 AI 인사이트 생성
            return dashboardAIService.generateDashboardInsights();
        } catch (Exception e) {
            log.error("AI 대시보드 생성 중 오류 발생", e);

            // 폴백: 기본 메시지
            return Map.of(
                    "mood", "데이터를 불러오는 중 오류가 발생했습니다.",
                    "brief", "판매 데이터를 확인할 수 없습니다.",
                    "insight", "재고 데이터를 확인할 수 없습니다.",
                    "strategy", "잠시 후 다시 시도해주세요."
            );
        }
    }

    /**
     * 차트 데이터 조회 (프론트엔드 그래프용)
     * GET /api/dashboard/charts
     */
    @GetMapping("/charts")
    public ResponseEntity<DashboardChartDto> getChartData() {
        log.info("차트 데이터 요청");

        try {
            DashboardChartDto chartData = dashboardAIService.generateChartData();
            return ResponseEntity.ok(chartData);
        } catch (Exception e) {
            log.error("차트 데이터 생성 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
