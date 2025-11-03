/**
 * 카메라/촬영 섹션 컴포넌트
 */

import { useState, useRef } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { Camera, Loader2 } from 'lucide-react'
import { detectBread } from '../api/breadApi'
import { useCartStore } from '../store/useCartStore'
import toast from 'react-hot-toast'

export default function CameraSection() {
  const [isLoading, setIsLoading] = useState(false)
  const [resultImage, setResultImage] = useState<string | null>(null)
  const [uploadedImage, setUploadedImage] = useState<string | null>(null)
  const fileInputRef = useRef<HTMLInputElement>(null)
  const { addItems } = useCartStore()

  const handleFileSelect = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0]
    if (!file) return

    // 업로드된 이미지 미리보기
    const reader = new FileReader()
    reader.onload = (e) => {
      setUploadedImage(e.target?.result as string)
    }
    reader.readAsDataURL(file)

    setIsLoading(true)
    setResultImage(null)

    try {
      const response = await detectBread(file)

      if (response.success && response.items.length > 0) {
        // 장바구니에 추가
        const cartItems = response.items.map((item) => ({
          breadName: item.bread_name,
          koreanName: item.korean_name,
          count: item.count,
          unitPrice: item.unit_price,
          confidence: item.confidence,
        }))

        addItems(cartItems)

        // 결과 이미지 표시
        if (response.image_base64) {
          setResultImage(response.image_base64)
        }

        toast.success(`${response.items.length}종류의 빵이 인식되었습니다!`, {
          icon: '✅',
          duration: 3000,
        })
      } else {
        toast.error(response.message || '빵이 인식되지 않았습니다', {
          icon: '⚠️',
          duration: 4000,
        })
      }
    } catch (error) {
      console.error('Detection error:', error)
      toast.error('이미지 처리 중 오류가 발생했습니다', {
        icon: '❌',
        duration: 4000,
      })
    } finally {
      setIsLoading(false)
      // 파일 input 초기화 (같은 파일 재선택 가능하도록)
      if (fileInputRef.current) {
        fileInputRef.current.value = ''
      }
    }
  }

  const handleButtonClick = () => {
    fileInputRef.current?.click()
  }

  return (
    <motion.section
      className="bg-white rounded-3xl p-7 shadow-md hover:shadow-lg transition-shadow"
      initial={{ opacity: 0, x: -50 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ delay: 0.1 }}
    >
      {/* 섹션 헤더 */}
      <div className="flex items-center gap-3 mb-6 pb-5 border-b-2 border-gray-100">
        <Camera className="w-8 h-8 text-primary" strokeWidth={2.5} />
        <div>
          <h2 className="text-2xl font-extrabold text-gray-900 tracking-tight">
            제품 촬영
          </h2>
          <p className="text-sm text-gray-500 font-medium mt-0.5">
            빵을 카메라에 담아주세요
          </p>
        </div>
      </div>

      {/* 이미지 표시 영역 */}
      <div className="mb-6 relative">
        <AnimatePresence mode="wait">
          {resultImage || uploadedImage ? (
            <motion.div
              key="image"
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              exit={{ opacity: 0, scale: 0.9 }}
              className="relative rounded-2xl overflow-hidden bg-gray-100"
              style={{ height: '380px' }}
            >
              <img
                src={resultImage || uploadedImage || ''}
                alt="Detection result"
                className="w-full h-full object-contain"
              />
              {isLoading && (
                <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
                  <Loader2 className="w-12 h-12 text-white animate-spin" />
                </div>
              )}
            </motion.div>
          ) : (
            <motion.div
              key="placeholder"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="rounded-2xl bg-gradient-to-br from-gray-50 to-gray-100 flex items-center justify-center"
              style={{ height: '380px' }}
            >
              <div className="text-center">
                <Camera className="w-20 h-20 text-gray-300 mx-auto mb-4" />
                <p className="text-gray-400 font-medium">
                  아래 버튼을 눌러 빵을 촬영하세요
                </p>
              </div>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      {/* 촬영 버튼 */}
      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        capture="environment"
        onChange={handleFileSelect}
        className="hidden"
      />

      <motion.button
        onClick={handleButtonClick}
        disabled={isLoading}
        className="w-full bg-gradient-to-r from-primary to-primary-dark text-white font-bold text-xl py-5 px-8 rounded-2xl shadow-lg hover:shadow-xl transition-all disabled:opacity-50 disabled:cursor-not-allowed relative overflow-hidden"
        whileHover={{ scale: isLoading ? 1 : 1.02, y: isLoading ? 0 : -2 }}
        whileTap={{ scale: isLoading ? 1 : 0.98 }}
      >
        <span className="relative z-10 flex items-center justify-center gap-3">
          {isLoading ? (
            <>
              <Loader2 className="w-6 h-6 animate-spin" />
              인식 중...
            </>
          ) : (
            <>
              <Camera className="w-6 h-6" />
              빵 촬영하기
            </>
          )}
        </span>

        {/* Shimmer effect */}
        <motion.div
          className="absolute inset-0 bg-gradient-to-r from-transparent via-white/30 to-transparent"
          initial={{ x: '-100%' }}
          whileHover={{ x: '100%' }}
          transition={{ duration: 0.6 }}
        />
      </motion.button>
    </motion.section>
  )
}
