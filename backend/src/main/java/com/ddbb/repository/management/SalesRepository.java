package com.ddbb.repository.management;

import com.ddbb.entity.management.Sales;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SalesRepository extends JpaRepository<Sales, Long> {
    
    List<Sales> findBySaleDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    List<Sales> findByBreadIdAndSaleDateBetween(Long breadId, LocalDateTime startDate, LocalDateTime endDate);
    
    List<Sales> findByBread_IdAndSaleDateBetween(Long breadId, LocalDateTime startDate, LocalDateTime endDate);
    
    @Query("SELECT s FROM Sales s WHERE s.saleDate >= :startDate AND s.saleDate < :endDate ORDER BY s.saleDate")
    List<Sales> findSalesInPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT s.bread.id as breadId, s.bread.name as breadName, SUM(s.quantity) as totalQuantity, SUM(s.totalPrice) as totalRevenue " +
           "FROM Sales s WHERE s.saleDate >= :startDate AND s.saleDate < :endDate " +
           "GROUP BY s.bread.id, s.bread.name ORDER BY totalQuantity DESC")
    List<Object[]> findSalesSummaryByPeriod(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}

