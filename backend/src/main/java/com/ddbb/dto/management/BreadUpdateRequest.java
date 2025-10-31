package com.ddbb.dto.management;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BreadUpdateRequest {
    
    @NotBlank(message = "빵 이름은 필수입니다")
    private String name;
    
    @Min(value = 0, message = "가격은 0 이상이어야 합니다")
    private Double price;
    
    private String description;
}

