# Roboflow 라벨링 가이드

> Python 3.12 환경 기준

## 1단계: Roboflow 계정 생성

1. https://roboflow.com 접속
2. **Sign Up** (무료 계정)
3. 이메일 인증

## 2단계: 프로젝트 생성

1. **Create New Project** 클릭
2. 설정:
   - **Project Name**: `bread-detection`
   - **Project Type**: `Object Detection`
   - **What will your model predict?**: `breads`
   - **License**: `CC BY 4.0` (공개 프로젝트)
3. **Create Project** 클릭

## 3단계: 이미지 업로드

### 업로드 방법
```
1. 좌측 메뉴에서 "Upload" 클릭
2. "Upload Images" 버튼 클릭
3. data/raw/ 폴더에서 전체 300장 선택
4. Upload 완료 대기 (5-10분)
```

### 업로드 팁
- 한 번에 모든 이미지 업로드 가능 (300장)
- 와이파이 연결 확인 (용량: 약 500MB - 1GB)
- 업로드 중 브라우저 끄지 말기

## 4단계: 라벨링 (Annotation)

### 라벨링 시작
```
1. 좌측 메뉴 "Annotate" 클릭
2. 첫 번째 이미지 선택
3. 빵에 박스 그리기 (Bounding Box)
```

### 박스 그리는 방법
```
1. 빵의 왼쪽 위 모서리 클릭
2. 드래그해서 오른쪽 아래 모서리까지
3. 빵 전체가 박스 안에 들어오도록
4. 라벨 선택 (예: soboro_bread)
```

### 라벨 목록 (10종)
```
1. soboro_bread      (소보로빵)
2. red_bean_bread    (단팥빵)
3. cream_bread       (크림빵)
4. butter_roll       (버터롤)
5. croissant         (크로와상)
6. salt_bread        (소금빵)
7. mocha_bread       (모카빵)
8. white_bread       (식빵)
9. baguette          (바게트)
10. bagel            (베이글)
```

### 라벨링 팁
- **박스는 빵에 딱 맞게**: 너무 크거나 작으면 안 됨
- **한 이미지에 여러 빵이 있으면**: 각각 박스 그리기
- **빵이 잘리거나 일부만 보이면**: 보이는 부분만 박스
- **단축키**:
  - `A`: 이전 이미지
  - `D`: 다음 이미지
  - `Space`: 박스 그리기 시작/끝
  - `Delete`: 선택한 박스 삭제

### 예상 소요 시간
```
300장 × 30초 = 150분 (약 2.5시간)

팁: 하루에 100장씩 나눠서 하면 편해요!
- Day 1: 100장 (50분)
- Day 2: 100장 (50분)
- Day 3: 100장 (50분)
```

## 5단계: Generate (증강 + 데이터셋 생성)

### Generate 시작
```
1. 좌측 메뉴 "Generate" 클릭
2. "Create new version" 클릭
```

### Preprocessing (전처리)
```
✅ Auto-Orient: ON (자동 방향 조정)
✅ Resize: Stretch to 640x640 (YOLO 표준)
❌ 나머지는 모두 OFF
```

### Augmentation (증강)
```
✅ Flip: Horizontal (좌우 반전)
✅ Rotation: Between -15° and +15°
✅ Brightness: Between -15% and +15%
✅ Blur: Up to 1.5px

⚠️ Augmentation 배수: 3x
   (300장 → 900장으로 증강)
```

### Train/Valid/Test Split
```
Train: 70% (630장)
Valid: 20% (180장)
Test: 10% (90장)
```

### Generate 실행
```
1. 설정 확인
2. "Continue" 클릭
3. "Generate" 클릭
4. 완료 대기 (5-10분)
```

## 6단계: Export (YOLOv8 형식)

### Export 방법
```
1. Generate 완료 후 "Export Dataset" 클릭
2. Format: "YOLOv8" 선택
3. "Continue" 클릭
4. "show download code" 클릭
```

### Roboflow API Key 복사
```
다음 형식의 코드가 보입니다:

!pip install roboflow

from roboflow import Roboflow
rf = Roboflow(api_key="YOUR_API_KEY_HERE")
project = rf.workspace("your-workspace").project("bread-detection")
dataset = project.version(1).download("yolov8")
```

**⚠️ 이 코드를 복사해두세요!** (Google Colab에서 사용)

## 7단계: Google Colab으로 이동

이제 `COLAB_TRAINING_GUIDE.md` 파일을 참고하여 Google Colab에서 학습을 진행합니다.

## 문제 해결 (Troubleshooting)

### Q1: 라벨링이 너무 오래 걸려요
**A**: 한 번에 다 하지 말고 하루에 100장씩 나눠서 하세요!

### Q2: 박스를 잘못 그렸어요
**A**: 박스 선택 후 `Delete` 키 또는 휴지통 아이콘 클릭

### Q3: 라벨 이름을 잘못 입력했어요
**A**: 박스 클릭 → 라벨 변경 가능

### Q4: 증강(Augmentation)을 너무 많이 하면?
**A**: 3x가 적당합니다. 너무 많으면 오히려 성능 저하

### Q5: Export 코드를 잃어버렸어요
**A**: Dataset 페이지 → Export → Show Download Code

## 다음 단계

✅ Roboflow 라벨링 완료
→ 📘 `COLAB_TRAINING_GUIDE.md` 참고하여 Google Colab에서 YOLOv8 학습

## 참고 자료
- Roboflow Docs: https://docs.roboflow.com
- YOLOv8 Docs: https://docs.ultralytics.com
