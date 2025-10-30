package com.ddbb.service.payment;

import com.ddbb.dto.payment.PaymentCompleteRequest;
import com.ddbb.dto.payment.PaymentCompleteResponse;
import com.ddbb.dto.payment.PaymentItem;
import com.ddbb.entity.management.Bread;
import com.ddbb.entity.management.Sales;
import com.ddbb.repository.management.BreadRepository;
import com.ddbb.repository.management.SalesRepository;
import com.ddbb.service.management.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 결제 처리 서비스
 * - 포트원 결제 게이트웨이 연동
 * - 결제 완료 시 자동으로 재고 차감 및 매출 기록
 */
@Slf4j
@Service
public class PaymentService {
    
    private final SalesRepository salesRepository;
    private final BreadRepository breadRepository;
    private final InventoryService inventoryService;
    
    @Value("${portone.api.secret}")
    private String apiSecret;
    
    @Value("${portone.api.url}")
    private String apiUrl;
    
    private final WebClient webClient;
    
    // WebClient 초기화를 위한 생성자
    public PaymentService(SalesRepository salesRepository, 
                         BreadRepository breadRepository, 
                         InventoryService inventoryService) {
        this.salesRepository = salesRepository;
        this.breadRepository = breadRepository;
        this.inventoryService = inventoryService;
        this.webClient = WebClient.builder().build();
    }
    
    /**
     * 결제 완료 처리
     * 1. 재고 차감
     * 2. 매출 기록
     * 
     * @param request 결제 완료 요청 (빵 목록 포함)
     * @return 결제 처리 결과
     */
    @Transactional
    public PaymentCompleteResponse processPaymentComplete(PaymentCompleteRequest request) {
        log.info("결제 완료 처리 시작 - Payment ID: {}", request.getPaymentId());
        
        List<Long> salesIds = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;
        int totalQuantity = 0;
        
        try {
            // 각 빵에 대해 재고 차감 및 매출 기록
            for (PaymentItem item : request.getItems()) {
                // 1. 빵 정보 조회
                Bread bread = breadRepository.findById(item.getBreadId())
                        .orElseThrow(() -> new RuntimeException(
                                "빵 정보를 찾을 수 없습니다. Bread ID: " + item.getBreadId()));
                
                // 2. 재고 차감
                log.info("재고 차감 - Bread: {}, Quantity: {}", bread.getName(), item.getQuantity());
                inventoryService.decreaseStock(item.getBreadId(), item.getQuantity());
                
                // 3. 매출 기록 생성
                BigDecimal itemTotalPrice = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
                
                Sales sales = Sales.builder()
                        .bread(bread)
                        .quantity(item.getQuantity())
                        .totalPrice(itemTotalPrice)
                        .saleDate(LocalDateTime.now())
                        .build();
                
                Sales savedSales = salesRepository.save(sales);
                salesIds.add(savedSales.getId());
                
                // 4. 합계 계산
                totalAmount = totalAmount.add(itemTotalPrice);
                totalQuantity += item.getQuantity();
                
                log.info("매출 기록 완료 - Sales ID: {}, Amount: {}", savedSales.getId(), itemTotalPrice);
            }
            
            log.info("결제 완료 처리 성공 - Total Amount: {}, Total Quantity: {}", totalAmount, totalQuantity);
            
            return PaymentCompleteResponse.builder()
                    .success(true)
                    .paymentId(request.getPaymentId())
                    .salesIds(salesIds)
                    .totalAmount(totalAmount)
                    .totalQuantity(totalQuantity)
                    .processedAt(LocalDateTime.now())
                    .message("결제가 성공적으로 처리되었습니다.")
                    .build();
                    
        } catch (Exception e) {
            log.error("결제 처리 중 오류 발생 - Payment ID: {}", request.getPaymentId(), e);
            throw new RuntimeException("결제 처리 실패: " + e.getMessage(), e);
        }
    }
    
    /**
     * 포트원 결제 정보 조회
     * 외부 결제 게이트웨이(PortOne)에서 결제 정보를 조회합니다.
     * 
     * @param paymentId 포트원 결제 ID
     * @return 결제 정보
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> getPaymentInfo(String paymentId) {
        log.info("포트원 결제 조회: paymentId={}", paymentId);
        
        try {
            Map<String, Object> paymentData = webClient.get()
                    .uri(apiUrl + "/payments/" + paymentId)
                    .header(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            
            log.info("결제 조회 성공: {}", paymentData);
            return paymentData;
            
        } catch (Exception e) {
            log.error("결제 조회 실패: paymentId={}", paymentId, e);
            throw new RuntimeException("결제 조회 실패: " + e.getMessage());
        }
    }
    
    /**
     * 결제 취소 처리 (향후 구현)
     * 재고 복구 및 매출 기록 취소
     */
    @Transactional
    public void processCancelPayment(String paymentId) {
        // TODO: 결제 취소 시 재고 복구 로직 구현
        log.info("결제 취소 처리 - Payment ID: {}", paymentId);
        throw new UnsupportedOperationException("결제 취소 기능은 아직 구현되지 않았습니다.");
    }
}

