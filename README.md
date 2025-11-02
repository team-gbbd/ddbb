# 딥딥빵빵 (DDBB) - 재고확인 및 판매량 관리 시스템

## 📋 프로젝트 소개

**딥딥빵빵**은 AI를 통한 빵 스캔 및 빵 추천과 결제 시스템을 제공하는 프로젝트입니다.

이 리포지토리는 **재고확인 및 판매량(그래프)** 기능을 담당합니다.

### 담당자
- **이름**: 고동욱
- **담당 기능**: 재고확인 및 판매량(그래프)

---

## 🛠 기술 스택

### Backend
- **Language**: Java 17
- **Framework**: Spring Boot 3.5.7
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA
- **Build Tool**: Gradle
- **기타**: Lombok, Spring Security

### Frontend
- **Build Tool**: Vite
- **Language**: JavaScript (JSX)
- **Framework**: React 19
- **UI Library**: Bootstrap 5
- **HTTP Client**: Axios
- **Routing**: React Router
- **Icons**: Bootstrap Icons

---

## 📁 프로젝트 구조

```
src/main/java/com/ddbb/
├── config/              # 설정 클래스
│   ├── SecurityConfig.java
│   └── DataInitializer.java
├── controller/          # REST API 컨트롤러
│   ├── management/
│   │   ├── InventoryController.java
│   │   └── SalesController.java
│   └── payment/
│       └── PaymentController.java     # 🆕 결제 연동
├── dto/                 # 데이터 전송 객체
│   ├── management/
│   │   ├── InventoryResponse.java
│   │   ├── SalesResponse.java
│   │   └── ...
│   └── payment/                        # 🆕 결제 DTO
│       ├── PaymentCompleteRequest.java
│       ├── PaymentCompleteResponse.java
│       └── PaymentItem.java
├── entity/              # JPA 엔티티
│   ├── Bread.java
│   ├── Inventory.java
│   └── Sales.java
├── exception/           # 예외 처리
│   ├── GlobalExceptionHandler.java
│   └── ErrorResponse.java
├── repository/          # 데이터 접근 계층
│   ├── BreadRepository.java
│   ├── InventoryRepository.java
│   └── SalesRepository.java
├── service/             # 비즈니스 로직
│   ├── management/
│   │   ├── InventoryService.java
│   │   └── SalesService.java
│   └── payment/
│       └── PaymentService.java         # 🆕 결제 처리
└── DdbbApplication.java # 메인 애플리케이션
```

---

## 🚀 시작하기

### 1. 사전 요구사항

- Java 17 이상
- MySQL 8.0 이상
- Gradle
- Node.js 16.x 이상
- npm 또는 yarn

### 2. 데이터베이스 설정

MySQL에 데이터베이스를 생성합니다:

```sql
CREATE DATABASE ddbb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 3. 설정 파일 수정

`backend/src/main/resources/application.properties` 파일을 수정합니다:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ddbb?useSSL=false&serverTimezone=Asia/Seoul&characterEncoding=UTF-8
spring.datasource.username=your_username
spring.datasource.password=your_password
```

### 4. 백엔드 실행

```bash
cd backend

# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

백엔드 서버가 시작되면 `http://localhost:8080`에서 접근 가능합니다.

### 5. 프론트엔드 실행

새 터미널을 열고:

```bash
cd frontend
npm install
npm run dev
```

Vite 개발 서버가 시작됩니다. 브라우저에서 `http://localhost:3000`으로 접속하세요.

### 6. 접속

- **프론트엔드**: http://localhost:3000
- **백엔드 API**: http://localhost:8080/api

---

## 📊 주요 기능

### 1. 재고 관리
- ✅ 전체 재고 조회
- ✅ 빵별 재고 조회
- ✅ 재고 부족 품목 알림
- ✅ 재고 수량 업데이트
- ✅ 재고 입고/출고 관리

### 2. 판매량 분석
- ✅ 실시간 판매 기록
- ✅ 기간별 판매 내역
- ✅ 빵별 판매 통계
- ✅ 일별/주별/월별 판매 추이
- ✅ 그래프용 데이터 제공

---

## 🔌 API 엔드포인트

### 재고 관리 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| GET | `/api/inventory` | 모든 재고 조회 |
| GET | `/api/inventory/bread/{breadId}` | 특정 빵 재고 조회 |
| GET | `/api/inventory/low-stock` | 재고 부족 품목 조회 |
| PUT | `/api/inventory/bread/{breadId}` | 재고 업데이트 |
| POST | `/api/inventory/bread/{breadId}/increase` | 재고 증가 (입고) |
| POST | `/api/inventory/bread/{breadId}/decrease` | 재고 감소 |

### 판매량 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/sales` | 판매 기록 생성 |
| GET | `/api/sales` | 기간별 판매 내역 |
| GET | `/api/sales/bread/{breadId}` | 특정 빵 판매 내역 |
| GET | `/api/sales/summary` | 빵별 판매 집계 (그래프용) |
| GET | `/api/sales/daily` | 일별 판매 통계 (그래프용) |
| GET | `/api/sales/statistics/today` | 오늘 판매 통계 |
| GET | `/api/sales/statistics/weekly` | 최근 7일 판매 통계 |
| GET | `/api/sales/statistics/monthly` | 최근 30일 판매 통계 |

### 💳 결제 연동 API

| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/payment/complete` | 결제 완료 처리 (재고 차감 + 매출 기록) |
| POST | `/api/payment/cancel/{paymentId}` | 결제 취소 (향후 구현) |

> ⚡ **자동 처리**: 결제가 완료되면 자동으로 재고가 차감되고 매출이 기록됩니다!
>
> 결제 API 담당자는 [PAYMENT_API_GUIDE.md](PAYMENT_API_GUIDE.md)를 참조하여 연동하세요.

자세한 API 명세는 [API_DOCUMENTATION.md](API_DOCUMENTATION.md)를 참조하세요.

---

## 🖥️ 프론트엔드 구조

프로젝트에는 Vite + React 기반의 완전한 웹 애플리케이션이 포함되어 있습니다.

### 주요 페이지

#### 메인 사이트
1. **메인 페이지** (`/`)
   - 빵집 소개 및 브랜딩
   - 결제 시작 버튼
   - 관리자 페이지 진입

2. **이용 가이드** (`/guide`)
   - 서비스 소개
   - 이용 방법 안내

#### 관리자 페이지
1. **대시보드** (`/admin`)
   - 오늘의 매출 및 판매량 실시간 조회
   - 저재고 품목 알림
   - 최근 매출 내역

2. **재고 관리** (`/admin/inventory`)
   - 전체 재고 조회 및 검색
   - 재고 수량 업데이트
   - 저재고 상태 표시

3. **매출 관리** (`/admin/sales`)
   - 매출 등록
   - 일별 매출 조회
   - 판매 내역 관리

4. **통계** (`/admin/statistics`)
   - 기간별 매출 통계
   - 빵별 판매 현황
   - 판매 비율 시각화

### React 컴포넌트 구조

```
frontend/src/
├── components/          # 재사용 가능한 컴포넌트
│   ├── Header.jsx
│   ├── AdminLayout.jsx
│   ├── AdminNavigation.jsx
│   ├── DashboardCard.jsx
│   ├── LoadingSpinner.jsx
│   └── EmptyState.jsx
├── pages/              # 페이지 컴포넌트
│   ├── MainPage.jsx
│   ├── GuidePage.jsx
│   └── admin/
│       ├── AdminDashboard.jsx
│       ├── Inventory.jsx
│       ├── Sales.jsx
│       └── Statistics.jsx
├── services/           # API 서비스
│   └── api.js
├── styles/             # 스타일 파일
│   ├── MainPage.css
│   ├── GuidePage.css
│   └── Admin.css
└── utils/              # 유틸리티 함수
    └── formatters.js
```

자세한 프론트엔드 가이드는 [frontend/README.md](frontend/README.md)를 참조하세요.

---

## 🧪 테스트

### API 테스트 (curl)

```bash
# 1. 전체 재고 조회
curl http://localhost:8080/api/inventory

# 2. 재고 부족 품목 조회
curl http://localhost:8080/api/inventory/low-stock

# 3. 판매 기록 생성
curl -X POST http://localhost:8080/api/sales \
  -H "Content-Type: application/json" \
  -d '{"breadId": 1, "quantity": 5}'

# 4. 최근 7일 판매 통계
curl http://localhost:8080/api/sales/statistics/weekly

# 5. 빵별 판매 집계
curl "http://localhost:8080/api/sales/summary?startDate=2025-10-01T00:00:00&endDate=2025-10-31T23:59:59"
```

---

## 💾 샘플 데이터

프로젝트를 처음 실행하면 자동으로 샘플 데이터가 생성됩니다:

- **빵 데이터**: 8종류 (크루아상, 바게트, 단팥빵, 소금빵, 초코 머핀, 치아바타, 크림빵, 식빵)
- **재고 데이터**: 각 빵별 재고 정보
- **판매 데이터**: 최근 30일간의 판매 기록

실제 운영 시에는 `backend/src/main/java/com/ddbb/config/DataInitializer.java`를 비활성화하거나 삭제하세요.

---

## 📝 주요 클래스 설명

### Entity
- **Bread**: 빵 정보 (이름, 가격, 카테고리 등)
- **Inventory**: 재고 정보 (수량, 최소 재고 수준)
- **Sales**: 판매 기록 (수량, 금액, 판매 시간)

### Service
- **InventoryService**: 재고 관리 비즈니스 로직
  - 재고 조회, 업데이트, 증감 처리
  - 재고 부족 알림
  
- **SalesService**: 판매 관리 비즈니스 로직
  - 판매 기록 생성 (자동 재고 감소)
  - 기간별/빵별 판매 통계
  - 그래프용 데이터 가공

### Controller
- **InventoryController**: 재고 관리 REST API
- **SalesController**: 판매 관리 REST API

---

## 🔧 추가 구현 가능 기능

- [ ] 재고 알림 기능 (이메일/SMS)
- [ ] 엑셀 다운로드 기능
- [ ] 판매 예측 기능 (AI/ML)
- [ ] 실시간 대시보드
- [ ] 재고 자동 발주 시스템

---

## 🤝 팀 구성

이 프로젝트는 **딥딥빵빵** 팀 프로젝트의 일부입니다.

- **사진 촬영**: 다같이
- **모델 학습**: 이경민
- **빵 추천**: 김경진
- **결제**: 김준기
- **재고확인 및 판매량(그래프)**: 고동욱 ⭐
- **프론트**: 이수현

---

## 📧 문의

프로젝트에 대한 문의사항이 있으시면 이슈를 등록해주세요.

---

## 📄 라이선스

이 프로젝트는 교육 목적으로 만들어졌습니다.

