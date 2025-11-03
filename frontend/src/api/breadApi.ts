/**
 * API 클라이언트 (Axios)
 */

import axios from 'axios'

const API_BASE_URL = 'http://localhost:8000'

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
})

export interface DetectionResult {
  bread_name: string
  korean_name: string
  count: number
  unit_price: number
  confidence: number
}

export interface DetectionResponse {
  success: boolean
  items: DetectionResult[]
  total_count: number
  total_price: number
  image_base64?: string
  message?: string
}

export interface CheckoutResponse {
  success: boolean
  total_price: number
  total_count: number
  receipt_number: string
  timestamp: string
  message: string
}

/**
 * 빵 인식 API 호출
 */
export const detectBread = async (file: File): Promise<DetectionResponse> => {
  const formData = new FormData()
  formData.append('file', file)

  const response = await api.post<DetectionResponse>('/api/detect', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  })

  return response.data
}

/**
 * 결제 API 호출
 */
export const checkout = async (items: Array<{ bread_name: string; count: number }>): Promise<CheckoutResponse> => {
  const response = await api.post<CheckoutResponse>('/api/checkout', {
    items,
  })

  return response.data
}

/**
 * 헬스 체크
 */
export const healthCheck = async () => {
  const response = await api.get('/')
  return response.data
}

export default api
