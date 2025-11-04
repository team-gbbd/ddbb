# 딥딥빵빵 (DDBB)

AI 빵 스캔과 스마트한 재고/매출 관리를 제공하는 베이커리 POS 시스템입니다.

## 프로젝트 소개

빵집에서 빵을 카메라로 찍으면 자동으로 인식해서 장바구니에 담아주고, 실시간으로 재고와 매출을 관리할 수 있는 시스템입니다.
YOLO v8 기반 객체 인식 모델과 OpenAI API를 활용한 인사이트 제공이 특징입니다.

### 팀 구성
- 사진 촬영 및 데이터 구축: 전체
- AI 모델 학습 (YOLO v8): 이경민
- AI 인사이트 (OpenAI): 김경진
- 결제 시스템 (PortOne): 김준기
- 재고/매출 관리: 고동욱
- 프론트엔드: 이수현

## 기술 스택

**Backend**
- Java 17 + Spring Boot 3.5.7
- MySQL 8.0 + Spring Data JPA
- OpenAI API (GPT-4o-mini)

**AI Scanner**
- Python 3.8+ FastAPI
- YOLO v8 (ultralytics)
- OpenCV

**Frontend**
- React 19 + Vite
- TypeScript + Tailwind CSS
- Zustand (상태관리)
- Chart.js (데이터 시각화)

## 주요 기능

### 1. AI 빵 스캔
카메라로 빵을 촬영하면 YOLO v8 모델이 자동으로 빵을 인식하고 장바구니에 추가합니다.
- 학습 데이터: 빵 7종 × 100장 → Roboflow 증강 → 2,100장
- 정확도: mAP 99.5%
- 추론 시간: 2-4초

### 2. AI 대시보드
OpenAI GPT-4o-mini 기반으로 실시간 경영 인사이트를 제공합니다.
- 오늘의 베이커리 무드 (날씨 연동)
- 실시간 판매 현황 브리핑
- 재고 알림 (긴급/과잉 재고 감지)
- 구체적인 발주/할인 전략 제안

### 3. AI 분석
통계 알고리즘 기반 판매량/수익 예측 기능입니다.
- 요일 패턴 분석 (주말 1.43배 효과)
- 지수 이동평균 (EMA) + 선형 회귀
- 향후 7일 판매량/수익 예측
- 빵별 판매 비교 및 성장세 분석

### 4. 결제
PortOne SDK를 통한 카드 결제 연동
- 결제 완료 시 자동 재고 차감
- 매출 데이터 자동 기록

### 5. 재고/매출 관리
- 실시간 재고 모니터링
- 기간별 매출 통계
- 일/주/월별 판매 추이
- 저재고 알림

## 시작하기

### 필요한 것
- Java 17
- MySQL 8.0
- Python 3.8+
- Node.js 16+

### 1. 데이터베이스 설정

```bash
cd backend
mysql -u root -p < setting.sql
```

또는 직접 생성:
```sql
CREATE DATABASE ddbb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 환경변수 설정

`backend/src/main/resources/application-local.properties` 파일 생성:

```properties
PORTONE_API_SECRET=your_portone_secret
OPENAI_API_KEY=your_openai_key
```

### 3. 서버 실행

**Backend (Spring Boot)**
```bash
cd backend
./gradlew bootRun
# http://localhost:8080
```

**AI Scanner (FastAPI)**
```bash
cd ai-scanner
python -m venv venv
source venv/bin/activate  # Windows: venv\Scripts\activate
pip install -r requirements.txt
python main.py
# http://localhost:8000
```

**Frontend (React)**
```bash
cd frontend
npm install
npm run dev
# http://localhost:5173
```

## API 엔드포인트

### 빵 스캔
- `POST /detect` - 이미지 업로드 및 빵 인식

### 재고 관리
- `GET /api/inventory` - 전체 재고 조회
- `GET /api/inventory/low-stock` - 저재고 품목
- `PUT /api/inventory/bread/{id}` - 재고 수정

### 판매/매출
- `POST /api/sales` - 판매 기록
- `GET /api/sales/summary` - 기간별 매출 집계
- `GET /api/sales/statistics/today` - 오늘 판매 통계

### AI 대시보드
- `GET /api/dashboard/insights` - AI 인사이트 생성
- `GET /api/dashboard/charts` - 차트 데이터

### AI 분석
- `POST /api/ai-analysis/analyze` - 기간별 예측 분석

### 결제
- `POST /api/payment/complete` - 결제 완료 처리

자세한 내용은 각 폴더의 README 참고

## 프로젝트 구조

```
ddbb/
├── backend/              # Spring Boot 백엔드
│   ├── src/main/java/com/ddbb/
│   │   ├── controller/   # REST API
│   │   ├── service/      # 비즈니스 로직
│   │   ├── repository/   # DB 접근
│   │   └── entity/       # JPA 엔티티
│   └── setting.sql       # DB 초기화
├── ai-scanner/           # Python AI 스캔 서버
│   ├── main.py           # FastAPI 서버
│   ├── models.py         # YOLO 추론
│   └── best.pt           # 학습된 모델 (21MB)
└── frontend/             # React 프론트엔드
    ├── src/pages/        # 페이지 컴포넌트
    ├── src/components/   # 재사용 컴포넌트
    └── src/api/          # API 호출
```

## 학습 과정

1. **데이터 수집**: 빵 7종 각 100장 촬영 (총 700장)
2. **Roboflow 처리**: 라벨링 + 증강 → 2,100장
3. **Google Colab 학습**: YOLO v8, T4 GPU, 50 epochs
4. **결과**: mAP 99.5% 달성

자세한 학습 가이드는 `docs/COLAB_TRAINING_GUIDE.md` 참고

