package com.ddbb.dto.payment;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 결제 항목 (개별 빵)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentItem {
    
    @NotNull(message = "빵 ID는 필수입니다")
    private Long breadId;
    
    @NotNull(message = "수량은 필수입니다")
    @Min(value = 1, message = "수량은 1개 이상이어야 합니다")
    private Integer quantity;
    
    @NotNull(message = "가격은 필수입니다")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다")
    private BigDecimal price;
}

