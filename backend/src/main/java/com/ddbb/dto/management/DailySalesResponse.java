package com.ddbb.dto.management;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DailySalesResponse {
    private LocalDate date;
    private Integer totalQuantity;
    private BigDecimal totalRevenue;
}

