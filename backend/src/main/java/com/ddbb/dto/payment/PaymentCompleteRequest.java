package com.ddbb.dto.payment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 결제 완료 요청 DTO
 * 다른 팀원이 만든 결제 API에서 이 형식으로 데이터를 보내면 됩니다
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCompleteRequest {
    
    /**
     * 포트원 결제 ID
     */
    @NotBlank(message = "결제 ID는 필수입니다")
    private String paymentId;
    
    /**
     * 판매된 빵 목록
     */
    @NotEmpty(message = "판매 항목은 최소 1개 이상이어야 합니다")
    @Valid
    private List<PaymentItem> items;
    
    /**
     * 고객 정보 (선택사항)
     */
    private String customerName;
    private String customerPhone;
}

