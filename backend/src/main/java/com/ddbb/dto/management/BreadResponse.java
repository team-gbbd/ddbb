package com.ddbb.dto.management;

import com.ddbb.entity.management.Bread;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreadResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private String description;
    
    public static BreadResponse from(Bread bread) {
        return BreadResponse.builder()
                .id(bread.getId())
                .name(bread.getName())
                .price(bread.getPrice())
                .description(bread.getDescription())
                .build();
    }
}

