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
     */
    @GetMapping("/quick-analysis")
    public ResponseEntity<AIAnalysisResponse> quickAnalysis() {
        log.info("빠른 AI 분석 요청");
        
        AIAnalysisRequest request = AIAnalysisRequest.builder()
                .startDate(java.time.LocalDate.now().minusDays(30))
                .endDate(java.time.LocalDate.now())
                .analysisType("COMPREHENSIVE")
                .build();
        
        AIAnalysisResponse response = aiAnalysisService.performAnalysis(request);
        
        return ResponseEntity.ok(response);
    }
}

