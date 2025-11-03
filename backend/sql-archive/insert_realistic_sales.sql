-- 개인 빵집 현실적인 판매 데이터
-- 하루 총 판매량: 30~60개 (개인 빵집 규모)
-- 최근 60일 데이터 생성

-- 기존 판매 데이터 삭제
DELETE FROM sales;

-- 더미 판매 데이터 생성 프로시저
DELIMITER //

DROP PROCEDURE IF EXISTS generate_realistic_sales;

CREATE PROCEDURE generate_realistic_sales()
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

    -- 60일 전부터 어제까지 데이터 생성 (오늘 제외)
    WHILE v_day_counter < v_days DO
        SET v_date = DATE_ADD(DATE_SUB(CURDATE(), INTERVAL v_days DAY), INTERVAL v_day_counter DAY);

        -- 각 날짜마다 빵별로 판매 데이터 생성
        OPEN bread_cursor;

        read_loop: LOOP
            FETCH bread_cursor INTO v_bread_id, v_bread_name, v_bread_price;
            IF done THEN
                LEAVE read_loop;
            END IF;

            -- 하루에 해당 빵이 팔린 횟수 (0~2회 랜덤, 일부는 안 팔릴 수도)
            SET v_daily_sales_count = FLOOR(RAND() * 3);

            SET v_bread_counter = 0;

            WHILE v_bread_counter < v_daily_sales_count DO
                -- 개인 빵집 현실적인 판매량 (하루 총 30~60개)
                CASE
                    WHEN v_bread_name LIKE '%소금버터롤%' THEN
                        SET v_quantity = FLOOR(2 + RAND() * 5); -- 2~6개 (인기)
                    WHEN v_bread_name LIKE '%크라상%' THEN
                        SET v_quantity = FLOOR(1 + RAND() * 4); -- 1~4개
                    WHEN v_bread_name LIKE '%머핀%' THEN
                        SET v_quantity = FLOOR(2 + RAND() * 4); -- 2~5개 (인기)
                    WHEN v_bread_name LIKE '%쿠키%' THEN
                        SET v_quantity = FLOOR(1 + RAND() * 3); -- 1~3개
                    WHEN v_bread_name LIKE '%에그마요%' THEN
                        SET v_quantity = FLOOR(1 + RAND() * 3); -- 1~3개
                    WHEN v_bread_name LIKE '%파이%' THEN
                        SET v_quantity = FLOOR(1 + RAND() * 3); -- 1~3개
                    WHEN v_bread_name LIKE '%꽈배기%' THEN
                        SET v_quantity = FLOOR(1 + RAND() * 2); -- 1~2개 (낮은 인기)
                    ELSE
                        SET v_quantity = FLOOR(1 + RAND() * 3); -- 기본 1~3개
                END CASE;

                -- 주말에는 판매량 30% 증가
                IF DAYOFWEEK(v_date) IN (1, 7) THEN
                    SET v_quantity = FLOOR(v_quantity * 1.3);
                END IF;

                -- 최근일수록 판매량 약간 증가 (트렌드 반영)
                IF v_day_counter > 45 THEN
                    SET v_quantity = FLOOR(v_quantity * 1.15);
                END IF;

                -- 총 금액 계산
                SET v_total_price = v_quantity * v_bread_price;

                -- 판매 데이터 삽입 (하루 중 랜덤 시간: 9시~19시)
                INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
                VALUES (
                    v_bread_id,
                    v_quantity,
                    v_total_price,
                    DATE_ADD(v_date, INTERVAL FLOOR(RAND() * 10 + 9) HOUR), -- 9시~19시 랜덤
                    DATE_ADD(v_date, INTERVAL FLOOR(RAND() * 10 + 9) HOUR)
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
CALL generate_realistic_sales();

-- 프로시저 삭제 (정리)
DROP PROCEDURE IF EXISTS generate_realistic_sales;

-- 결과 확인
SELECT '✅ 현실적인 판매 데이터 생성 완료 (하루 30~60개)' as status;

SELECT
    b.name AS '빵 이름',
    COUNT(*) AS '판매 기록 수',
    SUM(s.quantity) AS '총 판매량 (60일)',
    ROUND(SUM(s.quantity) / 60, 1) AS '일평균 판매량',
    CONCAT('₩', FORMAT(SUM(s.total_price), 0)) AS '총 매출'
FROM sales s
JOIN bread b ON s.bread_id = b.id
GROUP BY b.id, b.name
ORDER BY SUM(s.quantity) DESC;

SELECT
    DATE(sale_date) AS '날짜',
    COUNT(*) AS '판매 건수',
    SUM(quantity) AS '총 판매량'
FROM sales
GROUP BY DATE(sale_date)
ORDER BY DATE(sale_date) DESC
LIMIT 10;

-- ========================================
-- 최근 7일 정확한 데이터 재삽입 (어제부터 7일 전까지)
-- AI 대시보드 테스트용
-- ========================================

-- 최근 7일 데이터 삭제
DELETE FROM sales WHERE DATE(sale_date) >= DATE_SUB(CURDATE(), INTERVAL 7 DAY);

-- ========== D-7 (7일 전) ==========
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 4, 4 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 7 DAY), INTERVAL 10 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 7 DAY), INTERVAL 10 HOUR)
FROM bread b WHERE b.name = '소금버터롤';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 7 DAY), INTERVAL 14 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 7 DAY), INTERVAL 14 HOUR)
FROM bread b WHERE b.name = '초코청크머핀';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 7 DAY), INTERVAL 11 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 7 DAY), INTERVAL 11 HOUR)
FROM bread b WHERE b.name = '오리지널크라상';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 7 DAY), INTERVAL 15 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 7 DAY), INTERVAL 15 HOUR)
FROM bread b WHERE b.name = '에그마요소금버터롤';

-- ========== D-6 ==========
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 5, 5 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 9 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 9 HOUR)
FROM bread b WHERE b.name = '소금버터롤';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 13 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 13 HOUR)
FROM bread b WHERE b.name = '소금버터롤';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 4, 4 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 11 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 11 HOUR)
FROM bread b WHERE b.name = '초코청크머핀';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 12 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 12 HOUR)
FROM bread b WHERE b.name = '오리지널크라상';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 14 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 14 HOUR)
FROM bread b WHERE b.name = '다크초코피넛버터쿠키';

-- ========== D-5 ==========
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 6, 6 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 10 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 10 HOUR)
FROM bread b WHERE b.name = '소금버터롤';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 5, 5 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 12 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 12 HOUR)
FROM bread b WHERE b.name = '초코청크머핀';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 11 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 11 HOUR)
FROM bread b WHERE b.name = '에그마요소금버터롤';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 15 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 15 HOUR)
FROM bread b WHERE b.name = '오리지널크라상';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 16 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 16 HOUR)
FROM bread b WHERE b.name = '츄러스꽈배기';

-- ========== D-4 ==========
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 5, 5 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 9 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 9 HOUR)
FROM bread b WHERE b.name = '소금버터롤';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 4, 4 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 14 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 14 HOUR)
FROM bread b WHERE b.name = '초코청크머핀';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 11 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 11 HOUR)
FROM bread b WHERE b.name = '오리지널크라상';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 13 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 13 HOUR)
FROM bread b WHERE b.name = '호두파이(조각)';

-- ========== D-3 ==========
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 7, 7 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 10 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 10 HOUR)
FROM bread b WHERE b.name = '소금버터롤';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 5, 5 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 12 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 12 HOUR)
FROM bread b WHERE b.name = '초코청크머핀';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 4, 4 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 11 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 11 HOUR)
FROM bread b WHERE b.name = '에그마요소금버터롤';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 14 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 14 HOUR)
FROM bread b WHERE b.name = '다크초코피넛버터쿠키';

-- ========== D-2 (그제) ==========
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 6, 6 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 9 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 9 HOUR)
FROM bread b WHERE b.name = '소금버터롤';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 4, 4 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 13 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 13 HOUR)
FROM bread b WHERE b.name = '초코청크머핀';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 11 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 11 HOUR)
FROM bread b WHERE b.name = '오리지널크라상';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 15 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 15 HOUR)
FROM bread b WHERE b.name = '에그마요소금버터롤';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 16 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 16 HOUR)
FROM bread b WHERE b.name = '츄러스꽈배기';

-- ========== D-1 (어제) ==========
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 8, 8 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 10 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 10 HOUR)
FROM bread b WHERE b.name = '소금버터롤';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 6, 6 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 12 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 12 HOUR)
FROM bread b WHERE b.name = '초코청크머핀';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 4, 4 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 11 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 11 HOUR)
FROM bread b WHERE b.name = '오리지널크라상';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 13 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 13 HOUR)
FROM bread b WHERE b.name = '에그마요소금버터롤';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 14 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 14 HOUR)
FROM bread b WHERE b.name = '다크초코피넛버터쿠키';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 15 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 15 HOUR)
FROM bread b WHERE b.name = '호두파이(조각)';

INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 16 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 16 HOUR)
FROM bread b WHERE b.name = '츄러스꽈배기';

-- ========== 오늘 (진행 중) ==========
-- 오늘 데이터는 없음 (AI가 실시간으로 판단)

-- 최종 결과 확인
SELECT '✅ 최근 7일 정확한 판매 데이터 재삽입 완료!' as status;

SELECT
    DATE(sale_date) AS '날짜',
    COUNT(*) AS '판매 건수',
    SUM(quantity) AS '총 판매량',
    CONCAT('₩', FORMAT(SUM(total_price), 0)) AS '총 매출'
FROM sales
WHERE DATE(sale_date) >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY DATE(sale_date)
ORDER BY DATE(sale_date) DESC;

-- 빵별 최근 7일 평균
SELECT
    b.name AS '빵 이름',
    COUNT(*) AS '판매 기록 수',
    SUM(s.quantity) AS '7일 총 판매량',
    ROUND(SUM(s.quantity) / 7.0, 1) AS '일평균 판매량'
FROM sales s
JOIN bread b ON s.bread_id = b.id
WHERE DATE(s.sale_date) >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY b.id, b.name
ORDER BY SUM(s.quantity) DESC;
