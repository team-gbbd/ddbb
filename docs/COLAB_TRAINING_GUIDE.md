# Google Colab YOLOv8 학습 가이드

> Python 3.12 환경 기준

## 사전 준비

✅ Roboflow 라벨링 완료
✅ Roboflow Export 코드 복사
✅ Google 계정 (Gmail)

## 1단계: Google Colab 접속

1. https://colab.research.google.com 접속
2. **New Notebook** 클릭
3. 노트북 이름 변경: `DDBB_Bread_Training.ipynb`

## 2단계: GPU 설정

### GPU 활성화 (무료)
```
1. 상단 메뉴: Runtime → Change runtime type
2. Hardware accelerator: "T4 GPU" 선택
3. Save 클릭
```

### GPU 확인
새 셀에 입력 후 실행 (`Shift + Enter`):
```python
!nvidia-smi
```

출력에 `Tesla T4` 또는 GPU 정보가 보이면 성공!

## 3단계: 라이브러리 설치

### 셀 1: Ultralytics 설치
```python
# YOLOv8 설치 (Python 3.12 호환)
!pip install ultralytics==8.0.0
```

### 셀 2: Roboflow 설치
```python
# Roboflow 설치
!pip install roboflow
```

## 4단계: 데이터셋 다운로드

### 셀 3: Roboflow에서 데이터셋 가져오기
```python
from roboflow import Roboflow

# ⚠️ 여기에 Roboflow에서 복사한 API Key 입력
rf = Roboflow(api_key="YOUR_API_KEY_HERE")

# ⚠️ workspace와 project 이름을 Roboflow에서 복사한 것으로 변경
project = rf.workspace("your-workspace").project("bread-detection")

# 버전 1 다운로드 (YOLOv8 형식)
dataset = project.version(1).download("yolov8")

print(f"✅ 데이터셋 다운로드 완료: {dataset.location}")
```

**⚠️ 주의**: `YOUR_API_KEY_HERE`, `your-workspace`, `bread-detection`을 실제 값으로 변경하세요!

## 5단계: 데이터셋 확인

### 셀 4: 데이터셋 구조 확인
```python
import os

dataset_path = dataset.location

print("📁 데이터셋 구조:")
print(f"Train 이미지: {len(os.listdir(f'{dataset_path}/train/images'))}장")
print(f"Valid 이미지: {len(os.listdir(f'{dataset_path}/valid/images'))}장")
print(f"Test 이미지: {len(os.listdir(f'{dataset_path}/test/images'))}장")

print(f"\n📄 data.yaml 위치: {dataset_path}/data.yaml")
```

### 셀 5: data.yaml 확인
```python
# YAML 파일 내용 출력
with open(f'{dataset_path}/data.yaml', 'r') as f:
    print(f.read())
```

출력 예시:
```yaml
train: /content/bread-detection-1/train/images
val: /content/bread-detection-1/valid/images
test: /content/bread-detection-1/test/images

nc: 10
names: ['soboro_bread', 'red_bean_bread', 'cream_bread', 'butter_roll',
        'croissant', 'salt_bread', 'mocha_bread', 'white_bread',
        'baguette', 'bagel']
```

## 6단계: YOLOv8 학습

### 셀 6: 학습 시작
```python
from ultralytics import YOLO

# YOLOv8 Small 모델 로드 (사전 학습된 가중치)
model = YOLO('yolov8s.pt')

# 학습 시작
results = model.train(
    data=f'{dataset_path}/data.yaml',  # 데이터셋 경로
    epochs=50,                          # 학습 횟수 (50번)
    imgsz=640,                          # 이미지 크기
    batch=16,                           # 배치 크기
    name='ddbb_bread_model',            # 프로젝트 이름
    patience=10,                        # Early stopping
    save=True,                          # 모델 저장
    plots=True                          # 학습 결과 그래프
)

print("🎉 학습 완료!")
```

### 학습 시간
```
데이터셋 크기: 900장 (증강 후)
Epochs: 50
예상 시간: 30-60분 (GPU T4 기준)
```

## 7단계: 학습 결과 확인

### 셀 7: 학습 결과 그래프
```python
from IPython.display import Image, display

# 학습 곡선 (Loss, Precision, Recall, mAP)
display(Image('/content/runs/detect/ddbb_bread_model/results.png'))
```

### 셀 8: 검증 결과 이미지
```python
# 검증 이미지에서 예측 결과
display(Image('/content/runs/detect/ddbb_bread_model/val_batch0_pred.jpg'))
```

### 셀 9: Confusion Matrix
```python
# Confusion Matrix (어떤 빵을 잘못 예측했는지)
display(Image('/content/runs/detect/ddbb_bread_model/confusion_matrix.png'))
```

## 8단계: 모델 평가 (Test)

### 셀 10: 테스트 데이터로 평가
```python
# 학습된 모델 로드
model = YOLO('/content/runs/detect/ddbb_bread_model/weights/best.pt')

# 테스트 데이터셋 평가
metrics = model.val(data=f'{dataset_path}/data.yaml', split='test')

print(f"📊 mAP50: {metrics.box.map50:.3f}")
print(f"📊 mAP50-95: {metrics.box.map:.3f}")
print(f"📊 Precision: {metrics.box.mp:.3f}")
print(f"📊 Recall: {metrics.box.mr:.3f}")
```

### 목표 성능
```
✅ mAP50 > 0.85 (85%)
✅ mAP50-95 > 0.65 (65%)
✅ Precision > 0.80 (80%)
✅ Recall > 0.80 (80%)
```

## 9단계: 실제 이미지로 테스트

### 셀 11: 테스트 이미지 예측
```python
# 테스트 이미지 경로
test_image = f'{dataset_path}/test/images/soboro_bread_001.jpg'

# 예측
results = model.predict(test_image, save=True, conf=0.5)

# 결과 출력
display(Image(results[0].path))

# 감지된 객체
for box in results[0].boxes:
    class_name = model.names[int(box.cls)]
    confidence = float(box.conf)
    print(f"✅ {class_name}: {confidence:.2f}")
```

## 10단계: 모델 다운로드 (best.pt)

### 방법 1: 직접 다운로드
```python
from google.colab import files

# best.pt 파일 다운로드
files.download('/content/runs/detect/ddbb_bread_model/weights/best.pt')
```

### 방법 2: Google Drive 저장
```python
# Google Drive 마운트
from google.colab import drive
drive.mount('/content/drive')

# Google Drive에 복사
!cp /content/runs/detect/ddbb_bread_model/weights/best.pt /content/drive/MyDrive/

print("✅ Google Drive에 저장 완료!")
```

## 11단계: 팀원에게 전달

### 전달할 파일
```
1. best.pt (학습된 모델 파일, 약 20-30MB)
2. confusion_matrix.png (성능 확인용)
3. results.png (학습 곡선)
```

### 전달 방법
```
- Google Drive 공유 링크
- 카카오톡 파일 전송
- USB
```

### 팀원에게 전달할 내용
```
안녕하세요! 빵 인식 모델 학습 완료했습니다.

📦 파일: best.pt (모델 파일)
📊 성능:
  - mAP50: 0.XX
  - Precision: 0.XX
  - Recall: 0.XX

사용법:
python-api/best.pt 위치에 이 파일을 넣으시면 됩니다!
```

## 문제 해결 (Troubleshooting)

### Q1: GPU가 할당되지 않아요
**A**: Runtime → Change runtime type → T4 GPU 선택 후 재시작

### Q2: Colab 세션이 끊겼어요
**A**:
- Colab 무료 버전은 최대 12시간 사용 가능
- 90분 동안 활동 없으면 자동 종료
- 학습 중에는 탭을 닫지 마세요!

### Q3: 데이터셋 다운로드가 안 돼요
**A**:
- API Key가 정확한지 확인
- Roboflow workspace/project 이름 확인
- 인터넷 연결 확인

### Q4: 학습 중 에러가 발생했어요
**A**:
```python
# 로그 확인
!cat /content/runs/detect/ddbb_bread_model/train.log
```

### Q5: mAP가 너무 낮아요 (< 0.70)
**A**:
- Epochs를 50 → 100으로 증가
- Augmentation 다시 확인
- 라벨링이 정확한지 재확인

### Q6: 학습이 너무 느려요
**A**:
- GPU가 활성화되었는지 확인 (`!nvidia-smi`)
- batch size를 16 → 8로 감소

## Python 3.12 관련 참고사항

Google Colab은 기본적으로 Python 3.10을 사용합니다.
하지만 Ultralytics와 Roboflow는 Python 3.10/3.11/3.12 모두 지원하므로 문제없습니다.

팀원들이 로컬에서 Python API를 실행할 때만 Python 3.12를 사용하면 됩니다.

## 전체 코드 요약

전체 코드를 한 번에 실행하려면 새 노트북에 다음 셀들을 순서대로 입력하세요:

```python
# [셀 1] 라이브러리 설치
!pip install ultralytics==8.0.0 roboflow

# [셀 2] GPU 확인
!nvidia-smi

# [셀 3] 데이터셋 다운로드
from roboflow import Roboflow
rf = Roboflow(api_key="YOUR_API_KEY_HERE")
project = rf.workspace("your-workspace").project("bread-detection")
dataset = project.version(1).download("yolov8")

# [셀 4] 학습
from ultralytics import YOLO
model = YOLO('yolov8s.pt')
results = model.train(
    data=f'{dataset.location}/data.yaml',
    epochs=50,
    imgsz=640,
    batch=16,
    name='ddbb_bread_model',
    patience=10,
    save=True,
    plots=True
)

# [셀 5] 평가
model = YOLO('/content/runs/detect/ddbb_bread_model/weights/best.pt')
metrics = model.val(data=f'{dataset.location}/data.yaml', split='test')
print(f"mAP50: {metrics.box.map50:.3f}")

# [셀 6] 다운로드
from google.colab import files
files.download('/content/runs/detect/ddbb_bread_model/weights/best.pt')
```

## 다음 단계

✅ Google Colab 학습 완료
→ 📦 best.pt 파일을 팀원에게 전달
→ 🚀 팀원이 Python API에 통합
→ 🎯 전체 시스템 테스트

## 참고 자료
- Ultralytics YOLOv8 Docs: https://docs.ultralytics.com
- Google Colab Tips: https://colab.research.google.com/notebooks/basic_features_overview.ipynb
