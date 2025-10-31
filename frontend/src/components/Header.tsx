/**
 * 헤더 컴포넌트
 */

import { motion } from 'framer-motion'

export default function Header() {
  const today = new Date().toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
  }).replace(/\. /g, '.').replace(/\.$/, '')

  return (
    <motion.header
      className="bg-white shadow-sm sticky top-0 z-50 backdrop-blur-lg"
      initial={{ y: -100 }}
      animate={{ y: 0 }}
      transition={{ type: 'spring', stiffness: 100 }}
    >
      <div className="px-10 py-7 flex justify-between items-center">
        {/* 로고 */}
        <div className="flex items-center gap-5">
          <motion.div
            className="w-14 h-14 bg-gradient-to-br from-primary to-primary-dark rounded-2xl flex items-center justify-center text-3xl shadow-lg"
            whileHover={{ scale: 1.05, rotate: 5 }}
            whileTap={{ scale: 0.95 }}
          >
            🥖
          </motion.div>
          <div>
            <h1 className="text-3xl font-extrabold text-gray-900 tracking-tight">
              DDBB Bakery
            </h1>
            <p className="text-sm text-gray-500 font-semibold tracking-wide">
              AI-Powered Smart POS System
            </p>
          </div>
        </div>

        {/* 날짜 */}
        <div className="bg-gray-50 px-5 py-3 rounded-xl border border-gray-200">
          <div className="text-xs text-gray-500 font-semibold mb-1">OPEN</div>
          <div className="text-base text-gray-900 font-bold">{today}</div>
        </div>
      </div>
    </motion.header>
  )
}
