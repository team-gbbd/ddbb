package com.ddbb.dto.management;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BreadCreateRequest {
    
    @NotBlank(message = "빵 이름은 필수입니다")
    private String name;
    
    @NotNull(message = "가격은 필수입니다")
    @DecimalMin(value = "0.0", inclusive = false, message = "가격은 0보다 커야 합니다")
    private BigDecimal price;
    
    @NotNull(message = "초기 재고 수량은 필수입니다")
    @Min(value = 0, message = "재고 수량은 0 이상이어야 합니다")
    private Integer initialStock;
    
    @NotNull(message = "최소 재고 수준은 필수입니다")
    @Min(value = 0, message = "최소 재고 수준은 0 이상이어야 합니다")
    private Integer minStockLevel;
    
    private String description;
}

