-- 오늘자 판매 데이터 추가 (진행 중인 하루)
-- 현재 시각 기준 오전~오후 판매 기록

-- 오늘 데이터 삭제 (있다면)
DELETE FROM sales WHERE DATE(sale_date) = CURDATE();

-- ========== 오늘 (11월 2일) 판매 데이터 ==========
-- 개인 빵집 현실적인 판매량 (진행 중)

-- 📌 오전 판매 (9시~12시)
-- 소금버터롤 (인기 제품, 아침 손님)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price,
    CONCAT(CURDATE(), ' 09:15:00'),
    CONCAT(CURDATE(), ' 09:15:00')
FROM bread b WHERE b.name = '소금버터롤';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price,
    CONCAT(CURDATE(), ' 10:30:00'),
    CONCAT(CURDATE(), ' 10:30:00')
FROM bread b WHERE b.name = '소금버터롤';

-- 초코청크머핀 (아침 간식)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price,
    CONCAT(CURDATE(), ' 09:45:00'),
    CONCAT(CURDATE(), ' 09:45:00')
FROM bread b WHERE b.name = '초코청크머핀';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price,
    CONCAT(CURDATE(), ' 11:20:00'),
    CONCAT(CURDATE(), ' 11:20:00')
FROM bread b WHERE b.name = '초코청크머핀';

-- 오리지널크라상 (아침)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price,
    CONCAT(CURDATE(), ' 10:00:00'),
    CONCAT(CURDATE(), ' 10:00:00')
FROM bread b WHERE b.name = '오리지널크라상';

-- 📌 점심 판매 (12시~14시)
-- 소금버터롤 (점심 손님)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price,
    CONCAT(CURDATE(), ' 12:15:00'),
    CONCAT(CURDATE(), ' 12:15:00')
FROM bread b WHERE b.name = '소금버터롤';

-- 에그마요소금버터롤 (점심용)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price,
    CONCAT(CURDATE(), ' 12:30:00'),
    CONCAT(CURDATE(), ' 12:30:00')
FROM bread b WHERE b.name = '에그마요소금버터롤';

-- 초코청크머핀 (점심 후 간식)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price,
    CONCAT(CURDATE(), ' 13:00:00'),
    CONCAT(CURDATE(), ' 13:00:00')
FROM bread b WHERE b.name = '초코청크머핀';

-- 📌 오후 판매 (14시~현재)
-- 소금버터롤 (오후 손님)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price,
    CONCAT(CURDATE(), ' 15:00:00'),
    CONCAT(CURDATE(), ' 15:00:00')
FROM bread b WHERE b.name = '소금버터롤';

-- 오리지널크라상 (오후 간식)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price,
    CONCAT(CURDATE(), ' 14:30:00'),
    CONCAT(CURDATE(), ' 14:30:00')
FROM bread b WHERE b.name = '오리지널크라상';

-- 다크초코피넛버터쿠키
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price,
    CONCAT(CURDATE(), ' 16:00:00'),
    CONCAT(CURDATE(), ' 16:00:00')
FROM bread b WHERE b.name = '다크초코피넛버터쿠키';

-- 호두파이(조각) (오후 간식)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price,
    CONCAT(CURDATE(), ' 16:30:00'),
    CONCAT(CURDATE(), ' 16:30:00')
FROM bread b WHERE b.name = '호두파이(조각)';

-- 에그마요소금버터롤 (오후)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price,
    CONCAT(CURDATE(), ' 17:15:00'),
    CONCAT(CURDATE(), ' 17:15:00')
FROM bread b WHERE b.name = '에그마요소금버터롤';

-- 초코청크머핀 (오후)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price,
    CONCAT(CURDATE(), ' 18:00:00'),
    CONCAT(CURDATE(), ' 18:00:00')
FROM bread b WHERE b.name = '초코청크머핀';

-- ========================================
-- 결과 확인
-- ========================================

SELECT '✅ 오늘자 판매 데이터 추가 완료!' as status;

SELECT
    '오늘 (진행 중)' AS '날짜',
    COUNT(*) AS '판매 건수',
    SUM(quantity) AS '총 판매량',
    CONCAT('₩', FORMAT(SUM(total_price), 0)) AS '총 매출'
FROM sales WHERE DATE(sale_date) = CURDATE();

SELECT
    b.name AS '빵 이름',
    SUM(s.quantity) AS '오늘 판매량',
    CONCAT('₩', FORMAT(SUM(s.total_price), 0)) AS '오늘 매출'
FROM sales s
JOIN bread b ON s.bread_id = b.id
WHERE DATE(s.sale_date) = CURDATE()
GROUP BY b.id, b.name
ORDER BY SUM(s.quantity) DESC;

-- 시간대별 판매 확인
SELECT
    HOUR(sale_date) AS '시간대',
    COUNT(*) AS '판매 건수',
    SUM(quantity) AS '판매량'
FROM sales
WHERE DATE(sale_date) = CURDATE()
GROUP BY HOUR(sale_date)
ORDER BY HOUR(sale_date);

-- ========================================
-- AI 대시보드 예상 결과
-- ========================================

SELECT '
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
📊 오늘 판매 현황 (진행 중)
━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

[현재까지 판매]
🥇 소금버터롤: 9개 (베스트셀러)
🥈 초코청크머핀: 5개
🥉 오리지널크라상: 3개
   에그마요소금버터롤: 3개
   다크초코피넛버터쿠키: 1개
   호두파이(조각): 1개

총 판매량: 22개 (진행 중)
일평균 대비: 적정 수준 (하루 평균 30-40개)

[AI 예상]
- 소금버터롤: 일평균 6.3개 → 오늘 9개 (초과 판매 중!)
- 초코청크머핀: 일평균 4.4개 → 오늘 5개 (양호)
- 저녁 시간대 추가 판매 예상 (3-5개)

━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
' AS '예상 분석';
