-- 개인 빵집 현실적인 재고 설정
-- 재고 기준: 최소 5개, 과잉 15개 이상
-- 개인 빵집 특성: 최대 재고 15~20개 수준

-- ========================================
-- 현실적인 재고 시나리오
-- ========================================

-- 📌 긴급 발주 필요 (< 5개) - 인기 제품 빠르게 소진됨
-- 소금버터롤: 3개 (인기 제품, 긴급 발주!)
UPDATE inventory SET quantity = 3, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '소금버터롤');

-- 초코청크머핀: 4개 (인기 제품, 긴급 발주!)
UPDATE inventory SET quantity = 4, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '초코청크머핀');

-- 📌 재고 부족 주의 (5~7개) - 조금 덜 인기
-- 다크초코피넛버터쿠키: 6개 (발주 고려)
UPDATE inventory SET quantity = 6, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '다크초코피넛버터쿠키');

-- 오리지널크라상: 7개 (발주 고려)
UPDATE inventory SET quantity = 7, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '오리지널크라상');

-- 📌 적정 재고 (8~14개) - 안정적 판매
-- 에그마요소금버터롤: 11개 (적정, SNS 마케팅 가능)
UPDATE inventory SET quantity = 11, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '에그마요소금버터롤');

-- 호두파이(조각): 9개 (적정)
UPDATE inventory SET quantity = 9, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '호두파이(조각)');

-- 📌 과잉 재고 (≥ 15개) - 판매 부진, 할인 필요
-- 츄러스꽈배기: 17개 (과잉, 할인 프로모션 권장)
UPDATE inventory SET quantity = 17, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '츄러스꽈배기');

-- ========================================
-- 결과 확인
-- ========================================

SELECT '✅ 현실적인 재고 조정 완료!' as status;

SELECT
    b.name AS '빵 이름',
    i.quantity AS '현재 재고',
    i.min_stock_level AS '최소 재고',
    CASE
        WHEN i.quantity = 0 THEN '❌ 품절'
        WHEN i.quantity < i.min_stock_level THEN '🚨 긴급 발주 필요'
        WHEN i.quantity < i.min_stock_level * 1.5 THEN '⚠️ 재고 부족 주의'
        WHEN i.quantity >= 15 THEN '📦 과잉 재고'
        ELSE '✅ 적정'
    END AS '재고 상태'
FROM inventory i
JOIN bread b ON i.bread_id = b.id
ORDER BY
    CASE
        WHEN i.quantity = 0 THEN 1
        WHEN i.quantity < i.min_stock_level THEN 2
        WHEN i.quantity < i.min_stock_level * 1.5 THEN 3
        WHEN i.quantity >= 15 THEN 4
        ELSE 5
    END,
    i.quantity;

-- ========================================
-- AI 대시보드 예상 결과
-- ========================================

SELECT '
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📊 개인 빵집 현실적인 재고 (최대 17개)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[재고 현황]
🚨 소금버터롤: 3개 (긴급!)
🚨 초코청크머핀: 4개 (긴급!)
⚠️ 다크초코피넛버터쿠키: 6개 (주의)
⚠️ 오리지널크라상: 7개 (주의)
✅ 호두파이(조각): 9개 (적정)
✅ 에그마요소금버터롤: 11개 (적정, SNS 가능)
📦 츄러스꽈배기: 17개 (과잉, 할인 필요)

[AI 추천 액션]
1. 소금버터롤 10개 긴급 발주 (일평균 × 2일분)
2. 초코청크머핀 8개 발주 권장
3. 츄러스꽈배기 17개 중 20% 할인으로 7개 소진 목표
4. 에그마요소금버터롤 SNS 마케팅 강화 제안

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
' AS '예상 분석';
