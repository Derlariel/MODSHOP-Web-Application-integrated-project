import { defineStore } from "pinia"
import { ref, computed, watch } from "vue"
import { useAuthStore } from "@/stores/useAuthStore"

export const useCartStore = defineStore("cart", () => {
  const auth = useAuthStore()

  const cartKey = computed(() => {
    const uid = auth?.user?.id
    return auth.isAuthenticated && uid != null ? `cart:${uid}` : null
  })

  const cart = ref([])
  
  // persist ลง localStorage เฉพาะผู้ใช้ที่ล็อกอินแล้ว
  const loadCartForCurrentUser = () => {
    const key = cartKey.value
    if (key) {
      try {
        const raw = localStorage.getItem(key)
        cart.value = raw ? JSON.parse(raw) : []
      } catch {
        cart.value = []
      }
    } else {
      cart.value = []
    }
  }
  loadCartForCurrentUser()

  try { localStorage.removeItem("cart") } catch {}

  watch(cartKey, () => {
    loadCartForCurrentUser()
  })
  watch(
    cart,
    (val) => {
      const key = cartKey.value
      if (key) {
        localStorage.setItem(key, JSON.stringify(val))
      }
    },
    { deep: true }
  )

  const addToCart = (newItem, qty = 1) => {
    if (!cartKey.value) return false
    if (!newItem) return false
    const stock = Number(newItem.stock ?? 0)
    const addQty = Number(qty ?? 0)
    if (stock <= 0 || addQty <= 0) {
      return false
    }

    const existing = cart.value.find(
      i => i.saleItemId === newItem.saleItemId && i.sellerId === newItem.sellerId
    )
    if (existing) {
      if (existing.quantity >= existing.stock) return false
      existing.quantity = Math.min(existing.quantity + addQty, existing.stock)
      return true
    } else {
      const finalQty = Math.min(addQty, stock)
      if (finalQty <= 0) return false
      cart.value.push({
        ...newItem,
        quantity: finalQty
      })
      return true
    }
  }

  const  updateQuantity = (saleItemId, sellerId, qty) => {
    const item = cart.value.find(
      i => i.saleItemId === saleItemId && i.sellerId === sellerId
    )
    if (item) {
      if (qty <= 0) {
        if (confirm("Do you want to remove the sale item from the cart?")) {
          cart.value = cart.value.filter(
            i => !(i.saleItemId === saleItemId && i.sellerId === sellerId)
          )
        }
      } else {
        item.quantity = Math.min(qty, item.stock)
      }
    }
  }

  const removeItem = (saleItemId, sellerId) => {
    cart.value = cart.value.filter(
      i => !(i.saleItemId === saleItemId && i.sellerId === sellerId)
    )
  }

  const clearCart = () => {
    cart.value = []
  }

  const removeItemsByKeys = (keys) => {
    if (!Array.isArray(keys) || keys.length === 0) return
    const set = new Set(keys)
    cart.value = cart.value.filter(i => !set.has(i.saleItemId + "-" + i.sellerId))
  }

  const totalItems = computed(() =>
    cart.value.reduce((sum, i) => sum + i.quantity, 0)
  )
  const totalPrice = computed(() =>
    cart.value.reduce((sum, i) => sum + i.quantity * i.price, 0)
  )

  return { cart, addToCart, updateQuantity, removeItem, clearCart, removeItemsByKeys, totalItems, totalPrice }
})
