-- 테스트용 재고 조절 SQL
-- AI 대시보드의 다양한 시나리오 테스트
-- 새로운 재고 기준: 최소 5개, 과잉 15개 이상

-- ========================================
-- 시나리오별 재고 설정
-- ========================================

-- 📌 시나리오 1: 품절 (즉시 발주)
-- 현재는 품절 없음 (테스트 시 필요하면 0으로 설정)

-- 📌 시나리오 2: 긴급 발주 필요 (< 5개)
-- 소금버터롤: 현재 재고 3개 (긴급!)
UPDATE inventory SET quantity = 3, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '소금버터롤');

-- 초코청크머핀: 현재 재고 4개 (긴급!)
UPDATE inventory SET quantity = 4, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '초코청크머핀');

-- 📌 시나리오 3: 재고 부족 주의 (5~7개)
-- 크라상: 현재 재고 6개 (주의 필요)
UPDATE inventory SET quantity = 6, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '크라상');

-- 에그마요빵: 현재 재고 7개 (주의 필요)
UPDATE inventory SET quantity = 7, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '에그마요빵');

-- 📌 시나리오 4: 적정 재고 (8~14개)
-- 애플파이: 현재 재고 10개 (적정, SNS 마케팅 가능)
UPDATE inventory SET quantity = 10, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '애플파이');

-- 버터꽈배기: 현재 재고 12개 (적정, SNS 마케팅 가능)
UPDATE inventory SET quantity = 12, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '버터꽈배기');

-- 📌 시나리오 5: 과잉 재고 (≥ 15개, 할인 프로모션 필요)
-- 쿠키: 현재 재고 25개 (과잉!)
UPDATE inventory SET quantity = 25, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '쿠키');

-- ========================================
-- 결과 확인
-- ========================================

SELECT '✅ 테스트용 재고 조절 완료!' as status;

SELECT
    b.name AS '빵 이름',
    i.quantity AS '현재 재고',
    i.min_stock_level AS '최소 재고',
    ROUND(
        i.quantity / NULLIF(
            (SELECT AVG(daily_sales) FROM (
                SELECT SUM(s.quantity) as daily_sales
                FROM sales s
                WHERE s.bread_id = b.id
                AND DATE(s.sale_date) >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
                GROUP BY DATE(s.sale_date)
            ) as daily_avg),
            0
        ),
        1
    ) AS '재고 소진일',
    CASE
        WHEN i.quantity = 0 THEN '❌ 품절'
        WHEN i.quantity < i.min_stock_level THEN '🚨 긴급 발주 필요'
        WHEN i.quantity < i.min_stock_level * 1.5 THEN '⚠️ 재고 부족 주의'
        WHEN i.quantity > i.min_stock_level * 5 THEN '📦 과잉 재고'
        ELSE '✅ 적정'
    END AS '재고 상태'
FROM inventory i
JOIN bread b ON i.bread_id = b.id
ORDER BY
    CASE
        WHEN i.quantity = 0 THEN 1
        WHEN i.quantity < i.min_stock_level THEN 2
        WHEN i.quantity < i.min_stock_level * 1.5 THEN 3
        WHEN i.quantity > i.min_stock_level * 5 THEN 4
        ELSE 5
    END;

-- ========================================
-- AI 대시보드 예상 분석 결과
-- ========================================

SELECT '
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📊 AI 대시보드 예상 분석 결과 (새 기준)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

재고 기준:
- 품절: 0개
- 긴급 발주: < 5개
- 재고 부족: 5~7개
- 적정 재고: 8~14개
- 과잉 재고: ≥ 15개

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[INSIGHT] 재고 위험 분석:
🚨 긴급: 소금버터롤 3개, 초코청크머핀 4개 - 긴급 발주 필요
⚠️ 주의: 크라상 6개, 에그마요빵 7개 - 재고 부족 주의
✅ 적정: 애플파이 10개, 버터꽈배기 12개 - 정상 운영
📦 과잉: 쿠키 25개 - 할인 프로모션 권장

[STRATEGY] 즉시 액션:
1. 소금버터롤 10개 긴급 발주 (최소 재고 5개 + 2일분)
2. 초코청크머핀 10개 발주 (최소 재고 확보)
3. 쿠키 25개 중 30% 할인으로 10개 소진 목표
4. SNS 마케팅: 애플파이(10개), 버터꽈배기(12개) 홍보 가능

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
' AS '예상 분석';
