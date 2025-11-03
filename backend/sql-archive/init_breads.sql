-- AI Scanner 기준으로 빵 데이터 초기화
-- models.py의 KOREAN_NAMES와 PRICES 기준

-- 기존 데이터 삭제 (외래키 제약 때문에 순서 중요)
DELETE FROM sales;
DELETE FROM inventory;
DELETE FROM bread;

-- AI Scanner 빵 데이터 삽입
INSERT INTO bread (name, price, description, created_at, updated_at) VALUES
('오리지널크라상', 3200, 'YOLO 모델로 인식되는 크루아상', NOW(), NOW()),
('소금버터롤', 2800, 'YOLO 모델로 인식되는 소금빵', NOW(), NOW()),
('다크초코피넛버터쿠키', 4200, 'YOLO 모델로 인식되는 쿠키', NOW(), NOW()),
('에그마요소금버터롤', 4500, 'YOLO 모델로 인식되는 에그마요 빵', NOW(), NOW()),
('초코청크머핀', 4500, 'YOLO 모델로 인식되는 머핀', NOW(), NOW()),
('호두파이(조각)', 4700, 'YOLO 모델로 인식되는 호두파이', NOW(), NOW()),
('츄러스꽈배기', 3500, 'YOLO 모델로 인식되는 꽈배기', NOW(), NOW());

-- 재고 초기 설정 (각 빵마다 50개씩, 최소재고 10개)
INSERT INTO inventory (bread_id, quantity, min_stock_level, last_restocked_at)
SELECT id, 50, 10, NOW()
FROM bread;

SELECT '✅ AI Scanner 기준 빵 데이터 초기화 완료!' as status;
SELECT * FROM bread;
SELECT * FROM inventory;
