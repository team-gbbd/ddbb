package com.ddbb.controller.ai;

import com.ddbb.service.ai.AiInsightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiInsightController {

    private final AiInsightService aiInsightService;

    @GetMapping("/insights")
    public ResponseEntity<Map<String, Object>> getInsights() {
        Map<String, Object> insights = aiInsightService.generateInsights();
        return ResponseEntity.ok(insights);
    }
}
