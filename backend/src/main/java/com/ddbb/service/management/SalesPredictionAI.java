package com.ddbb.service.management;

import com.ddbb.dto.management.BreadPredictionResult;
import com.ddbb.dto.management.PredictionResult;
import com.ddbb.dto.management.RevenuePredictionResult;
import dev.langchain4j.service.SystemMessage;
import dev.langchain4j.service.UserMessage;
import dev.langchain4j.service.V;

/**
 * LangChain4j AI Service 인터페이스
 * 구조화된 출력(Structured Output)을 보장하여 일관성 있는 예측 제공
 */
public interface SalesPredictionAI {
    
    /**
     * 판매량 예측 (일관성 보장)
     */
    @SystemMessage("""
            당신은 빵집 판매 데이터 분석 전문가입니다.
            과거 판매 데이터를 기반으로 미래 판매량을 정확하게 예측합니다.
            
            ** 필수 예측 방법론 (반드시 준수) **:
            1. 최근 14일 데이터의 이동평균을 기준값으로 사용
            2. 요일별 패턴 유지: 같은 요일의 과거 평균을 반영
            3. 마지막 실제 데이터에서 부드럽게 전환 (첫 예측값은 마지막 실제값의 ±15% 이내)
            4. 전체 예측값은 최근 14일 평균 대비 ±25% 범위 내로 제한
            5. 급격한 변화 방지: 연속된 예측값 간 차이는 ±20% 이내로 유지
            6. 과거 데이터의 전반적인 추세선을 예측에 반영
            
            반드시 JSON 형식으로 응답하며, predictions 필드에는 날짜를 키로, 예측 판매량을 값으로 하는 객체를 포함해야 합니다.
            """)
    @UserMessage("""
            다음은 과거 판매량 데이터입니다:
            {{historicalData}}
            
            마지막 날짜 {{lastDate}}를 기준으로 향후 {{days}}일간의 판매량을 예측해주세요.
            
            각 날짜별로 정수 형태의 예측 판매량을 제공하고,
            예측의 신뢰도와 예측 근거를 함께 제공해주세요.
            """)
    PredictionResult predictSales(
            @V("historicalData") String historicalData,
            @V("days") int days,
            @V("lastDate") String lastDate
    );
    
    /**
     * 수익 예측 (일관성 보장)
     */
    @SystemMessage("""
            당신은 빵집 수익 분석 전문가입니다.
            과거 수익 데이터를 기반으로 미래 수익을 정확하게 예측합니다.
            
            ** 필수 예측 방법론 (반드시 준수) **:
            1. 최근 14일 데이터의 이동평균을 기준값으로 사용
            2. 요일별 패턴 유지
            3. 마지막 실제 데이터에서 부드럽게 전환
            4. 전체 예측값은 최근 14일 평균 대비 ±25% 범위 내로 제한
            
            반드시 JSON 형식으로 응답하며, predictions 필드에는 날짜를 키로, 예측 수익(실수)를 값으로 하는 객체를 포함해야 합니다.
            """)
    @UserMessage("""
            다음은 과거 일별 수익 데이터입니다:
            {{historicalData}}
            
            마지막 날짜 {{lastDate}}를 기준으로 향후 {{days}}일간의 수익을 예측해주세요.
            """)
    RevenuePredictionResult predictRevenue(
            @V("historicalData") String historicalData,
            @V("days") int days,
            @V("lastDate") String lastDate
    );
    
    /**
     * 빵별 판매량 예측 (일관성 보장)
     */
    @SystemMessage("""
            당신은 빵집 재고 관리 전문가입니다.
            각 빵의 판매 데이터와 재고 수준을 분석하여 다음 주(7일) 판매량을 예측합니다.
            
            ** 필수 예측 방법론 (반드시 준수) **:
            1. 일평균 판매량을 기준값으로 사용
            2. 요일별 가중치 적용
            3. 전체 7일 예측 합계는 (일평균 × 7 × 1.1) ± 12% 범위 내로 제한
            
            반드시 JSON 형식으로 응답하며, predictions 필드에는 빵 이름을 키로, 예측 판매량(정수)를 값으로 하는 객체를 포함해야 합니다.
            """)
    @UserMessage("""
            다음은 각 빵의 판매 및 재고 데이터입니다:
            {{inventoryInfo}}
            
            다음 주(7일간)의 빵별 예상 판매량을 예측해주세요.
            """)
    BreadPredictionResult predictBreadSales(
            @V("inventoryInfo") String inventoryInfo
    );
}

