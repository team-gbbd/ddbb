package com.ddbb.controller.management;

import com.ddbb.dto.management.*;
import com.ddbb.service.management.SalesService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SalesController {
    
    private final SalesService salesService;
    
    /**
     * 판매 기록 생성
     * POST /api/sales
     */
    @PostMapping
    public ResponseEntity<SalesResponse> createSale(@Valid @RequestBody SalesCreateRequest request) {
        SalesResponse sale = salesService.createSale(request);
        return ResponseEntity.ok(sale);
    }
    
    /**
     * 기간별 판매 내역 조회
     * GET /api/sales?startDate=...&endDate=...
     */
    @GetMapping
    public ResponseEntity<List<SalesResponse>> getSalesByPeriod(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<SalesResponse> sales = salesService.getSalesByPeriod(startDate, endDate);
        return ResponseEntity.ok(sales);
    }
    
    /**
     * 특정 빵의 기간별 판매 내역 조회
     * GET /api/sales/bread/{breadId}?startDate=...&endDate=...
     */
    @GetMapping("/bread/{breadId}")
    public ResponseEntity<List<SalesResponse>> getSalesByBreadAndPeriod(
            @PathVariable Long breadId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        List<SalesResponse> sales = salesService.getSalesByBreadAndPeriod(breadId, startDate, endDate);
        return ResponseEntity.ok(sales);
    }
    
    /**
     * 기간별 판매 요약 (빵별 집계) - 그래프용
     * GET /api/sales/summary?startDate=...&endDate=...
     */
    @GetMapping("/summary")
    public ResponseEntity<SalesStatisticsResponse> getSalesStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        SalesStatisticsResponse statistics = salesService.getSalesStatistics(startDate, endDate);
        return ResponseEntity.ok(statistics);
    }
    
    /**
     * 일별 판매 통계 - 그래프용
     * GET /api/sales/daily?date=...
     */
    @GetMapping("/daily")
    public ResponseEntity<DailySalesResponse> getDailySalesStatistics(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String date) {
        java.time.LocalDate localDate = java.time.LocalDate.parse(date);
        LocalDateTime startDate = localDate.atStartOfDay();
        LocalDateTime endDate = localDate.atTime(23, 59, 59);
        
        List<DailySalesResponse> dailyStats = salesService.getDailySalesStatistics(startDate, endDate);
        
        // 해당 날짜의 데이터가 없으면 빈 데이터 반환
        if (dailyStats.isEmpty()) {
            return ResponseEntity.ok(DailySalesResponse.builder()
                    .date(localDate)
                    .totalQuantity(0)
                    .totalRevenue(BigDecimal.ZERO)
                    .build());
        }
        
        return ResponseEntity.ok(dailyStats.get(0));
    }
    
    /**
     * 오늘 판매 통계
     * GET /api/sales/statistics/today
     */
    @GetMapping("/statistics/today")
    public ResponseEntity<DailySalesResponse> getTodaySalesStatistics() {
        DailySalesResponse todayStats = salesService.getTodaySalesStatistics();
        return ResponseEntity.ok(todayStats);
    }
    
    /**
     * 최근 7일 판매 통계 - 그래프용
     * GET /api/sales/statistics/weekly
     */
    @GetMapping("/statistics/weekly")
    public ResponseEntity<List<DailySalesResponse>> getWeeklySalesStatistics() {
        List<DailySalesResponse> weeklyStats = salesService.getWeeklySalesStatistics();
        return ResponseEntity.ok(weeklyStats);
    }
    
    /**
     * 최근 30일 판매 통계 - 그래프용
     * GET /api/sales/statistics/monthly
     */
    @GetMapping("/statistics/monthly")
    public ResponseEntity<List<DailySalesResponse>> getMonthlySalesStatistics() {
        List<DailySalesResponse> monthlyStats = salesService.getMonthlySalesStatistics();
        return ResponseEntity.ok(monthlyStats);
    }
}

