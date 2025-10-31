/**
 * 메인 App 컴포넌트
 */

import { Toaster } from 'react-hot-toast'
import Header from './components/Header'
import CameraSection from './components/CameraSection'
import CartSection from './components/CartSection'
import PaymentPanel from './components/PaymentPanel'

export default function App() {
  return (
    <div className="min-h-screen bg-background">
      {/* Toast 알림 */}
      <Toaster
        position="top-center"
        toastOptions={{
          duration: 3000,
          style: {
            background: '#fff',
            color: '#1A1A1A',
            fontWeight: '600',
            padding: '16px 24px',
            borderRadius: '12px',
            boxShadow: '0 4px 12px rgba(0,0,0,0.15)',
          },
          success: {
            iconTheme: {
              primary: '#10B981',
              secondary: '#fff',
            },
          },
          error: {
            iconTheme: {
              primary: '#EF4444',
              secondary: '#fff',
            },
          },
        }}
      />

      {/* 헤더 */}
      <Header />

      {/* 메인 컨텐츠 */}
      <main className="px-10 py-8 grid grid-cols-[1.5fr,1fr] gap-6 min-h-[calc(100vh-200px)]">
        {/* 왼쪽: 카메라 섹션 */}
        <CameraSection />

        {/* 오른쪽: 장바구니 섹션 */}
        <CartSection />
      </main>

      {/* 하단: 결제 패널 */}
      <PaymentPanel />
    </div>
  )
}
