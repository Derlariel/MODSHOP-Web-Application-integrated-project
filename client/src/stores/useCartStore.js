import { defineStore } from "pinia"
import { ref, computed, watch } from "vue"

export const useCartStore = defineStore("cart", () => {
  const cart = ref(JSON.parse(localStorage.getItem("cart") || "[]"))

  // persist ลง localStorage
  watch(cart, (val) => {
    localStorage.setItem("cart", JSON.stringify(val))
  }, { deep: true })

  const addToCart = (newItem, qty = 1) => {
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
