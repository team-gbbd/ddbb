/**
 * 장바구니 아이템 컴포넌트
 */

import { motion } from 'framer-motion'
import { Minus, Plus, Trash2 } from 'lucide-react'
import { CartItem as CartItemType } from '../store/useCartStore'
import { useCartStore } from '../store/useCartStore'

interface CartItemProps {
  item: CartItemType
  index: number
}

export default function CartItem({ item, index }: CartItemProps) {
  const { updateItemCount, removeItem } = useCartStore()

  const handleIncrement = () => {
    updateItemCount(item.breadName, item.count + 1)
  }

  const handleDecrement = () => {
    if (item.count > 1) {
      updateItemCount(item.breadName, item.count - 1)
    } else {
      removeItem(item.breadName)
    }
  }

  const handleRemove = () => {
    removeItem(item.breadName)
  }

  const subtotal = item.unitPrice * item.count

  return (
    <motion.div
      initial={{ opacity: 0, x: 30 }}
      animate={{ opacity: 1, x: 0 }}
      exit={{ opacity: 0, x: -30 }}
      transition={{ delay: index * 0.05 }}
      layout
      className="bg-white rounded-2xl p-5 mb-3.5 shadow-sm border border-gray-200 hover:border-primary-light hover:shadow-md transition-all cursor-pointer group"
      whileHover={{ x: 4, scale: 1.01 }}
      whileTap={{ scale: 0.99 }}
    >
      {/* 상단: 이름 & 수량 */}
      <div className="flex justify-between items-start mb-3">
        <div className="flex items-center gap-3.5 flex-1">
          {/* 아이콘 */}
          <div className="w-12 h-12 bg-gradient-to-br from-primary-light to-surface-secondary rounded-xl flex items-center justify-center text-2xl flex-shrink-0">
            🥖
          </div>

          {/* 정보 */}
          <div className="flex-1">
            <div className="font-bold text-gray-900 text-base leading-tight mb-1">
              {item.koreanName}
            </div>
            <div className="text-sm text-gray-500 font-medium">
              {item.unitPrice.toLocaleString()}원
            </div>
          </div>
        </div>

        {/* 수량 배지 */}
        <div className="bg-gradient-to-r from-primary to-primary-dark text-white px-4 py-2 rounded-full text-base font-bold shadow-sm">
          ×{item.count}
        </div>
      </div>

      {/* 하단: 금액 & 컨트롤 */}
      <div className="flex justify-between items-center pt-3.5 border-t border-gray-100">
        {/* 금액 */}
        <div className="text-xl font-extrabold text-primary-dark tracking-tight">
          {subtotal.toLocaleString()}원
        </div>

        {/* 컨트롤 버튼 */}
        <div className="flex items-center gap-2">
          <motion.button
            onClick={handleDecrement}
            className="w-8 h-8 bg-gray-100 hover:bg-gray-200 rounded-lg flex items-center justify-center transition-colors"
            whileHover={{ scale: 1.1 }}
            whileTap={{ scale: 0.9 }}
          >
            <Minus className="w-4 h-4 text-gray-600" />
          </motion.button>

          <motion.button
            onClick={handleIncrement}
            className="w-8 h-8 bg-primary hover:bg-primary-dark text-white rounded-lg flex items-center justify-center transition-colors"
            whileHover={{ scale: 1.1 }}
            whileTap={{ scale: 0.9 }}
          >
            <Plus className="w-4 h-4" />
          </motion.button>

          <motion.button
            onClick={handleRemove}
            className="w-8 h-8 bg-red-50 hover:bg-red-100 text-red-500 rounded-lg flex items-center justify-center transition-colors ml-1"
            whileHover={{ scale: 1.1 }}
            whileTap={{ scale: 0.9 }}
          >
            <Trash2 className="w-4 h-4" />
          </motion.button>
        </div>
      </div>
    </motion.div>
  )
}
