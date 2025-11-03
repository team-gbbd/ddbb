"""
YOLO 모델 및 데이터 정의
"""

import os
import sys

# PyTorch 2.6+ 호환: torch.load 함수 패치
import torch
_original_load = torch.load

def _patched_load(*args, **kwargs):
    # weights_only를 False로 강제 설정
    kwargs['weights_only'] = False
    return _original_load(*args, **kwargs)

# torch.load 함수를 패치된 버전으로 교체
torch.load = _patched_load

from ultralytics import YOLO
import numpy as np
import cv2

# 빵 가격표
PRICES = {
    'croissant': 3200,
    'salt_bread': 2800,
    'cookie': 4200,
    'eggmayo': 4500,
    'muffin': 4500,
    'pie': 4700,
    'twisted_bread': 3500
}

# 빵 이름 한글 변환
KOREAN_NAMES = {
    'croissant': '오리지널크라상',
    'salt_bread': '소금버터롤',
    'cookie': '다크초코피넛버터쿠키',
    'eggmayo': '에그마요소금버터롤',
    'muffin': '초코청크머핀',
    'pie': '호두파이(조각)',
    'twisted_bread': '츄러스꽈배기'
}


class BreadDetector:
    """빵 인식 클래스"""

    def __init__(self, model_path: str = 'best.pt', confidence_threshold: float = 0.70):
        self.model_path = model_path
        self.confidence_threshold = confidence_threshold
        self.model = None

        # 모델 로드
        if os.path.exists(model_path):
            self.model = YOLO(model_path)
            print(f"✅ 모델 로드 성공: {model_path}")

            # 모델 워밍업
            try:
                dummy_frame = np.zeros((640, 640, 3), dtype=np.uint8)
                self.model(dummy_frame, imgsz=640, verbose=False)
                print("✅ 모델 워밍업 완료!")
            except:
                pass
        else:
            print(f"⚠️ 모델 파일을 찾을 수 없습니다: {model_path}")

    def detect(self, image: np.ndarray):
        """
        빵 인식 수행

        Args:
            image: OpenCV 이미지 (BGR)

        Returns:
            result_image: 바운딩 박스가 그려진 이미지
            detections: 인식된 빵 정보 딕셔너리
        """
        if self.model is None:
            return image, {}

        # 이미지 전처리 (리사이즈)
        h, w = image.shape[:2]
        if max(h, w) > 1280:
            scale = 1280 / max(h, w)
            new_w = int(w * scale)
            new_h = int(h * scale)
            image = cv2.resize(image, (new_w, new_h), interpolation=cv2.INTER_AREA)

        # YOLO 추론
        results = self.model(
            image,
            imgsz=640,
            conf=0.1,  # 낮은 threshold로 모든 후보 탐지
            iou=0.45,
            verbose=False,
            augment=True
        )

        # Confidence 필터링
        filtered_boxes = []
        for box in results[0].boxes:
            if float(box.conf) >= self.confidence_threshold:
                filtered_boxes.append(box)

        # 바운딩 박스가 그려진 이미지
        result_image = results[0].plot()

        # 빵 카운트 및 confidence 저장
        detections = {}
        for box in filtered_boxes:
            class_id = int(box.cls)
            class_name = self.model.names[class_id]
            confidence = float(box.conf)

            if class_name not in detections:
                detections[class_name] = {
                    'count': 0,
                    'confidence': 0.0
                }

            detections[class_name]['count'] += 1
            # 평균 confidence 계산
            detections[class_name]['confidence'] = (
                (detections[class_name]['confidence'] + confidence) / 2
                if detections[class_name]['confidence'] > 0
                else confidence
            )

        return result_image, detections
