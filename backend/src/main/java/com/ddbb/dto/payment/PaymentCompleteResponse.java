package com.ddbb.dto.payment;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 결제 완료 응답 DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompleteResponse {
    
    /**
     * 결제 성공 여부
     */
    private boolean success;
    
    /**
     * 결제 ID
     */
    private String paymentId;
    
    /**
     * 처리된 매출 ID 목록
     */
    private List<Long> salesIds;
    
    /**
     * 총 매출액
     */
    private BigDecimal totalAmount;
    
    /**
     * 총 판매 수량
     */
    private Integer totalQuantity;
    
    /**
     * 처리 시간
     */
    private LocalDateTime processedAt;
    
    /**
     * 메시지
     */
    private String message;
}

