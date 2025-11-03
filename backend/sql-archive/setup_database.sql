-- ========================================
-- DDBB 데이터베이스 초기 설정
-- ========================================
-- 이 파일은 개발/테스트 환경 초기화용입니다
-- 실행 순서: 빵 데이터 → 재고 설정 → 판매 데이터
--
-- 실행 방법:
-- mysql -u root -p1234 ddbb < setup_database.sql
-- ========================================

-- 1단계: 기존 데이터 삭제 (주의!)
-- DELETE FROM sales;
-- DELETE FROM inventory;
-- DELETE FROM bread;

SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' as '';
SELECT '📦 DDBB 데이터베이스 초기화 시작' as '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' as '';
SELECT '' as '';

-- 2단계: 빵 초기 데이터
SELECT '🍞 1/3 빵 데이터 초기화 중...' as '';
SOURCE init_breads.sql;

-- 3단계: 현실적인 재고 설정
SELECT '' as '';
SELECT '📊 2/3 재고 데이터 설정 중...' as '';
SOURCE update_inventory_realistic.sql;

-- 4단계: 판매 데이터 생성 (60일 + 오늘)
SELECT '' as '';
SELECT '💰 3/3 판매 데이터 생성 중...' as '';
SOURCE insert_realistic_sales.sql;

SELECT '' as '';
SELECT '✅ 오늘자 데이터 추가 중...' as '';
SOURCE insert_today_sales.sql;

-- 최종 결과
SELECT '' as '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' as '';
SELECT '🎉 데이터베이스 초기화 완료!' as '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' as '';
SELECT '' as '';

SELECT '📊 최종 데이터 요약:' as '';
SELECT '' as '';

-- 빵 개수
SELECT
    CONCAT('🍞 등록된 빵: ', COUNT(*), '개') as '요약'
FROM bread;

-- 재고 현황
SELECT
    CONCAT('📦 총 재고: ', SUM(quantity), '개 (', COUNT(*), '종)') as '요약'
FROM inventory;

-- 판매 데이터
SELECT
    CONCAT('💰 판매 기록: 최근 ',
           DATEDIFF(MAX(DATE(sale_date)), MIN(DATE(sale_date))) + 1,
           '일간 ', COUNT(*), '건') as '요약'
FROM sales;

SELECT '' as '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' as '';
SELECT '✅ AI 대시보드를 이용할 준비가 완료되었습니다!' as '';
SELECT '━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━' as '';
