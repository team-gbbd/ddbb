package com.ddbb.controller.management;

import com.ddbb.dto.management.AIAnalysisRequest;
import com.ddbb.dto.management.AIAnalysisResponse;
import com.ddbb.service.management.AIAnalysisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/ai-analysis")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AIAnalysisController {
    
    private final AIAnalysisService aiAnalysisService;
    
    /**
     * AI 재고 분석 수행
     * POST /api/ai-analysis/analyze
     */
    @PostMapping("/analyze")
    public ResponseEntity<AIAnalysisResponse> performAnalysis(@RequestBody AIAnalysisRequest request) {
        log.info("AI 분석 요청: {}", request);
        
        AIAnalysisResponse response = aiAnalysisService.performAnalysis(request);
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * 빠른 분석 - 최근 30일 데이터 기반 종합 분석
     * GET /api/ai-analysis/quick-analysis
     *
     * 주의: 오늘 날짜는 제외하고 어제까지의 완전한 데이터만 사용
     * (오늘 데이터는 진행 중이라 불완전하여 예측 왜곡 방지)
     */
    @GetMapping("/quick-analysis")
    public ResponseEntity<AIAnalysisResponse> quickAnalysis() {
        log.info("빠른 AI 분석 요청");

        // 어제 날짜까지만 분석 (오늘은 데이터가 불완전하므로 제외)
        java.time.LocalDate yesterday = java.time.LocalDate.now().minusDays(1);

        AIAnalysisRequest request = AIAnalysisRequest.builder()
                .startDate(yesterday.minusDays(30))
                .endDate(yesterday)
                .analysisType("COMPREHENSIVE")
                .build();

        AIAnalysisResponse response = aiAnalysisService.performAnalysis(request);

        return ResponseEntity.ok(response);
    }
}

