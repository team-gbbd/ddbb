"""
DDBB Bakery POS - FastAPI Backend
AI-Powered Bread Recognition System
"""

from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from fastapi.responses import JSONResponse
from pydantic import BaseModel
from typing import Dict, List, Optional
import uvicorn
import numpy as np
import cv2
from PIL import Image
import io
import base64
from datetime import datetime

from models import BreadDetector, PRICES, KOREAN_NAMES

# FastAPI 앱 초기화
app = FastAPI(
    title="DDBB Bakery POS API",
    description="AI 기반 빵 인식 POS 시스템",
    version="2.0.0"
)

# CORS 설정 (프론트엔드 연결)
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:5173", "http://localhost:3000"],  # Vite, React 기본 포트
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# YOLO 모델 로드 (confidence threshold를 50%로 낮춤)
detector = BreadDetector(model_path="best.pt", confidence_threshold=0.50)


# Pydantic 모델 (요청/응답 스키마)
class DetectionResult(BaseModel):
    bread_name: str
    korean_name: str
    count: int
    unit_price: int
    confidence: float


class DetectionResponse(BaseModel):
    success: bool
    items: List[DetectionResult]
    total_count: int
    total_price: int
    image_base64: Optional[str] = None
    message: Optional[str] = None


class CartItem(BaseModel):
    bread_name: str
    count: int


class CheckoutRequest(BaseModel):
    items: List[CartItem]


class CheckoutResponse(BaseModel):
    success: bool
    total_price: int
    total_count: int
    receipt_number: str
    timestamp: str
    message: str


# API 엔드포인트

@app.get("/")
async def root():
    """헬스 체크"""
    return {
        "status": "healthy",
        "service": "DDBB Bakery POS API",
        "version": "2.0.0",
        "model_loaded": detector.model is not None
    }


@app.post("/api/detect", response_model=DetectionResponse)
async def detect_bread(file: UploadFile = File(...)):
    """
    빵 인식 API
    - 이미지 업로드
    - YOLO 추론
    - 결과 반환 (JSON)
    """
    try:
        # 이미지 읽기
        contents = await file.read()
        image = Image.open(io.BytesIO(contents))
        image_np = np.array(image.convert('RGB'))

        # BGR 변환 (OpenCV 형식)
        image_bgr = cv2.cvtColor(image_np, cv2.COLOR_RGB2BGR)

        # YOLO 추론
        result_image, detections = detector.detect(image_bgr)

        # 결과가 없는 경우
        if not detections:
            return DetectionResponse(
                success=False,
                items=[],
                total_count=0,
                total_price=0,
                message="빵이 인식되지 않았습니다. 더 가까이 촬영해주세요."
            )

        # 결과 변환
        items = []
        total_count = 0
        total_price = 0

        for bread_name, data in detections.items():
            count = data['count']
            confidence = data['confidence']
            unit_price = PRICES.get(bread_name, 0)

            items.append(DetectionResult(
                bread_name=bread_name,
                korean_name=KOREAN_NAMES.get(bread_name, bread_name),
                count=count,
                unit_price=unit_price,
                confidence=confidence
            ))

            total_count += count
            total_price += unit_price * count

        # 결과 이미지를 Base64로 인코딩
        _, buffer = cv2.imencode('.jpg', result_image)
        image_base64 = base64.b64encode(buffer).decode('utf-8')

        return DetectionResponse(
            success=True,
            items=items,
            total_count=total_count,
            total_price=total_price,
            image_base64=f"data:image/jpeg;base64,{image_base64}",
            message="인식 완료!"
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"처리 중 오류 발생: {str(e)}")


@app.post("/api/checkout", response_model=CheckoutResponse)
async def checkout(request: CheckoutRequest):
    """
    결제 처리 API
    - 장바구니 데이터 수신
    - 결제 처리
    - 영수증 번호 발급
    """
    try:
        total_price = 0
        total_count = 0

        for item in request.items:
            unit_price = PRICES.get(item.bread_name, 0)
            total_price += unit_price * item.count
            total_count += item.count

        # 영수증 번호 생성
        now = datetime.now()
        receipt_number = f"DDBB{now.strftime('%Y%m%d%H%M%S')}"

        return CheckoutResponse(
            success=True,
            total_price=total_price,
            total_count=total_count,
            receipt_number=receipt_number,
            timestamp=now.isoformat(),
            message="결제가 완료되었습니다!"
        )

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"결제 처리 중 오류 발생: {str(e)}")


@app.get("/api/prices")
async def get_prices():
    """가격표 조회"""
    return {
        "prices": {
            KOREAN_NAMES[k]: v for k, v in PRICES.items()
        }
    }


if __name__ == "__main__":
    uvicorn.run(
        "main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    )
