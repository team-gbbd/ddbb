# 🥖 DDBB Bakery POS - 프로젝트 구조

## 📁 디렉토리 설명

### ✅ 사용 중 (Main Project)

```
fullstack-pos/              # 🚀 메인 프로젝트 (FastAPI + React)
├── backend/               # Python FastAPI 백엔드
│   ├── main.py           # API 엔드포인트
│   ├── models.py         # YOLO 모델
│   ├── best.pt           # AI 모델 파일
│   └── venv/             # Python 가상환경
└── frontend/             # React TypeScript 프론트엔드
    ├── src/              # 소스 코드
    ├── node_modules/     # npm 패키지
    └── package.json      # 의존성

data/                      # 📊 학습 데이터
├── train/                # 학습용 이미지
├── valid/                # 검증용 이미지
└── README.md             # 데이터셋 설명
```

### ⚠️ 삭제됨 (Cleaned Up)

```
✅ gradio-demo/            # Gradio 버전 (삭제됨 - fullstack-pos로 대체)
✅ .idea/                  # IntelliJ 설정 (삭제됨)
✅ venv/ (in Git)          # 가상환경 (.gitignore 처리)
✅ Java/Gradle 파일        # 초기 Spring Boot 파일들 (삭제됨)
```

### 📝 문서

```
COLAB_TRAINING_GUIDE.md    # Colab 학습 가이드
ROBOFLOW_GUIDE.md          # Roboflow 데이터셋 가이드
README.md                  # 프로젝트 메인 설명 (작성 필요)
```

---

## 🚀 실행 방법

```bash
# 백엔드
cd fullstack-pos/backend
source venv/bin/activate
python3 main.py

# 프론트엔드 (새 터미널)
cd fullstack-pos/frontend
npm install
npm run dev
```

---

## 🧹 정리 완료

### ✅ 삭제된 항목들
- `gradio-demo/` - fullstack-pos로 대체
- `.idea/` - IntelliJ 설정
- `venv/` (Git 히스토리에서) - .gitignore 처리
- Java/Gradle 관련 파일들 - 초기 Spring Boot 잔재

### ✅ 유지 중인 항목들
- `fullstack-pos/` - 메인 프로젝트
- `data/` - 학습 데이터
- `docs/` - 문서
- `.git/` - Git 히스토리
- `.gitignore`, `.gitattributes` - Git 설정

---

## 📊 프로젝트 히스토리

1. **Spring Boot 프로젝트** (초기, 삭제됨)
   - Java 기반, Gradle 빌드 시스템
   - Python 기반으로 전환하며 삭제

2. **Gradio 버전** (중간, 삭제됨)
   - Python 올인원 프로토타입
   - FastAPI + React로 발전하며 삭제

3. **FastAPI + React** (현재)
   - 프로덕션 레벨 풀스택 아키텍처
   - 메인 프로젝트로 확정

---

## 🎯 최종 구조 (정리 완료)

```
ddbb/
├── fullstack-pos/        # 메인 프로젝트 (FastAPI + React)
│   ├── backend/          # Python FastAPI
│   └── frontend/         # React TypeScript
├── data/                 # 학습 데이터
├── docs/                 # 문서
│   ├── COLAB_TRAINING_GUIDE.md
│   └── ROBOFLOW_GUIDE.md
├── PROJECT_STRUCTURE.md  # 프로젝트 구조 설명
├── README.md             # 프로젝트 메인 설명
├── .git/                 # Git
├── .gitignore
└── .gitattributes
```
