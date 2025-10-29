# 딤딤빵빵 Backend API

Spring Boot 기반의 빵집 재고관리 및 판매량 분석 백엔드 서버입니다.

## 🛠 기술 스택

- **Language**: Java 17
- **Framework**: Spring Boot 3.5.7
- **Database**: MySQL 8.0
- **ORM**: Spring Data JPA
- **Build Tool**: Gradle
- **기타**: Lombok, Spring Security

## 🚀 실행 방법

### 1. 데이터베이스 설정

```sql
CREATE DATABASE ddbb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. application.properties 수정

`src/main/resources/application.properties`:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/ddbb
spring.datasource.username=your_username
spring.datasource.password=your_password

# OpenAI API 키 (AI 분석 기능 사용 시)
openai.api.key=your_openai_api_key
```

### 3. 서버 실행

```bash
# Windows
gradlew.bat bootRun

# Linux/Mac
./gradlew bootRun
```

서버가 `http://localhost:8080`에서 실행됩니다.

## 📁 프로젝트 구조

```
src/main/java/com/ddbb/
├── config/              # 설정 클래스
│   ├── SecurityConfig.java
│   ├── CorsConfig.java
│   └── DataInitializer.java
├── controller/          # REST API 컨트롤러
│   ├── management/
│   │   ├── InventoryController.java
│   │   ├── SalesController.java
│   │   ├── BreadController.java
│   │   └── AIAnalysisController.java
│   └── payment/
│       └── PaymentController.java
├── dto/                 # 데이터 전송 객체
├── entity/              # JPA 엔티티
│   ├── Bread.java
│   ├── Inventory.java
│   └── Sales.java
├── exception/           # 예외 처리
├── repository/          # 데이터 접근 계층
├── service/             # 비즈니스 로직
│   ├── management/
│   └── payment/
└── DdbbApplication.java # 메인 애플리케이션
```

## 🔌 주요 API 엔드포인트

### 재고 관리
- `GET /api/inventory` - 전체 재고 조회
- `GET /api/inventory/low-stock` - 저재고 품목 조회
- `PUT /api/inventory/bread/{breadId}` - 재고 업데이트

### 판매 관리
- `POST /api/sales` - 판매 기록 생성
- `GET /api/sales` - 판매 내역 조회
- `GET /api/sales/statistics/today` - 오늘 판매 통계

### 빵 관리
- `GET /api/breads` - 전체 빵 목록
- `POST /api/breads` - 빵 등록

### AI 분석
- `POST /api/ai/analyze` - AI 분석 실행
- `GET /api/ai/quick-analysis` - 빠른 분석 (최근 30일)

자세한 API 문서는 상위 디렉토리의 `API_DOCUMENTATION.md`를 참조하세요.

## 💾 샘플 데이터

`DataInitializer.java`에서 자동으로 샘플 데이터를 생성합니다:
- 빵 데이터 8종
- 재고 데이터
- 최근 30일 판매 데이터

프로덕션 환경에서는 이 파일을 비활성화하세요.

