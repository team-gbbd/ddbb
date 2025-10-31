/**
 * 결제 패널 컴포넌트
 */

import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { CreditCard, CheckCircle, RefreshCw, Loader2 } from 'lucide-react'
import { useCartStore } from '../store/useCartStore'
import { checkout } from '../api/breadApi'
import toast from 'react-hot-toast'

export default function PaymentPanel() {
  const { items, totalPrice, totalCount, clearCart } = useCartStore()
  const [isProcessing, setIsProcessing] = useState(false)
  const [isSuccess, setIsSuccess] = useState(false)

  const handleCheckout = async () => {
    if (items.length === 0) {
      toast.error('장바구니가 비어있습니다', { icon: '⚠️' })
      return
    }

    setIsProcessing(true)

    try {
      const checkoutItems = items.map((item) => ({
        bread_name: item.breadName,
        count: item.count,
      }))

      const response = await checkout(checkoutItems)

      if (response.success) {
        // 성공 애니메이션
        setIsSuccess(true)

        // 2초 후 초기화
        setTimeout(() => {
          clearCart()
          setIsSuccess(false)
        }, 3000)

        toast.success(`결제 완료!\n영수증 번호: ${response.receipt_number}`, {
          icon: '✅',
          duration: 4000,
        })
      }
    } catch (error) {
      console.error('Checkout error:', error)
      toast.error('결제 처리 중 오류가 발생했습니다', { icon: '❌' })
    } finally {
      setIsProcessing(false)
    }
  }

  const handleReset = () => {
    clearCart()
    toast.success('장바구니가 초기화되었습니다', { icon: '🔄' })
  }

  return (
    <motion.div
      className="bg-white border-t border-gray-200 shadow-lg backdrop-blur-lg"
      initial={{ y: 100 }}
      animate={{ y: 0 }}
      transition={{ type: 'spring', stiffness: 100 }}
    >
      <div className="px-10 py-7 grid grid-cols-[1.5fr,1fr] gap-6">
        {/* 왼쪽: 합계 정보 */}
        <div>
          {items.length === 0 ? (
            <div className="bg-gray-50 rounded-2xl p-8 text-center border-2 border-dashed border-gray-200">
              <svg
                width="48"
                height="48"
                viewBox="0 0 48 48"
                fill="none"
                className="mx-auto mb-4"
              >
                <circle cx="24" cy="24" r="24" fill="#F0F0F0" />
                <text x="24" y="30" textAnchor="middle" fontSize="20" fill="#999">
                  ₩
                </text>
              </svg>
              <p className="text-gray-500 font-medium">
                상품을 추가하면 금액이 계산됩니다
              </p>
            </div>
          ) : (
            <div className="bg-white rounded-2xl p-7 shadow-md border border-gray-200">
              {/* 총 수량 */}
              <div className="flex justify-between items-center mb-5">
                <span className="text-gray-600 font-semibold flex items-center gap-2">
                  <svg
                    width="20"
                    height="20"
                    viewBox="0 0 20 20"
                    fill="none"
                    className="inline"
                  >
                    <rect
                      x="3"
                      y="3"
                      width="14"
                      height="14"
                      rx="2"
                      stroke="#7f8c8d"
                      strokeWidth="1.5"
                      fill="none"
                    />
                    <path
                      d="M7 10L9 12L13 8"
                      stroke="#7f8c8d"
                      strokeWidth="1.5"
                      strokeLinecap="round"
                    />
                  </svg>
                  총 수량
                </span>
                <span className="text-xl font-bold text-gray-900">
                  {totalCount}개
                </span>
              </div>

              {/* 구분선 */}
              <div className="h-px bg-gradient-to-r from-transparent via-gray-300 to-transparent my-5" />

              {/* 총 결제금액 */}
              <div className="flex justify-between items-center">
                <span className="text-xl font-bold text-gray-900">
                  총 결제금액
                </span>
                <div className="flex items-baseline gap-1.5">
                  <span className="text-4xl font-black bg-gradient-to-r from-primary to-primary-dark bg-clip-text text-transparent">
                    {totalPrice.toLocaleString()}
                  </span>
                  <span className="text-xl font-bold text-gray-500">원</span>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* 오른쪽: 버튼 */}
        <div className="flex flex-col gap-3.5">
          {/* 결제하기 버튼 */}
          <motion.button
            onClick={handleCheckout}
            disabled={items.length === 0 || isProcessing}
            className="flex-1 bg-gradient-to-r from-primary to-primary-dark text-white font-bold text-2xl py-6 px-8 rounded-2xl shadow-lg hover:shadow-xl transition-all disabled:opacity-50 disabled:cursor-not-allowed relative overflow-hidden"
            whileHover={{ scale: items.length > 0 && !isProcessing ? 1.02 : 1, y: items.length > 0 && !isProcessing ? -2 : 0 }}
            whileTap={{ scale: items.length > 0 && !isProcessing ? 0.98 : 1 }}
          >
            <span className="relative z-10 flex items-center justify-center gap-3">
              <AnimatePresence mode="wait">
                {isProcessing ? (
                  <motion.div
                    key="processing"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="flex items-center gap-3"
                  >
                    <Loader2 className="w-6 h-6 animate-spin" />
                    처리 중...
                  </motion.div>
                ) : isSuccess ? (
                  <motion.div
                    key="success"
                    initial={{ opacity: 0, scale: 0.8 }}
                    animate={{ opacity: 1, scale: 1 }}
                    exit={{ opacity: 0 }}
                    className="flex items-center gap-3"
                  >
                    <CheckCircle className="w-6 h-6" />
                    결제 완료!
                  </motion.div>
                ) : (
                  <motion.div
                    key="idle"
                    initial={{ opacity: 0 }}
                    animate={{ opacity: 1 }}
                    exit={{ opacity: 0 }}
                    className="flex items-center gap-3"
                  >
                    <CreditCard className="w-6 h-6" />
                    결제하기
                  </motion.div>
                )}
              </AnimatePresence>
            </span>

            {/* Shimmer effect */}
            {items.length > 0 && !isProcessing && (
              <motion.div
                className="absolute inset-0 bg-gradient-to-r from-transparent via-white/30 to-transparent"
                initial={{ x: '-100%' }}
                whileHover={{ x: '100%' }}
                transition={{ duration: 0.6 }}
              />
            )}
          </motion.button>

          {/* 초기화 버튼 */}
          <motion.button
            onClick={handleReset}
            disabled={items.length === 0}
            className="bg-white text-gray-600 font-semibold text-base py-4 px-8 rounded-2xl border-2 border-gray-200 hover:border-primary-light hover:bg-surface-secondary hover:text-primary-dark transition-all disabled:opacity-50 disabled:cursor-not-allowed"
            whileHover={{ scale: items.length > 0 ? 1.02 : 1, y: items.length > 0 ? -1 : 0 }}
            whileTap={{ scale: items.length > 0 ? 0.98 : 1 }}
          >
            <RefreshCw className="w-4 h-4 inline mr-2" />
            초기화
          </motion.button>
        </div>
      </div>
    </motion.div>
  )
}
