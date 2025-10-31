# 🥖 DDBB Bakery POS System

AI 기반 실시간 빵 인식 POS 시스템

---

## 🎯 프로젝트 개요

- **목적**: YOLOv8을 활용한 빵 자동 인식 및 계산 시스템
- **인식 가능 빵**: 7종류 (크라상, 소금버터롤, 쿠키, 에그마요, 머핀, 파이, 꽈배기)
- **정확도**: mAP 99.5% (학습), 실사용 80%+
- **처리 속도**: ~200ms

---

## 📁 프로젝트 구조

```
ddbb/
├── fullstack-pos/        # ⭐ 메인: FastAPI + React (프로덕션 레벨)
│   ├── backend/          # Python FastAPI
│   └── frontend/         # React TypeScript
├── data/                 # 📊 학습 데이터
└── docs/                 # 📝 문서
```

---

## 🚀 빠른 시작

**1. 백엔드 실행**
```bash
cd fullstack-pos/backend
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python3 main.py
# → http://localhost:8000
```

**2. 프론트엔드 실행 (새 터미널)**
```bash
cd fullstack-pos/frontend
npm install
npm run dev
# → http://localhost:5173
```

---

## 🛠️ 기술 스택

- **프론트엔드**: React 18, TypeScript, Vite, Tailwind CSS, Framer Motion, Zustand
- **백엔드**: FastAPI, Uvicorn, Pydantic
- **AI**: YOLOv8, PyTorch, OpenCV

---

## 🔧 중요 설정

### PyTorch 2.6+ 호환성
`backend/models.py` 파일 상단에 다음 코드 필수:
```python
import torch
_original_load = torch.load
def _patched_load(*args, **kwargs):
    kwargs['weights_only'] = False
    return _original_load(*args, **kwargs)
torch.load = _patched_load
```

### Confidence Threshold
- 기본값: 70% (`confidence_threshold=0.70`)
- 조정: `backend/main.py`에서 `BreadDetector(confidence_threshold=0.50)` 수정

---

## 📊 성능

- **학습 정확도**: mAP50 99.5%
- **실사용 정확도**: 74-85%
- **추론 속도**: ~200ms
- **이미지 전처리**: 최대 1280x1280 리사이즈

---

## 🎨 주요 기능

- ✅ 실시간 빵 인식
- ✅ 인터랙티브 장바구니 (수량 조절, 삭제)
- ✅ 프로덕션 레벨 UI/UX (Framer Motion)
- ✅ Toast 알림 (React Hot Toast)
- ✅ 완벽한 반응형 디자인

---

## 📝 API 엔드포인트

### POST `/api/detect`
빵 이미지 인식
```bash
curl -X POST http://localhost:8000/api/detect \
  -F "file=@bread.jpg"
```

### POST `/api/checkout`
결제 처리
```json
{
  "items": [
    {"bread_name": "croissant", "count": 2}
  ]
}
```

---

## 🐛 알려진 이슈

1. **PyTorch 2.6 weights_only 에러**
   - 해결: `models.py`에서 `torch.load` 패치 (이미 적용됨)

2. **이미지 크기 불일치로 정확도 저하**
   - 해결: 1280x1280 리사이즈 적용 (이미 적용됨)

3. **iPhone 프레임 렉**
   - 해결: Axios FormData 업로드 방식 사용

---

## 📖 문서

- [프로젝트 구조 상세](./PROJECT_STRUCTURE.md)
- [Colab 학습 가이드](./docs/COLAB_TRAINING_GUIDE.md)
- [Roboflow 데이터 가이드](./docs/ROBOFLOW_GUIDE.md)

---

## 🔄 버전 히스토리

- **v1.0**: Gradio 프로토타입 (2024.10)
- **v2.0**: FastAPI + React 풀스택 전환 (2024.10.30)

---

## 📦 빵 가격표

| 빵 이름 | 영문명 | 가격 |
|---------|--------|------|
| 오리지널크라상 | croissant | 3,200원 |
| 소금버터롤 | salt_bread | 2,800원 |
| 다크초코피넛버터쿠키 | cookie | 4,200원 |
| 에그마요소금버터롤 | eggmayo | 4,500원 |
| 초코청크머핀 | muffin | 4,500원 |
| 호두파이(조각) | pie | 4,700원 |
| 츄러스꽈배기 | twisted_bread | 3,500원 |

---

## 🎓 새 채팅 시작 시 필수 정보

**작업 중인 프로젝트**: DDBB Bakery POS
**위치**: `/Users/kyungmin/Downloads/ddbb/`
**메인 프로젝트**: `fullstack-pos/` (FastAPI + React)
**모델**: `best.pt` (YOLOv8, mAP 99.5%)

**현재 실행 중**:
- 백엔드: `fullstack-pos/backend/main.py` (port 8000)
- 프론트엔드: `fullstack-pos/frontend/` (port 5173)

**주요 이슈 해결됨**:
- ✅ PyTorch 2.6 호환성 (torch.load 패치)
- ✅ 이미지 리사이즈 (정확도 개선)
- ✅ Confidence threshold 조정

**다음 개선 사항**:
- Docker 컨테이너화
- CI/CD 파이프라인
- 실시간 통계 대시보드
