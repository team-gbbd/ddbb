/**
 * 장바구니 상태 관리 (Zustand)
 */

import { create } from 'zustand'

export interface CartItem {
  breadName: string
  koreanName: string
  count: number
  unitPrice: number
  confidence?: number
}

interface CartStore {
  items: CartItem[]
  totalPrice: number
  totalCount: number

  // Actions
  addItems: (newItems: CartItem[]) => void
  updateItemCount: (breadName: string, count: number) => void
  removeItem: (breadName: string) => void
  clearCart: () => void
  calculateTotals: () => void
}

export const useCartStore = create<CartStore>((set, get) => ({
  items: [],
  totalPrice: 0,
  totalCount: 0,

  addItems: (newItems) => {
    const currentItems = get().items
    const updatedItems = [...currentItems]

    newItems.forEach((newItem) => {
      const existingIndex = updatedItems.findIndex(
        (item) => item.breadName === newItem.breadName
      )

      if (existingIndex >= 0) {
        // 이미 존재하는 아이템이면 수량 증가
        updatedItems[existingIndex].count += newItem.count
      } else {
        // 새로운 아이템 추가
        updatedItems.push(newItem)
      }
    })

    set({ items: updatedItems })
    get().calculateTotals()
  },

  updateItemCount: (breadName, count) => {
    const items = get().items.map((item) =>
      item.breadName === breadName ? { ...item, count } : item
    )
    set({ items })
    get().calculateTotals()
  },

  removeItem: (breadName) => {
    const items = get().items.filter((item) => item.breadName !== breadName)
    set({ items })
    get().calculateTotals()
  },

  clearCart: () => {
    set({ items: [], totalPrice: 0, totalCount: 0 })
  },

  calculateTotals: () => {
    const items = get().items
    const totalPrice = items.reduce(
      (sum, item) => sum + item.unitPrice * item.count,
      0
    )
    const totalCount = items.reduce((sum, item) => sum + item.count, 0)
    set({ totalPrice, totalCount })
  },
}))
