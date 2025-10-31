package com.ddbb.dto.management;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryUpdateRequest {
    
    @NotNull(message = "재고 수량은 필수입니다")
    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다")
    private Integer quantity;
    
    @Min(value = 0, message = "최소 재고 수준은 0 이상이어야 합니다")
    private Integer minStockLevel;
}

