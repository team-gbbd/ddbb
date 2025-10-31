package com.ddbb.service.management;

import com.ddbb.dto.management.*;
import com.ddbb.entity.management.Bread;
import com.ddbb.entity.management.Sales;
import com.ddbb.repository.management.BreadRepository;
import com.ddbb.repository.management.SalesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SalesService {
    
    private final SalesRepository salesRepository;
    private final BreadRepository breadRepository;
    private final InventoryService inventoryService;
    
    /**
     * 판매 기록 생성
     */
    @Transactional
    public SalesResponse createSale(SalesCreateRequest request) {
        Bread bread = breadRepository.findById(request.getBreadId())
                .orElseThrow(() -> new RuntimeException("빵 정보를 찾을 수 없습니다. ID: " + request.getBreadId()));
        
        // 재고 확인 및 감소
        inventoryService.decreaseStock(request.getBreadId(), request.getQuantity());
        
        // 판매 기록 생성
        BigDecimal totalPrice = bread.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));
        
        Sales sales = Sales.builder()
                .bread(bread)
                .quantity(request.getQuantity())
                .totalPrice(totalPrice)
                .saleDate(LocalDateTime.now())
                .build();
        
        Sales savedSales = salesRepository.save(sales);
        return SalesResponse.from(savedSales);
    }
    
    /**
     * 기간별 판매 내역 조회
     */
    public List<SalesResponse> getSalesByPeriod(LocalDateTime startDate, LocalDateTime endDate) {
        return salesRepository.findSalesInPeriod(startDate, endDate).stream()
                .map(SalesResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 특정 빵의 기간별 판매 내역 조회
     */
    public List<SalesResponse> getSalesByBreadAndPeriod(Long breadId, LocalDateTime startDate, LocalDateTime endDate) {
        return salesRepository.findByBreadIdAndSaleDateBetween(breadId, startDate, endDate).stream()
                .map(SalesResponse::from)
                .collect(Collectors.toList());
    }
    
    /**
     * 기간별 판매 요약 (빵별 집계) - 그래프용
     */
    public List<SalesSummaryResponse> getSalesSummary(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = salesRepository.findSalesSummaryByPeriod(startDate, endDate);
        
        List<SalesSummaryResponse> summaries = new ArrayList<>();
        for (Object[] result : results) {
            summaries.add(SalesSummaryResponse.builder()
                    .breadId(((Number) result[0]).longValue())
                    .breadName((String) result[1])
                    .totalQuantity(((Number) result[2]).longValue())
                    .totalRevenue((BigDecimal) result[3])
                    .build());
        }
        
        return summaries;
    }
    
    /**
     * 기간별 판매 통계 (총계 + 빵별 집계) - 통계 페이지용
     */
    public SalesStatisticsResponse getSalesStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Object[]> results = salesRepository.findSalesSummaryByPeriod(startDate, endDate);
        
        if (results.isEmpty()) {
            return SalesStatisticsResponse.builder()
                    .totalSales(BigDecimal.ZERO)
                    .totalQuantity(0L)
                    .averagePrice(BigDecimal.ZERO)
                    .breadSales(new ArrayList<>())
                    .build();
        }
        
        // 빵별 판매 정보 생성
        List<SalesStatisticsResponse.BreadSalesInfo> breadSales = new ArrayList<>();
        BigDecimal totalSales = BigDecimal.ZERO;
        long totalQuantity = 0;
        
        for (Object[] result : results) {
            Long breadId = ((Number) result[0]).longValue();
            String breadName = (String) result[1];
            Long quantity = ((Number) result[2]).longValue();
            BigDecimal revenue = (BigDecimal) result[3];
            
            breadSales.add(SalesStatisticsResponse.BreadSalesInfo.builder()
                    .breadId(breadId)
                    .breadName(breadName)
                    .totalQuantity(quantity)
                    .totalSales(revenue)
                    .build());
            
            totalSales = totalSales.add(revenue);
            totalQuantity += quantity;
        }
        
        // 평균 단가 계산
        BigDecimal averagePrice = totalQuantity > 0 
                ? totalSales.divide(BigDecimal.valueOf(totalQuantity), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;
        
        return SalesStatisticsResponse.builder()
                .totalSales(totalSales)
                .totalQuantity(totalQuantity)
                .averagePrice(averagePrice)
                .breadSales(breadSales)
                .build();
    }
    
    /**
     * 일별 판매 통계 - 그래프용
     */
    public List<DailySalesResponse> getDailySalesStatistics(LocalDateTime startDate, LocalDateTime endDate) {
        List<Sales> sales = salesRepository.findSalesInPeriod(startDate, endDate);
        
        // 날짜별로 그룹핑
        Map<LocalDate, List<Sales>> salesByDate = sales.stream()
                .collect(Collectors.groupingBy(s -> s.getSaleDate().toLocalDate()));
        
        // 각 날짜별 통계 계산
        List<DailySalesResponse> dailyStats = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Sales>> entry : salesByDate.entrySet()) {
            LocalDate date = entry.getKey();
            List<Sales> daySales = entry.getValue();
            
            int totalQuantity = daySales.stream()
                    .mapToInt(Sales::getQuantity)
                    .sum();
            
            BigDecimal totalRevenue = daySales.stream()
                    .map(Sales::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            
            dailyStats.add(DailySalesResponse.builder()
                    .date(date)
                    .totalQuantity(totalQuantity)
                    .totalRevenue(totalRevenue)
                    .build());
        }
        
        // 날짜순 정렬
        dailyStats.sort((a, b) -> a.getDate().compareTo(b.getDate()));
        
        return dailyStats;
    }
    
    /**
     * 오늘 판매 통계
     */
    public DailySalesResponse getTodaySalesStatistics() {
        LocalDateTime startOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        
        List<DailySalesResponse> dailyStats = getDailySalesStatistics(startOfDay, endOfDay);
        
        if (dailyStats.isEmpty()) {
            return DailySalesResponse.builder()
                    .date(LocalDate.now())
                    .totalQuantity(0)
                    .totalRevenue(BigDecimal.ZERO)
                    .build();
        }
        
        return dailyStats.get(0);
    }
    
    /**
     * 최근 7일 판매 통계
     */
    public List<DailySalesResponse> getWeeklySalesStatistics() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);
        
        return getDailySalesStatistics(startDate, endDate);
    }
    
    /**
     * 최근 30일 판매 통계
     */
    public List<DailySalesResponse> getMonthlySalesStatistics() {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(30);
        
        return getDailySalesStatistics(startDate, endDate);
    }
}

