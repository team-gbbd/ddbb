package com.ddbb.dto.management;

import com.ddbb.entity.management.Sales;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesResponse {
    private Long id;
    private Long breadId;
    private String breadName;
    private Integer quantity;
    private BigDecimal totalPrice;
    private LocalDateTime saleDate;
    
    public static SalesResponse from(Sales sales) {
        return SalesResponse.builder()
                .id(sales.getId())
                .breadId(sales.getBread().getId())
                .breadName(sales.getBread().getName())
                .quantity(sales.getQuantity())
                .totalPrice(sales.getTotalPrice())
                .saleDate(sales.getSaleDate())
                .build();
    }
}

