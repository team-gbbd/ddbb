package com.ddbb.controller.payment;

import com.ddbb.dto.payment.PaymentCompleteRequest;
import com.ddbb.dto.payment.PaymentCompleteResponse;
import com.ddbb.service.payment.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 결제 처리 컨트롤러
 * 
 * 다른 팀원이 만든 결제 API에서 결제가 완료되면
 * 이 엔드포인트를 호출하여 재고 차감 및 매출 기록을 처리합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PaymentController {
    
    private final PaymentService paymentService;
    
    /**
     * 결제 완료 처리
     * 
     * POST /api/payment/complete
     * 
     * 요청 예시:
     * {
     *   "paymentId": "payment-1234567890",
     *   "items": [
     *     {
     *       "breadId": 1,
     *       "quantity": 2,
     *       "price": 4300
     *     },
     *     {
     *       "breadId": 2,
     *       "quantity": 1,
     *       "price": 4700
     *     }
     *   ],
     *   "customerName": "홍길동",
     *   "customerPhone": "010-1234-5678"
     * }
     * 
     * @param request 결제 완료 요청 데이터
     * @return 결제 처리 결과
     */
    @PostMapping("/complete")
    public ResponseEntity<PaymentCompleteResponse> completePayment(
            @Valid @RequestBody PaymentCompleteRequest request) {
        
        log.info("결제 완료 요청 수신 - Payment ID: {}, Items: {}", 
                request.getPaymentId(), request.getItems().size());
        
        try {
            PaymentCompleteResponse response = paymentService.processPaymentComplete(request);
            log.info("결제 완료 처리 성공 - Payment ID: {}", request.getPaymentId());
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("결제 완료 처리 실패 - Payment ID: {}", request.getPaymentId(), e);
            
            PaymentCompleteResponse errorResponse = PaymentCompleteResponse.builder()
                    .success(false)
                    .paymentId(request.getPaymentId())
                    .message("결제 처리 실패: " + e.getMessage())
                    .build();
            
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
    
    /**
     * 포트원 결제 정보 조회
     * 
     * GET /api/payment/info/{paymentId}
     * 
     * @param paymentId 조회할 포트원 결제 ID
     * @return 결제 정보
     */
    @GetMapping("/info/{paymentId}")
    public ResponseEntity<?> getPaymentInfo(@PathVariable String paymentId) {
        log.info("포트원 결제 정보 조회 요청 - Payment ID: {}", paymentId);
        
        try {
            var paymentData = paymentService.getPaymentInfo(paymentId);
            return ResponseEntity.ok(paymentData);
            
        } catch (Exception e) {
            log.error("결제 정보 조회 실패 - Payment ID: {}", paymentId, e);
            return ResponseEntity.badRequest().body("결제 정보 조회 실패: " + e.getMessage());
        }
    }
    
    /**
     * 결제 취소 처리 (향후 구현)
     * 
     * POST /api/payment/cancel/{paymentId}
     * 
     * @param paymentId 취소할 결제 ID
     * @return 취소 처리 결과
     */
    @PostMapping("/cancel/{paymentId}")
    public ResponseEntity<String> cancelPayment(@PathVariable String paymentId) {
        log.info("결제 취소 요청 - Payment ID: {}", paymentId);
        
        try {
            paymentService.processCancelPayment(paymentId);
            return ResponseEntity.ok("결제가 취소되었습니다.");
            
        } catch (UnsupportedOperationException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}

