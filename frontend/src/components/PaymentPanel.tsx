/**
 * 결제 패널 컴포넌트
 */

import { useState } from 'react'
import { motion, AnimatePresence } from 'framer-motion'
import { CreditCard, CheckCircle, RefreshCw, Loader2 } from 'lucide-react'
import { useCartStore } from '../store/useCartStore'
import { checkout } from '../api/breadApi'
import axios from 'axios'
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
      // 포트원 결제 초기화
      const { IMP } = window as any
      if (!IMP) {
        throw new Error('포트원 SDK가 로드되지 않았습니다')
      }

      // 포트원 가맹점 식별코드 (실제 가맹점 코드로 변경 필요)
      IMP.init('imp10391932') // 테스트 가맹점 코드

      // 주문번호 생성
      const merchantUid = `order_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`

      // 포트원 결제 요청
      IMP.request_pay(
        {
          pg: 'kakaopay.TC0ONETIME', // 카카오페이
          pay_method: 'card',
          merchant_uid: merchantUid,
          name: `빵 ${totalCount}개`,
          amount: totalPrice,
          buyer_email: 'customer@ddbb.com',
          buyer_name: '고객',
          buyer_tel: '010-0000-0000',
        },
        async (response: any) => {
          if (response.success) {
            // 결제 성공 시 백엔드에 결제 정보 저장
            try {
              const checkoutItems = items.map((item) => ({
                bread_name: item.breadName,
                count: item.count,
              }))

              // 1. AI Scanner에 영수증 번호 요청
              console.log('🔹 Step 1: AI Scanner 영수증 요청')
              const checkoutResponse = await checkout(checkoutItems)
              console.log('✅ AI Scanner 응답:', checkoutResponse)

              // 2. Java Backend에서 빵 이름으로 ID 조회 및 판매 데이터 저장
              console.log('🔹 Step 2: Java Backend 빵 목록 조회')
              const breadsResponse = await axios.get('/api/breads')
              console.log('✅ 빵 목록:', breadsResponse.data)

              const breadsMap = new Map(
                breadsResponse.data.map((bread: any) => [bread.name, bread.id])
              )
              console.log('📋 빵 이름 → ID 매핑:', Array.from(breadsMap.entries()))

              // 각 빵마다 개별 판매 기록 생성
              console.log('🔹 Step 3: 판매 데이터 저장 시작')
              const salesPromises = items.map(async (item) => {
                const breadId = breadsMap.get(item.koreanName)
                console.log(`  - ${item.koreanName} → breadId: ${breadId}`)

                if (!breadId) {
                  console.warn(`❌ 빵을 찾을 수 없습니다: ${item.koreanName}`)
                  console.warn(`   사용 가능한 빵 목록:`, Array.from(breadsMap.keys()))
                  return null
                }

                console.log(`  → POST /api/sales:`, { breadId, quantity: item.count })
                const salesResponse = await axios.post('/api/sales', {
                  breadId: breadId,
                  quantity: item.count,
                })
                console.log(`  ✅ 저장 성공:`, salesResponse.data)
                return salesResponse
              })

              const results = await Promise.all(salesPromises.filter(p => p !== null))
              console.log('✅ Step 3 완료: 모든 판매 데이터 저장됨', results)

              // 성공 애니메이션
              setIsSuccess(true)

              // 3초 후 초기화
              setTimeout(() => {
                clearCart()
                setIsSuccess(false)
              }, 3000)

              toast.success(
                `결제 완료!\n영수증 번호: ${checkoutResponse.receipt_number}\n결제 금액: ${totalPrice.toLocaleString()}원`,
                {
                  icon: '✅',
                  duration: 5000,
                }
              )
            } catch (error) {
              console.error('Checkout save error:', error)
              toast.error('결제는 완료되었으나 저장에 실패했습니다', { icon: '⚠️' })
            }
          } else {
            // 결제 실패
            toast.error(`결제 실패: ${response.error_msg}`, { icon: '❌' })
          }
          setIsProcessing(false)
        }
      )
    } catch (error) {
      console.error('Payment error:', error)
      toast.error('결제 처리 중 오류가 발생했습니다', { icon: '❌' })
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
