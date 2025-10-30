package com.ddbb.dto.management;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesSummaryResponse {
    private Long breadId;
    private String breadName;
    private Long totalQuantity;
    private BigDecimal totalRevenue;
}

