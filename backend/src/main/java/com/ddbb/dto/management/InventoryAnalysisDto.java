package com.ddbb.dto.management;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryAnalysisDto {
    private Long breadId;
    private String breadName;
    private Integer currentStock;
    private Integer minStockLevel;
    private Integer totalSold;
    private Double averageDailySales;
    private Integer daysOfStock;
}

