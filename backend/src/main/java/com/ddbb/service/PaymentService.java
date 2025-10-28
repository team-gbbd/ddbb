package com.ddbb.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
public class PaymentService {

    @Value("${portone.api.secret}")
    private String apiSecret;

    @Value("${portone.api.url}")
    private String apiUrl;

    private final WebClient webClient;

    public PaymentService(){
        this.webClient =WebClient.builder().build();
    }

    //포트원 결제 정보 조회 로직
    public Map<String, Object> getPaymentInfo(String paymentId){
        log.info("포트원 결제 조회: paymentId={}",paymentId);

        try{
            Map<String, Object> paymentData = webClient.get()
                    .uri(apiUrl + "/payments/" + paymentId)
                    .header(HttpHeaders.AUTHORIZATION, "PortOne " + apiSecret)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
            log.info("결제 조회 성공: {}", paymentData);
            return  paymentData;
        }
        catch (Exception e){
            log.error("결제 조회 실패: paymentId={}", paymentId, e);
            throw new RuntimeException("결제 조회 실패: " + e.getMessage());
        }

    }
}
