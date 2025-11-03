# 🥖 DDBB Bakery POS - 프로젝트 구조

## 📁 최종 디렉토리 구조

```
ddbb/
├── backend/               # ☕ Java Spring Boot (관리, 결제, 대시보드)
│   ├── src/main/java/    # Java 소스 코드
│   ├── build.gradle      # Gradle 빌드 설정
│   └── application.properties
├── ai-scanner/            # 🤖 Python FastAPI (빵 인식 AI)
│   ├── main.py           # FastAPI 서버
│   ├── models.py         # YOLO 모델
│   ├── best.pt           # 학습된 모델 파일
│   └── requirements.txt  # Python 의존성
├── frontend/              # ⚛️ React (통합 UI)
│   ├── src/              # React 소스 코드
│   ├── package.json      # npm 의존성
│   └── vite.config.ts    # Vite 설정
├── data/                  # 📊 AI 학습 데이터
└── docs/                  # 📝 문서
```

## 🎯 각 폴더의 역할

### backend/ (Java Spring Boot)
- **담당**: 팀원들
- **기능**:
  - 빵 관리 (CRUD)
  - 재고 관리
  - 매출 통계
  - AI 분석 요청 처리
  - 결제 시스템
  - 대시보드 API
- **포트**: 8080 (기본)
- **실행**: `./gradlew bootRun`

### ai-scanner/ (Python FastAPI)
- **담당**: 경민
- **기능**:
  - YOLOv8 빵 인식 AI
  - 이미지 전처리
  - 빵 종류 감지 및 개수 계산
- **포트**: 8000
- **실행**: `python3 main.py`
- **모델**: best.pt (mAP 99.5%)

### frontend/ (React + TypeScript)
- **담당**: 경민 (빵 스캔 UI) + 팀원들 (관리 UI)
- **기능**:
  - 빵 스캔 인터페이스
  - 장바구니
  - 관리자 대시보드
- **포트**: 5173 (Vite dev server)
- **실행**: `npm run dev`

## 🚀 실행 방법

### 1. Java 백엔드 실행
```bash
cd backend
./gradlew bootRun
# → http://localhost:8080
```

### 2. AI 스캐너 실행
```bash
cd ai-scanner
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python3 main.py
# → http://localhost:8000
```

### 3. 프론트엔드 실행
```bash
cd frontend
npm install
npm run dev
# → http://localhost:5173
```

## 📊 프로젝트 히스토리

1. **Spring Boot 시작** (초기)
   - Java 기반 백엔드 구축

2. **Gradio 프로토타입** (중간, 삭제됨)
   - 빠른 AI 테스트용
   - fullstack으로 발전하며 삭제

3. **Full-stack 분리** (현재)
   - Java 백엔드 + Python AI + React 프론트엔드
   - 마이크로서비스 아키텍처 지향

## 🔄 Git 브랜치 전략

- `main` - 프로덕션 브랜치
- `kyungmin` - 경민 작업 브랜치 (AI 스캐너 + 빵 스캔 UI)
- `dwdw` - 팀원 브랜치
- `kimjungi` - 팀원 브랜치

## 📦 주요 의존성

### Backend (Java)
- Spring Boot 3.x
- Spring Data JPA
- H2/MySQL Database

### AI Scanner (Python)
- FastAPI
- Ultralytics (YOLOv8)
- OpenCV
- PyTorch

### Frontend (React)
- React 18
- TypeScript
- Vite
- Tailwind CSS
- Zustand (상태 관리)
- Framer Motion (애니메이션)

## 🎓 신규 팀원 온보딩

1. 저장소 클론: `git clone https://github.com/team-gbbd/ddbb.git`
2. 각 폴더별 README 참고
3. 로컬 개발 환경 구축
4. 자신의 브랜치에서 작업 시작

---

**Last Updated**: 2024.10.31
