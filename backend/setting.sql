-- ========================================
-- DDBB 데이터베이스 통합 초기화 파일
-- ========================================
-- 개인 빵집 현실적인 데이터 (재고 3~17개, 하루 판매 30~40개)
--
-- 실행 방법:
-- mysql -u root -p1234 ddbb < all_in_one.sql
--
-- 포함 내용:
-- 1. 빵 7종 데이터
-- 2. 현실적인 재고 설정
-- 3. 60일 판매 데이터
-- 4. 오늘자 판매 데이터
-- ========================================

SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' as '';
SELECT '📦 DDBB 데이터베이스 초기화 시작' as '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' as '';
SELECT '' as '';

-- ========================================
-- 1단계: 기존 데이터 삭제
-- ========================================
SELECT '🗑️  기존 데이터 삭제 중...' as '';

DELETE FROM sales;
DELETE FROM inventory;
DELETE FROM bread;

-- ========================================
-- 2단계: 빵 데이터 초기화 (7종)
-- ========================================
SELECT '🍞 빵 데이터 생성 중...' as '';

INSERT INTO bread (name, price, description, created_at, updated_at) VALUES
('오리지널크라상', 3200, 'YOLO 모델로 인식되는 크루아상', NOW(), NOW()),
('소금버터롤', 2800, 'YOLO 모델로 인식되는 소금빵', NOW(), NOW()),
('다크초코피넛버터쿠키', 4200, 'YOLO 모델로 인식되는 쿠키', NOW(), NOW()),
('에그마요소금버터롤', 4500, 'YOLO 모델로 인식되는 에그마요 빵', NOW(), NOW()),
('초코청크머핀', 4500, 'YOLO 모델로 인식되는 머핀', NOW(), NOW()),
('호두파이(조각)', 4700, 'YOLO 모델로 인식되는 호두파이', NOW(), NOW()),
('츄러스꽈배기', 3500, 'YOLO 모델로 인식되는 꽈배기', NOW(), NOW());

-- 재고 초기 설정 (임시로 50개, 나중에 현실적으로 조정)
INSERT INTO inventory (bread_id, quantity, min_stock_level, last_restocked_at)
SELECT id, 50, 10, NOW()
FROM bread;

SELECT CONCAT('✅ 빵 ', COUNT(*), '종 등록 완료') as '' FROM bread;

-- ========================================
-- 3단계: 현실적인 재고 설정
-- ========================================
SELECT '' as '';
SELECT '📊 재고 데이터 설정 중...' as '';

-- 긴급 발주 필요 (< 5개)
UPDATE inventory SET quantity = 3, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '소금버터롤');

UPDATE inventory SET quantity = 4, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '초코청크머핀');

-- 재고 부족 주의 (5~7개)
UPDATE inventory SET quantity = 6, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '다크초코피넛버터쿠키');

UPDATE inventory SET quantity = 7, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '오리지널크라상');

-- 적정 재고 (8~14개)
UPDATE inventory SET quantity = 11, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '에그마요소금버터롤');

UPDATE inventory SET quantity = 9, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '호두파이(조각)');

-- 과잉 재고 (≥ 15개)
UPDATE inventory SET quantity = 17, min_stock_level = 5
WHERE bread_id = (SELECT id FROM bread WHERE name = '츄러스꽈배기');

SELECT CONCAT('✅ 총 재고: ', SUM(quantity), '개') as '' FROM inventory;

-- ========================================
-- 4단계: 판매 데이터 생성 (60일간)
-- ========================================
SELECT '' as '';
SELECT '💰 판매 데이터 생성 중 (60일)...' as '';

DELIMITER //

DROP PROCEDURE IF EXISTS generate_realistic_sales//

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

    DECLARE bread_cursor CURSOR FOR
        SELECT id, name, price FROM bread;
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;

    WHILE v_day_counter < v_days DO
        SET v_date = DATE_ADD(DATE_SUB(CURDATE(), INTERVAL v_days DAY), INTERVAL v_day_counter DAY);
        OPEN bread_cursor;

        read_loop: LOOP
            FETCH bread_cursor INTO v_bread_id, v_bread_name, v_bread_price;
            IF done THEN
                LEAVE read_loop;
            END IF;

            SET v_daily_sales_count = FLOOR(RAND() * 3);
            SET v_bread_counter = 0;

            WHILE v_bread_counter < v_daily_sales_count DO
                CASE
                    WHEN v_bread_name LIKE '%소금버터롤%' THEN
                        SET v_quantity = FLOOR(2 + RAND() * 5);
                    WHEN v_bread_name LIKE '%크라상%' THEN
                        SET v_quantity = FLOOR(1 + RAND() * 4);
                    WHEN v_bread_name LIKE '%머핀%' THEN
                        SET v_quantity = FLOOR(2 + RAND() * 4);
                    WHEN v_bread_name LIKE '%쿠키%' THEN
                        SET v_quantity = FLOOR(1 + RAND() * 3);
                    WHEN v_bread_name LIKE '%에그마요%' THEN
                        SET v_quantity = FLOOR(1 + RAND() * 3);
                    WHEN v_bread_name LIKE '%파이%' THEN
                        SET v_quantity = FLOOR(1 + RAND() * 3);
                    WHEN v_bread_name LIKE '%꽈배기%' THEN
                        SET v_quantity = FLOOR(1 + RAND() * 2);
                    ELSE
                        SET v_quantity = FLOOR(1 + RAND() * 3);
                END CASE;

                IF DAYOFWEEK(v_date) IN (1, 7) THEN
                    SET v_quantity = FLOOR(v_quantity * 1.3);
                END IF;

                IF v_day_counter > 45 THEN
                    SET v_quantity = FLOOR(v_quantity * 1.15);
                END IF;

                SET v_total_price = v_quantity * v_bread_price;

                INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
                VALUES (
                    v_bread_id,
                    v_quantity,
                    v_total_price,
                    DATE_ADD(v_date, INTERVAL FLOOR(RAND() * 10 + 9) HOUR),
                    DATE_ADD(v_date, INTERVAL FLOOR(RAND() * 10 + 9) HOUR)
                );

                SET v_bread_counter = v_bread_counter + 1;
            END WHILE;

        END LOOP;

        CLOSE bread_cursor;
        SET done = FALSE;
        SET v_day_counter = v_day_counter + 1;
    END WHILE;

END//

DELIMITER ;

CALL generate_realistic_sales();
DROP PROCEDURE IF EXISTS generate_realistic_sales;

SELECT CONCAT('✅ 60일 판매 데이터 ', COUNT(*), '건 생성') as '' FROM sales;

-- ========================================
-- 5단계: 최근 7일 정확한 데이터 재삽입
-- ========================================
SELECT '' as '';
SELECT '📅 최근 7일 데이터 정리 중...' as '';

DELETE FROM sales WHERE DATE(sale_date) >= DATE_SUB(CURDATE(), INTERVAL 7 DAY);

-- D-7
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 4, 4 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 7 DAY), INTERVAL 10 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 7 DAY), INTERVAL 10 HOUR) FROM bread b WHERE b.name = '소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 7 DAY), INTERVAL 14 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 7 DAY), INTERVAL 14 HOUR) FROM bread b WHERE b.name = '초코청크머핀';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 7 DAY), INTERVAL 11 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 7 DAY), INTERVAL 11 HOUR) FROM bread b WHERE b.name = '오리지널크라상';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 7 DAY), INTERVAL 15 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 7 DAY), INTERVAL 15 HOUR) FROM bread b WHERE b.name = '에그마요소금버터롤';

-- D-6
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 5, 5 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 9 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 9 HOUR) FROM bread b WHERE b.name = '소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 13 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 13 HOUR) FROM bread b WHERE b.name = '소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 4, 4 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 11 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 11 HOUR) FROM bread b WHERE b.name = '초코청크머핀';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 12 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 12 HOUR) FROM bread b WHERE b.name = '오리지널크라상';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 14 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 6 DAY), INTERVAL 14 HOUR) FROM bread b WHERE b.name = '다크초코피넛버터쿠키';

-- D-5
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 6, 6 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 10 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 10 HOUR) FROM bread b WHERE b.name = '소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 5, 5 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 12 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 12 HOUR) FROM bread b WHERE b.name = '초코청크머핀';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 11 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 11 HOUR) FROM bread b WHERE b.name = '에그마요소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 15 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 15 HOUR) FROM bread b WHERE b.name = '오리지널크라상';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 16 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 5 DAY), INTERVAL 16 HOUR) FROM bread b WHERE b.name = '츄러스꽈배기';

-- D-4
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 5, 5 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 9 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 9 HOUR) FROM bread b WHERE b.name = '소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 4, 4 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 14 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 14 HOUR) FROM bread b WHERE b.name = '초코청크머핀';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 11 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 11 HOUR) FROM bread b WHERE b.name = '오리지널크라상';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 13 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 4 DAY), INTERVAL 13 HOUR) FROM bread b WHERE b.name = '호두파이(조각)';

-- D-3
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 7, 7 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 10 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 10 HOUR) FROM bread b WHERE b.name = '소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 5, 5 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 12 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 12 HOUR) FROM bread b WHERE b.name = '초코청크머핀';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 4, 4 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 11 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 11 HOUR) FROM bread b WHERE b.name = '에그마요소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 14 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 3 DAY), INTERVAL 14 HOUR) FROM bread b WHERE b.name = '다크초코피넛버터쿠키';

-- D-2
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 6, 6 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 9 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 9 HOUR) FROM bread b WHERE b.name = '소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 4, 4 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 13 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 13 HOUR) FROM bread b WHERE b.name = '초코청크머핀';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 11 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 11 HOUR) FROM bread b WHERE b.name = '오리지널크라상';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 15 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 15 HOUR) FROM bread b WHERE b.name = '에그마요소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 16 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 2 DAY), INTERVAL 16 HOUR) FROM bread b WHERE b.name = '츄러스꽈배기';

-- D-1 (어제)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 8, 8 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 10 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 10 HOUR) FROM bread b WHERE b.name = '소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 6, 6 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 12 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 12 HOUR) FROM bread b WHERE b.name = '초코청크머핀';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 4, 4 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 11 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 11 HOUR) FROM bread b WHERE b.name = '오리지널크라상';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 13 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 13 HOUR) FROM bread b WHERE b.name = '에그마요소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 14 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 14 HOUR) FROM bread b WHERE b.name = '다크초코피넛버터쿠키';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 15 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 15 HOUR) FROM bread b WHERE b.name = '호두파이(조각)';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price, DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 16 HOUR), DATE_ADD(DATE_SUB(CURDATE(), INTERVAL 1 DAY), INTERVAL 16 HOUR) FROM bread b WHERE b.name = '츄러스꽈배기';

SELECT '✅ 최근 7일 데이터 재삽입 완료' as '';

-- ========================================
-- 6단계: 오늘 판매 데이터 (진행 중)
-- ========================================
SELECT '' as '';
SELECT '📅 오늘 판매 데이터 추가 중...' as '';

-- 오전 (9시~12시)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 3, 3 * b.price, CONCAT(CURDATE(), ' 09:15:00'), CONCAT(CURDATE(), ' 09:15:00') FROM bread b WHERE b.name = '소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, CONCAT(CURDATE(), ' 10:30:00'), CONCAT(CURDATE(), ' 10:30:00') FROM bread b WHERE b.name = '소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, CONCAT(CURDATE(), ' 09:45:00'), CONCAT(CURDATE(), ' 09:45:00') FROM bread b WHERE b.name = '초코청크머핀';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price, CONCAT(CURDATE(), ' 11:20:00'), CONCAT(CURDATE(), ' 11:20:00') FROM bread b WHERE b.name = '초코청크머핀';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, CONCAT(CURDATE(), ' 10:00:00'), CONCAT(CURDATE(), ' 10:00:00') FROM bread b WHERE b.name = '오리지널크라상';

-- 점심 (12시~14시)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, CONCAT(CURDATE(), ' 12:15:00'), CONCAT(CURDATE(), ' 12:15:00') FROM bread b WHERE b.name = '소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, CONCAT(CURDATE(), ' 12:30:00'), CONCAT(CURDATE(), ' 12:30:00') FROM bread b WHERE b.name = '에그마요소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price, CONCAT(CURDATE(), ' 13:00:00'), CONCAT(CURDATE(), ' 13:00:00') FROM bread b WHERE b.name = '초코청크머핀';

-- 오후 (14시~18시)
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 2, 2 * b.price, CONCAT(CURDATE(), ' 15:00:00'), CONCAT(CURDATE(), ' 15:00:00') FROM bread b WHERE b.name = '소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price, CONCAT(CURDATE(), ' 14:30:00'), CONCAT(CURDATE(), ' 14:30:00') FROM bread b WHERE b.name = '오리지널크라상';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price, CONCAT(CURDATE(), ' 16:00:00'), CONCAT(CURDATE(), ' 16:00:00') FROM bread b WHERE b.name = '다크초코피넛버터쿠키';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price, CONCAT(CURDATE(), ' 16:30:00'), CONCAT(CURDATE(), ' 16:30:00') FROM bread b WHERE b.name = '호두파이(조각)';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price, CONCAT(CURDATE(), ' 17:15:00'), CONCAT(CURDATE(), ' 17:15:00') FROM bread b WHERE b.name = '에그마요소금버터롤';
INSERT INTO sales (bread_id, quantity, total_price, sale_date, created_at)
SELECT b.id, 1, 1 * b.price, CONCAT(CURDATE(), ' 18:00:00'), CONCAT(CURDATE(), ' 18:00:00') FROM bread b WHERE b.name = '초코청크머핀';

SELECT CONCAT('✅ 오늘 판매 ', COUNT(*), '건 추가') as ''
FROM sales WHERE DATE(sale_date) = CURDATE();

-- ========================================
-- 최종 요약
-- ========================================
SELECT '' as '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' as '';
SELECT '🎉 데이터베이스 초기화 완료!' as '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' as '';
SELECT '' as '';

SELECT CONCAT('🍞 등록된 빵: ', COUNT(*), '개') as '요약' FROM bread;
SELECT CONCAT('📦 총 재고: ', SUM(quantity), '개') as '요약' FROM inventory;
SELECT CONCAT('💰 판매 기록: ', COUNT(*), '건 (최근 ',
    DATEDIFF(MAX(DATE(sale_date)), MIN(DATE(sale_date))) + 1, '일)') as '요약'
FROM sales;

SELECT '' as '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' as '';
SELECT '✅ AI 대시보드 사용 준비 완료!' as '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' as '';
