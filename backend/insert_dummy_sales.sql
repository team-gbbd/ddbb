-- 과거 판매 데이터 더미 생성 (최근 60일)
-- AI 분석을 위한 충분한 데이터 제공

-- 더미 판매 데이터 생성 프로시저
DELIMITER //

DROP PROCEDURE IF EXISTS generate_dummy_sales;

CREATE PROCEDURE generate_dummy_sales()
BEGIN
    DECLARE v_date DATE;
    DECLARE v_bread_id INT;
    DECLARE v_bread_name VARCHAR(100);
    DECLARE v_bread_price DECIMAL(10,2);
    DECLARE v_quantity INT;
    DECLARE v_total_price DECIMAL(10,2);
    DECLARE v_days INT DEFAULT 60;
    DECLARE v_day_counter INT DEFAULT 0;
    DECLARE v_bread_counter INT;
    DECLARE v_daily_sales_count INT;
    DECLARE done INT DEFAULT FALSE;

    -- 빵 목록 커서
    DECLARE bread_cursor CURSOR FOR
        SELECT id, name, price FROM bread;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    -- 60일 전부터 오늘까지 데이터 생성
    SET v_date = DATE_SUB(CURDATE(), INTERVAL v_days DAY);

    WHILE v_day_counter < v_days DO
        SET v_date = DATE_ADD(DATE_SUB(CURDATE(), INTERVAL v_days DAY), INTERVAL v_day_counter DAY);

        -- 각 날짜마다 빵별로 판매 데이터 생성
        OPEN bread_cursor;

        read_loop: LOOP
            FETCH bread_cursor INTO v_bread_id, v_bread_name, v_bread_price;
            IF done THEN
                LEAVE read_loop;
            END IF;

            -- 하루에 해당 빵이 팔린 횟수 (1~3회 랜덤)
            SET v_daily_sales_count = FLOOR(1 + RAND() * 3);
            SET v_bread_counter = 0;

            WHILE v_bread_counter < v_daily_sales_count DO
                -- 판매량 랜덤 생성 (빵 종류별로 다른 범위)
                CASE
                    WHEN v_bread_name LIKE '%크라상%' THEN
                        SET v_quantity = FLOOR(3 + RAND() * 8); -- 3~10개
                    WHEN v_bread_name LIKE '%소금버터롤%' THEN
                        SET v_quantity = FLOOR(5 + RAND() * 10); -- 5~14개
                    WHEN v_bread_name LIKE '%쿠키%' THEN
                        SET v_quantity = FLOOR(2 + RAND() * 6); -- 2~7개
                    WHEN v_bread_name LIKE '%에그마요%' THEN
                        SET v_quantity = FLOOR(2 + RAND() * 5); -- 2~6개
                    WHEN v_bread_name LIKE '%머핀%' THEN
                        SET v_quantity = FLOOR(3 + RAND() * 7); -- 3~9개
                    WHEN v_bread_name LIKE '%파이%' THEN
                        SET v_quantity = FLOOR(2 + RAND() * 5); -- 2~6개
                    WHEN v_bread_name LIKE '%꽈배기%' THEN
                        SET v_quantity = FLOOR(4 + RAND() * 8); -- 4~11개
                    ELSE
                        SET v_quantity = FLOOR(2 + RAND() * 6); -- 기본 2~7개
                END CASE;

                -- 주말에는 판매량 20% 증가
                IF DAYOFWEEK(v_date) IN (1, 7) THEN
                    SET v_quantity = FLOOR(v_quantity * 1.2);
                END IF;

                -- 최근일수록 판매량 약간 증가 (트렌드 반영)
                IF v_day_counter > 40 THEN
                    SET v_quantity = FLOOR(v_quantity * 1.1);
                END IF;

                -- 총 금액 계산
                SET v_total_price = v_quantity * v_bread_price;

                -- 판매 데이터 삽입 (하루 중 랜덤 시간)
                INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
                VALUES (
                    v_bread_id,
                    v_quantity,
                    v_total_price,
                    DATE_ADD(v_date, INTERVAL FLOOR(RAND() * 14 + 8) HOUR), -- 8시~22시 랜덤
                    DATE_ADD(v_date, INTERVAL FLOOR(RAND() * 14 + 8) HOUR)
                );

                SET v_bread_counter = v_bread_counter + 1;
            END WHILE;

        END LOOP;

        CLOSE bread_cursor;
        SET done = FALSE; -- 커서 리셋

        SET v_day_counter = v_day_counter + 1;
    END WHILE;

END //

DELIMITER ;

-- 프로시저 실행
CALL generate_dummy_sales();

-- 프로시저 삭제 (정리)
DROP PROCEDURE IF EXISTS generate_dummy_sales;

-- 결과 확인
SELECT '✅ 과거 60일 더미 판매 데이터 생성 완료!' as status;
SELECT
    b.name AS '빵 이름',
    COUNT(*) AS '판매 기록 수',
    SUM(s.quantity) AS '총 판매량',
    SUM(s.total_price) AS '총 매출',
    MIN(DATE(s.sale_date)) AS '최초 판매일',
    MAX(DATE(s.sale_date)) AS '최근 판매일'
FROM sales s
JOIN bread b ON s.bread_id = b.id
GROUP BY b.id, b.name
ORDER BY SUM(s.total_price) DESC;

SELECT
    DATE(sale_date) AS '날짜',
    COUNT(*) AS '판매 건수',
    SUM(quantity) AS '총 판매량',
    SUM(total_price) AS '일 매출'
FROM sales
GROUP BY DATE(sale_date)
ORDER BY DATE(sale_date) DESC
LIMIT 10;

-- ========================================
-- 최근 날짜 정확한 데이터 재삽입 (10/31, 11/1)
-- AI 대시보드 테스트 정확성 향상
-- ========================================

-- 기존 10/31, 11/1 데이터 삭제
DELETE FROM sales WHERE DATE(sale_date) IN ('2024-10-31', '2024-11-01');

-- ========== 10월 31일 데이터 ==========
-- 소금버터롤 (베스트셀러, 35개)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 12, 12 * b.price, '2024-10-31 09:30:00', '2024-10-31 09:30:00'
FROM bread b WHERE b.name = '소금버터롤';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 15, 15 * b.price, '2024-10-31 12:15:00', '2024-10-31 12:15:00'
FROM bread b WHERE b.name = '소금버터롤';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 8, 8 * b.price, '2024-10-31 16:45:00', '2024-10-31 16:45:00'
FROM bread b WHERE b.name = '소금버터롤';

-- 크라상 (2위, 20개)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 9, 9 * b.price, '2024-10-31 10:00:00', '2024-10-31 10:00:00'
FROM bread b WHERE b.name = '크라상';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 11, 11 * b.price, '2024-10-31 14:30:00', '2024-10-31 14:30:00'
FROM bread b WHERE b.name = '크라상';

-- 초코청크머핀 (3위, 15개)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 7, 7 * b.price, '2024-10-31 11:20:00', '2024-10-31 11:20:00'
FROM bread b WHERE b.name = '초코청크머핀';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 8, 8 * b.price, '2024-10-31 15:10:00', '2024-10-31 15:10:00'
FROM bread b WHERE b.name = '초코청크머핀';

-- 에그마요빵 (12개)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 5, 5 * b.price, '2024-10-31 10:45:00', '2024-10-31 10:45:00'
FROM bread b WHERE b.name = '에그마요빵';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 7, 7 * b.price, '2024-10-31 13:20:00', '2024-10-31 13:20:00'
FROM bread b WHERE b.name = '에그마요빵';

-- 쿠키 (10개)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 4, 4 * b.price, '2024-10-31 11:00:00', '2024-10-31 11:00:00'
FROM bread b WHERE b.name = '쿠키';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 6, 6 * b.price, '2024-10-31 17:30:00', '2024-10-31 17:30:00'
FROM bread b WHERE b.name = '쿠키';

-- 애플파이 (8개)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, '2024-10-31 12:00:00', '2024-10-31 12:00:00'
FROM bread b WHERE b.name = '애플파이';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 5, 5 * b.price, '2024-10-31 16:00:00', '2024-10-31 16:00:00'
FROM bread b WHERE b.name = '애플파이';

-- 버터꽈배기 (14개)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 6, 6 * b.price, '2024-10-31 10:30:00', '2024-10-31 10:30:00'
FROM bread b WHERE b.name = '버터꽈배기';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 8, 8 * b.price, '2024-10-31 14:00:00', '2024-10-31 14:00:00'
FROM bread b WHERE b.name = '버터꽈배기';

-- ========== 11월 1일 데이터 ==========
-- 소금버터롤 (베스트셀러 지속, 18개)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 10, 10 * b.price, '2024-11-01 09:15:00', '2024-11-01 09:15:00'
FROM bread b WHERE b.name = '소금버터롤';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 8, 8 * b.price, '2024-11-01 11:30:00', '2024-11-01 11:30:00'
FROM bread b WHERE b.name = '소금버터롤';

-- 크라상 (12개)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 7, 7 * b.price, '2024-11-01 10:00:00', '2024-11-01 10:00:00'
FROM bread b WHERE b.name = '크라상';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 5, 5 * b.price, '2024-11-01 13:00:00', '2024-11-01 13:00:00'
FROM bread b WHERE b.name = '크라상';

-- 초코청크머핀 (9개)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 5, 5 * b.price, '2024-11-01 10:30:00', '2024-11-01 10:30:00'
FROM bread b WHERE b.name = '초코청크머핀';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 4, 4 * b.price, '2024-11-01 12:00:00', '2024-11-01 12:00:00'
FROM bread b WHERE b.name = '초코청크머핀';

-- 에그마요빵 (7개)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 4, 4 * b.price, '2024-11-01 09:45:00', '2024-11-01 09:45:00'
FROM bread b WHERE b.name = '에그마요빵';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, '2024-11-01 11:15:00', '2024-11-01 11:15:00'
FROM bread b WHERE b.name = '에그마요빵';

-- 쿠키 (6개)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, '2024-11-01 10:15:00', '2024-11-01 10:15:00'
FROM bread b WHERE b.name = '쿠키';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, '2024-11-01 12:30:00', '2024-11-01 12:30:00'
FROM bread b WHERE b.name = '쿠키';

-- 애플파이 (5개)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, '2024-11-01 11:00:00', '2024-11-01 11:00:00'
FROM bread b WHERE b.name = '애플파이';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, '2024-11-01 13:15:00', '2024-11-01 13:15:00'
FROM bread b WHERE b.name = '애플파이';

-- 버터꽈배기 (8개)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 4, 4 * b.price, '2024-11-01 09:30:00', '2024-11-01 09:30:00'
FROM bread b WHERE b.name = '버터꽈배기';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 4, 4 * b.price, '2024-11-01 12:45:00', '2024-11-01 12:45:00'
FROM bread b WHERE b.name = '버터꽈배기';

-- 최종 결과 확인
SELECT '✅ 10/31, 11/1 정확한 데이터 재삽입 완료!' as status;

SELECT
    '10월 31일' AS '날짜',
    COUNT(*) AS '판매 건수',
    SUM(quantity) AS '총 판매량',
    CONCAT('₩', FORMAT(SUM(total_price), 0)) AS '총 매출'
FROM sales WHERE DATE(sale_date) = '2024-10-31'
UNION ALL
SELECT
    '11월 1일' AS '날짜',
    COUNT(*) AS '판매 건수',
    SUM(quantity) AS '총 판매량',
    CONCAT('₩', FORMAT(SUM(total_price), 0)) AS '총 매출'
FROM sales WHERE DATE(sale_date) = '2024-11-01';
