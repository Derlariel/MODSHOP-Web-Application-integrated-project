import { defineStore } from "pinia"
import { ref, computed, watch } from "vue"

export const useCartStore = defineStore("cart", () => {
  const cart = ref(JSON.parse(localStorage.getItem("cart") || "[]"))

  // persist ลง localStorage
  watch(cart, (val) => {
    localStorage.setItem("cart", JSON.stringify(val))
  }, { deep: true })

  const addToCart =(newItem, qty = 1) => {
    const existing = cart.value.find(
      i => i.saleItemId === newItem.saleItemId && i.sellerId === newItem.sellerId
    )
    if (existing) {
      existing.quantity = Math.min(existing.quantity + qty, existing.stock)
    } else {
      cart.value.push({
        ...newItem,
        quantity: Math.min(qty, newItem.stock)
      })
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

  const clearCart = () => {
    cart.value = []
  }

  const totalItems = computed(() =>
    cart.value.reduce((sum, i) => sum + i.quantity, 0)
  )
  const totalPrice = computed(() =>
    cart.value.reduce((sum, i) => sum + i.quantity * i.price, 0)
  )

  return { cart, addToCart, updateQuantity, clearCart , totalItems, totalPrice }
})
