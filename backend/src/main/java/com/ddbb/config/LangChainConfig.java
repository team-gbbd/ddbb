package com.ddbb.config;

import com.ddbb.service.management.SalesPredictionAI;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.openai.OpenAiChatModel;
import dev.langchain4j.service.AiServices;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * LangChain4j 설정
 * 구조화된 AI 출력을 위한 빈 설정
 */
@Slf4j
@Configuration
public class LangChainConfig {
    
    @Value("${openai.api.key}")
    private String openaiApiKey;
    
    /**
     * OpenAI Chat Model 빈
     */
    @Bean
    public ChatLanguageModel chatLanguageModel() {
        log.info("🚀 LangChain4j ChatLanguageModel Bean 생성 시작");
        try {
            ChatLanguageModel model = OpenAiChatModel.builder()
                    .apiKey(openaiApiKey)
                    .modelName("gpt-4o-mini")
                    .temperature(0.2)
                    .timeout(Duration.ofSeconds(60))
                    .maxRetries(3)
                    .logRequests(true)
                    .logResponses(true)
                    .build();
            log.info("✅ ChatLanguageModel Bean 생성 완료");
            return model;
        } catch (Exception e) {
            log.error("❌ ChatLanguageModel Bean 생성 실패: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Sales Prediction AI Service 빈
     */
    @Bean
    public SalesPredictionAI salesPredictionAI(ChatLanguageModel chatLanguageModel) {
        log.info("🚀 SalesPredictionAI Bean 생성 시작");
        try {
            SalesPredictionAI ai = AiServices.builder(SalesPredictionAI.class)
                    .chatLanguageModel(chatLanguageModel)
                    .build();
            log.info("✅ SalesPredictionAI Bean 생성 완료");
            return ai;
        } catch (Exception e) {
            log.error("❌ SalesPredictionAI Bean 생성 실패: {}", e.getMessage(), e);
            throw e;
        }
    }
}

