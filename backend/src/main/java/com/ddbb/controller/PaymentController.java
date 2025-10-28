package com.ddbb.controller;

import com.ddbb.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/complete")
    public Map<String, Object> completePayment(@RequestBody Map<String, String> request){
        String paymentId = request.get("paymentId");

        log.info("결제 요청 : paymentId={}", paymentId);

        Map<String, Object> paymentData = paymentService.getPaymentInfo((paymentId));

        log.info("결제 처리 완료: paymentId={}", paymentId);

        return  paymentData;


    }
}
