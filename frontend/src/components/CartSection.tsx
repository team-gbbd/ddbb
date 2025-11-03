/**
 * 장바구니 섹션 컴포넌트
 */

import { motion, AnimatePresence } from 'framer-motion'
import { ShoppingCart } from 'lucide-react'
import { useCartStore } from '../store/useCartStore'
import CartItem from './CartItem'

export default function CartSection() {
  const { items } = useCartStore()

  return (
    <motion.section
      className="bg-surface-secondary rounded-3xl p-7 shadow-md flex flex-col"
      initial={{ opacity: 0, x: 50 }}
      animate={{ opacity: 1, x: 0 }}
      transition={{ delay: 0.2 }}
    >
      {/* 섹션 헤더 */}
      <div className="flex items-center justify-between mb-6 pb-5 border-b-2 border-primary-light">
        <div className="flex items-center gap-3">
          <ShoppingCart className="w-8 h-8 text-primary" strokeWidth={2.5} />
          <div>
            <h2 className="text-2xl font-extrabold text-gray-900 tracking-tight">
              장바구니
            </h2>
            <p className="text-sm text-gray-500 font-medium mt-0.5">
              선택된 제품 목록
            </p>
          </div>
        </div>
      </div>

      {/* 장바구니 리스트 */}
      <div className="flex-1 overflow-y-auto pr-3" style={{ maxHeight: '500px' }}>
        <AnimatePresence mode="popLayout">
          {items.length === 0 ? (
            <motion.div
              key="empty"
              initial={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              exit={{ opacity: 0 }}
              className="flex flex-col items-center justify-center h-full py-20 text-center"
            >
              {/* SVG 장바구니 아이콘 */}
              <motion.svg
                width="120"
                height="120"
                viewBox="0 0 120 120"
                fill="none"
                className="mb-6"
                animate={{ y: [0, -10, 0] }}
                transition={{ repeat: Infinity, duration: 3, ease: 'easeInOut' }}
              >
                <circle cx="60" cy="60" r="60" fill="#F8F9FA" />
                <path
                  d="M40 45L50 35L70 35L80 45L75 75H45L40 45Z"
                  stroke="#D4A574"
                  strokeWidth="3"
                  fill="none"
                />
                <circle cx="50" cy="82" r="4" fill="#D4A574" />
                <circle cx="70" cy="82" r="4" fill="#D4A574" />
              </motion.svg>

              <h3 className="text-xl font-bold text-gray-900 mb-3">
                장바구니가 비어있습니다
              </h3>
              <p className="text-sm text-gray-500 leading-relaxed">
                왼쪽 촬영 버튼을 눌러<br />
                빵을 인식시켜 주세요
              </p>
            </motion.div>
          ) : (
            items.map((item, index) => (
              <CartItem key={item.breadName} item={item} index={index} />
            ))
          )}
        </AnimatePresence>
      </div>
    </motion.section>
  )
}
