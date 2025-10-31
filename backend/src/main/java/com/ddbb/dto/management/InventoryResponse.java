package com.ddbb.dto.management;

import com.ddbb.entity.management.Inventory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryResponse {
    private Long id;
    private Long breadId;
    private String breadName;
    private Double breadPrice;
    private String breadDescription;
    private Integer quantity;
    private Integer minStockLevel;
    private Boolean isLowStock;
    private LocalDateTime lastRestockedAt;
    private LocalDateTime updatedAt;
    
    public static InventoryResponse from(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .breadId(inventory.getBread().getId())
                .breadName(inventory.getBread().getName())
                .breadPrice(inventory.getBread().getPrice() != null ? inventory.getBread().getPrice().doubleValue() : 0.0)
                .breadDescription(inventory.getBread().getDescription())
                .quantity(inventory.getQuantity())
                .minStockLevel(inventory.getMinStockLevel())
                .isLowStock(inventory.getMinStockLevel() != null && inventory.getQuantity() <= inventory.getMinStockLevel())
                .lastRestockedAt(inventory.getLastRestockedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}

