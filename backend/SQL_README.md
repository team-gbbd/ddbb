# 📂 SQL 파일 가이드

개인 빵집 DDBB 프로젝트의 데이터베이스 초기화 및 테스트 데이터 관리 가이드입니다.

---

## 🚀 빠른 시작 (데이터베이스 초기화)

**한 번에 모든 데이터 설정하기:**

```bash
cd /Users/kyungmin/Downloads/ddbb/backend
mysql -u root -p1234 ddbb < setup_database.sql
```

이 명령어는 다음을 자동으로 실행합니다:
1. 빵 초기 데이터
2. 현실적인 재고 설정
3. 60일간 판매 데이터
4. 오늘자 판매 데이터

---

## 📋 SQL 파일 목록

### 🔹 통합 실행 파일

| 파일명 | 설명 | 용도 |
|--------|------|------|
| **setup_database.sql** | 전체 데이터베이스 초기화 | 개발 환경 리셋 시 사용 |

### 🔹 개별 실행 파일

| 파일명 | 크기 | 설명 |
|--------|------|------|
| **init_breads.sql** | 1.2K | 빵 7종 초기 데이터 (이름, 가격, 설명) |
| **update_inventory_realistic.sql** | 3.8K | 현실적인 재고 설정 (3~17개) |
| **insert_realistic_sales.sql** | 16K | 60일간 판매 데이터 (하루 30~40개) |
| **insert_today_sales.sql** | 5.9K | 오늘자 판매 데이터 (진행 중) |

### 🔹 보관 파일 (sql-archive/)

| 파일명 | 상태 | 비고 |
|--------|------|------|
| insert_dummy_sales.sql | ⛔ 구버전 | 비현실적인 판매량 (하루 200개+) |
| update_inventory_for_test.sql | ⛔ 구버전 | 비현실적인 재고 (50개+) |

---

## 📊 데이터 상세

### 1. 빵 데이터 (7종)
```
- 소금버터롤 (₩2,800)
- 오리지널크라상 (₩3,200)
- 초코청크머핀 (₩4,500)
- 다크초코피넛버터쿠키 (₩4,200)
- 에그마요소금버터롤 (₩4,500)
- 호두파이(조각) (₩4,700)
- 츄러스꽈배기 (₩3,500)
```

### 2. 재고 설정 (개인 빵집 규모)
```
🚨 긴급: 소금버터롤(3개), 초코청크머핀(4개)
⚠️ 주의: 다크초코피넛버터쿠키(6개), 오리지널크라상(7개)
✅ 적정: 호두파이(9개), 에그마요소금버터롤(11개)
📦 과잉: 츄러스꽈배기(17개)
```

### 3. 판매 데이터 특징
- **기간**: 최근 60일 + 오늘
- **하루 판매량**: 30~40개 (개인 빵집 현실 반영)
- **인기 제품**: 소금버터롤(일평균 6.3개), 초코청크머핀(일평균 4.4개)
- **비인기 제품**: 츄러스꽈배기(일평균 0.4개)

---

## 💡 개별 파일 실행 방법

### 빵 데이터만 초기화
```bash
mysql -u root -p1234 ddbb < init_breads.sql
```

### 재고만 리셋
```bash
mysql -u root -p1234 ddbb < update_inventory_realistic.sql
```

### 판매 데이터만 재생성
```bash
mysql -u root -p1234 ddbb < insert_realistic_sales.sql
mysql -u root -p1234 ddbb < insert_today_sales.sql
```

---

## 🔧 데이터베이스 완전 초기화

**⚠️ 주의: 모든 데이터가 삭제됩니다!**

```bash
mysql -u root -p1234 ddbb -e "
DELETE FROM sales;
DELETE FROM inventory;
DELETE FROM bread;
"

mysql -u root -p1234 ddbb < setup_database.sql
```

---

## 📈 AI 대시보드 예상 결과

위 데이터를 설정하면 AI 대시보드에서 다음과 같은 인사이트를 확인할 수 있습니다:

### [STRATEGY] 전략 제안
```
소금버터롤과 초코청크머핀 긴급 발주 권장:
- 소금버터롤 13개[2일분]
- 초코청크머핀 9개[2일분]

츄러스꽈배기 10-30% 할인으로 소진 목표
에그마요소금버터롤 SNS 마케팅 강화로 홍보 가능!
```

---

## 🗂️ 폴더 구조

```
backend/
├── setup_database.sql              ✨ 통합 실행 파일
├── init_breads.sql                 📦 빵 데이터
├── update_inventory_realistic.sql  📊 재고 설정
├── insert_realistic_sales.sql      💰 판매 데이터 (60일)
├── insert_today_sales.sql          📅 오늘 판매
├── SQL_README.md                   📖 이 문서
└── sql-archive/                    🗄️ 구버전 보관
    ├── insert_dummy_sales.sql
    └── update_inventory_for_test.sql
```

---

## ✅ 체크리스트

데이터베이스가 제대로 설정되었는지 확인:

```bash
# 빵 7종 등록 확인
mysql -u root -p1234 ddbb -e "SELECT COUNT(*) as '빵 개수' FROM bread;"

# 재고 설정 확인
mysql -u root -p1234 ddbb -e "
SELECT b.name, i.quantity
FROM inventory i
JOIN bread b ON i.bread_id = b.id
ORDER BY i.quantity;"

# 판매 데이터 확인 (최근 7일)
mysql -u root -p1234 ddbb -e "
SELECT DATE(sale_date) as '날짜', SUM(quantity) as '총 판매량'
FROM sales
WHERE DATE(sale_date) >= DATE_SUB(CURDATE(), INTERVAL 7 DAY)
GROUP BY DATE(sale_date)
ORDER BY DATE(sale_date) DESC;"
```

---

## 🚨 문제 해결

### Q: SOURCE 명령어 에러 발생
```bash
# SOURCE 대신 개별 실행
mysql -u root -p1234 ddbb < init_breads.sql
mysql -u root -p1234 ddbb < update_inventory_realistic.sql
mysql -u root -p1234 ddbb < insert_realistic_sales.sql
mysql -u root -p1234 ddbb < insert_today_sales.sql
```

### Q: 오늘 날짜만 업데이트하고 싶음
```bash
mysql -u root -p1234 ddbb < insert_today_sales.sql
```

### Q: 데이터가 중복됨
```bash
# sales, inventory 삭제 후 재실행
mysql -u root -p1234 ddbb -e "DELETE FROM sales; DELETE FROM inventory;"
mysql -u root -p1234 ddbb < setup_database.sql
```

---

**마지막 업데이트**: 2025-11-02
**버전**: 1.0 (현실적인 개인 빵집 데이터)
