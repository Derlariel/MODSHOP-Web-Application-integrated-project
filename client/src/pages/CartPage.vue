<script setup>
import { useCartStore } from "@/stores/useCartStore"
import BaseInput from "@/components/shared/BaseInput.vue"
import ConfirmModal from "@/components/shared/modal/ConfirmModal.vue"
import Card from "@/components/UI/cart/Card.vue"
import CardHeader from "@/components/UI/cart/CardHeader.vue"
import CardContent from "@/components/UI/cart/CardContent.vue"
import CardTitle from "@/components/UI/cart/CartTitle.vue"
import { computed, ref, onMounted } from "vue"
import { createOrder, getOrders } from "@/api/orderAPI"
import { useAuthStore } from "@/stores/useAuthStore"
import { useOrderStore } from "@/stores/useOrderStore"
import { useSellerOrdersStore } from "@/stores/useSellerOrdersStore"
import ErrorModal from "@/components/shared/modal/ErrorModal.vue"
import SuccessModal from "@/components/shared/modal/SuccessModal.vue"

const cart = useCartStore()
const auth = useAuthStore()
const orderStore = useOrderStore()
const sellerOrdersStore = useSellerOrdersStore()

const shippingAddress = ref("")
const orderNote = ref("")
const isDefaultAddress = ref(false)

const buyerName = computed(() => {
  const u = auth.user || {}
  return (u.fullName && String(u.fullName).trim()) || ""
})

// Fetch previous order data on mount
onMounted(async () => {
  if (auth.isAuthenticated && auth.user?.id) {
    try {
      const response = await getOrders(auth.user.id, 0, 1, "orderDate,desc")
      const orders = response?.data?.content || []
      
      if (orders.length > 0) {
        const lastOrder = orders[0]
        if (lastOrder.shippingAddress) {
          shippingAddress.value = lastOrder.shippingAddress
        }
        if (lastOrder.orderNote) {
          orderNote.value = lastOrder.orderNote
        }
      }
      
      // If no previous address found, use mock address
      if (!shippingAddress.value || !shippingAddress.value.trim()) {
        shippingAddress.value = "123 Mock Street, Subdistrict, District, Province, 10100"
        isDefaultAddress.value = true
      }
    } catch (error) {
      console.error("Failed to fetch previous order:", error)
      // Use mock address on error
      shippingAddress.value = "123 Mock Street, Subdistrict, District, Province, 10100"
      isDefaultAddress.value = true
    }
  }
})

const shipToPreview = computed(() => {
  const name = buyerName.value
  const addr = (shippingAddress.value || "").trim()
  if (!name && !addr) return ""
  return addr ? `${name}, ${addr}` : name
})

// group cart by seller
const groupedCart = computed(() =>
  cart.cart.reduce((acc, item) => {
    const key = String(item.sellerId)
    if (!acc[key]) acc[key] = []
    acc[key].push(item)
    return acc
  }, {})
)

const showConfirm = ref(false)
const pendingRemove = ref(null)

const selectedItems = ref(new Set()) // keys: `${saleItemId}-${sellerId}`
const allSelected = computed({
  get: () => cart.cart.length > 0 && selectedItems.value.size === cart.cart.length,
  set: (val) => {
    if (val) {
      selectedItems.value = new Set(cart.cart.map(i => i.saleItemId + "-" + i.sellerId))
    } else {
      selectedItems.value.clear()
    }
  }
})

function toggleItem(item) {
  const key = item.saleItemId + "-" + item.sellerId
  if (selectedItems.value.has(key)) {
    selectedItems.value.delete(key)
  } else {
    selectedItems.value.add(key)
  }
}

function toggleSeller(sellerId, checked) {
  const items = groupedCart.value[String(sellerId)] || []
  if (checked) {
    items.forEach(i => selectedItems.value.add(i.saleItemId + "-" + i.sellerId))
  } else {
    items.forEach(i => selectedItems.value.delete(i.saleItemId + "-" + i.sellerId))
  }
}

function handleDecrease(item) {
  if (item.quantity <= 1) {
    showConfirm.value = true
    pendingRemove.value = item
  } else {
    cart.updateQuantity(item.saleItemId, item.sellerId, item.quantity - 1)
  }
}

function confirmRemove() {
  if (pendingRemove.value) {
    cart.removeItem(pendingRemove.value.saleItemId, pendingRemove.value.sellerId)
  }
  showConfirm.value = false
  pendingRemove.value = null
}

function cancelRemove() {
  showConfirm.value = false
  pendingRemove.value = null
}

const selectedTotalPrice = computed(() =>
  cart.cart.reduce((sum, i) => {
    const key = i.saleItemId + "-" + i.sellerId
    return selectedItems.value.has(key) ? sum + i.quantity * i.price : sum
  }, 0)
)

const selectedTotalItems = computed(() =>
  cart.cart.reduce((sum, i) => {
    const key = i.saleItemId + "-" + i.sellerId
    return selectedItems.value.has(key) ? sum + i.quantity : sum
  }, 0)
)

const placing = ref(false)
const showError = ref(false)
const errorMessage = ref("")
const showSuccess = ref(false)
const successMessage = ref("")
async function placeOrder() {
  if (placing.value || selectedItems.value.size === 0) return
  if (!auth.isAuthenticated) return

  // Block seller from placing orders for their own items
  if (auth?.user?.role === "SELLER") {
    const hasOwnItemSelected = Array.from(selectedItems.value).some(key => {
      const [saleItemIdStr, sellerIdStr] = String(key).split("-")
      return Number(sellerIdStr) === Number(auth.user.id)
    })
    if (hasOwnItemSelected) {
      errorMessage.value = "You cannot place an order for your own sale items."
      showError.value = true
      return
    }
  }

  // group selected items by seller for payload
  const bySeller = {}
  const orderedKeys = []
  for (const item of cart.cart) {
    const key = item.saleItemId + "-" + item.sellerId
    if (!selectedItems.value.has(key)) continue
    orderedKeys.push(key)
    const sid = String(item.sellerId)
    if (!bySeller[sid]) bySeller[sid] = []
    bySeller[sid].push({
      no: undefined,
      saleItemId: item.saleItemId,
      price: item.price,
      quantity: item.quantity,
      description: item.name,
    })
  }

  const orders = Object.entries(bySeller).map(([sellerId, items]) => ({
    buyerId: auth.user?.id,
    sellerId: Number(sellerId),
    shippingAddress: shippingAddress.value || "",
    orderNote: orderNote.value || "",
    items,
  }))

  placing.value = true
  try {
    const response = await createOrder(orders)
    cart.removeItemsByKeys(orderedKeys)
    selectedItems.value.clear()
    
    const createdOrders = response?.data || []
    const newOrdersCount = createdOrders.filter(order => order.orderStatus === 'NEW').length
    const cancelledOrdersCount = createdOrders.filter(order => order.orderStatus === 'CANCELLED').length
    
    if (newOrdersCount > 0) {
      for (let i = 0; i < newOrdersCount; i++) {
        orderStore.incrementBadge()
      }
      if (auth.user?.role === 'SELLER') {
        const newOrdersForThisSeller = createdOrders.filter(
          order => order.orderStatus === 'NEW' && order.seller?.id === auth.user.id
        ).length
        
        for (let i = 0; i < newOrdersForThisSeller; i++) {
          sellerOrdersStore.incrementBadge()
        }
      }
    }
    
    if (cancelledOrdersCount > 0 && newOrdersCount === 0) {
      errorMessage.value = "Your order has been cancelled automatically due to insufficient stock. Please check 'Your Orders > Cancelled'."
      showError.value = true
    } else if (cancelledOrdersCount > 0 && newOrdersCount > 0) {
      errorMessage.value = `${cancelledOrdersCount} order(s) were cancelled due to insufficient stock. Please check 'Your Orders > Cancelled' tab.`
      showError.value = true
    } else {
      successMessage.value = "Your order has been successfully processed."
      showSuccess.value = true
    }
  } catch (e) {
    errorMessage.value = e?.message || "Failed to place order"
    showError.value = true
  } finally {
    placing.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-black text-white px-6 py-24">
    <div class="text-center mb-12">
      <h1 class="text-5xl font-extrabold tracking-tight">🛒 Your Shopping Cart</h1>
      <p class="text-gray-400 mt-2">Review items and proceed to checkout</p>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-3 gap-8 max-w-7xl mx-auto">
      <!-- LEFT: Cart Items -->
      <div class="lg:col-span-2 space-y-8">
        <Card>
          <CardContent class="flex items-center">
            <input
              type="checkbox"
              v-model="allSelected"
              class="w-5 h-5 accent-blue-500 mr-3"
            />
            <span>Select All ({{ cart.totalItems }} items)</span>
          </CardContent>
        </Card>

        <div v-for="(items, sellerId) in groupedCart" :key="sellerId" class="space-y-6">
          <Card>
            <CardHeader>
              <div class="flex items-center gap-1">
                <input
                  type="checkbox"
                  :checked="items.every(i => selectedItems.has(i.saleItemId + '-' + i.sellerId)) && items.length > 0"
                  @change="(e) => toggleSeller(sellerId, e.target.checked)"
                  class="w-5 h-5 accent-blue-500"
                />
                <svg xmlns="http://www.w3.org/2000/svg" class="h-6 w-6 text-blue-300" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
                </svg>
                <CardTitle>{{ items[0].sellerNickname }} <span class="text-sm text-gray-400 "> (SELLER)</span></CardTitle>
              </div>
            </CardHeader>

            <CardContent class="space-y-4">
              <div
                v-for="item in items"
                :key="item.saleItemId"
                class="flex items-center justify-between bg-neutral-900/80 border border-neutral-700 rounded-xl p-4 shadow hover:border-purple-500 transition"
              >
                <div class="flex items-center gap-4">
                  <input
                    type="checkbox"
                    :checked="selectedItems.has(item.saleItemId + '-' + item.sellerId)"
                    @change="toggleItem(item)"
                    class="w-5 h-5 accent-green-500"
                  />

                  <div class="w-16 h-16 bg-neutral-700 rounded-lg"></div>

                  <div>
                    <div class="font-semibold text-lg">{{ item.name }}</div>
                    <div class="text-sm text-gray-400">
                      {{ (item.price * item.quantity).toLocaleString() }} THB
                      <span class="text-xs text-gray-500">
                        ({{ item.price.toLocaleString() }} THB / pc)
                      </span>
                    </div>
                  </div>
                </div>

                <div class="flex items-center space-x-3">
                  <BaseInput
                    isButton
                    buttonText="-"
                    variant="secondary"
                    @click="handleDecrease(item)"
                  />
                  <span class="px-4 font-bold text-lg">{{ item.quantity }}</span>
                  <BaseInput
                    isButton
                    buttonText="+"
                    variant="primary"
                    :disabled="item.quantity >= item.stock"
                    @click="cart.updateQuantity(item.saleItemId, item.sellerId, item.quantity + 1)"
                  />
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>

      <!-- RIGHT: Summary -->
      <Card class="h-fit sticky top-24">
        <CardHeader>
          <CardTitle>Cart Summary</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="mb-4">
            <div class="space-y-3">
              <div class="text-sm text-gray-400">
                <span class="text-gray-300">Ship to: </span>
                <span class="text-white font-medium"> {{ buyerName }} </span>
                <span v-if="shippingAddress && shippingAddress.trim()">, {{ shippingAddress }}</span>
              </div>
              
              <!-- Default Address Warning -->
              <div v-if="isDefaultAddress" class="text-xs text-yellow-400 bg-yellow-900/20 border border-yellow-700/50 rounded-lg px-3 py-2">
                ⚠️ Using default shipping address. Please update with your real address.
              </div>
              
              <label class="block text-sm text-gray-400">Address<span class="text-red-500">*</span> (Address No, Street, Subdistrict, District, Province, Postal Code)</label>
              <textarea
                class="itbms-shipping-address w-full px-4 py-3.5 rounded-xl border focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white border-neutral-700"
                rows="3"
                v-model="shippingAddress"
                placeholder="Enter shipping address"
                @input="isDefaultAddress = false"
              ></textarea>

              <label class="block text-sm text-gray-400">Note</label>
              <textarea
                class="itbms-order-note w-full px-4 py-3.5 rounded-xl border focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white border-neutral-700"
                rows="2"
                v-model="orderNote"
                placeholder="Additional instructions or requests"
              ></textarea>
            </div>
          </div>

          <p class="text-gray-300 text-lg">Selected Items: <b>{{ selectedTotalItems }}</b></p>
          <p class="text-gray-300 text-lg mb-6">
            Total Price:
            <b class="text-white">{{ selectedTotalPrice.toLocaleString() }} THB</b>
          </p>
          <BaseInput
            isButton
            buttonText="Place Order"
            variant="primary"
            :disabled="selectedTotalItems === 0 || placing || !shippingAddress || !shippingAddress.trim()"
            class="w-full py-4 text-lg font-semibold rounded-xl"
            @click="placeOrder"
          />
        </CardContent>
      </Card>
    </div>

    <ConfirmModal
      :visible="showConfirm"
      title="Remove Item"
      message="Do you want to remove this item from the cart?"
      confirmText="Remove"
      cancelText="Cancel"
      @confirm="confirmRemove"
      @cancel="cancelRemove"
    />

    <ErrorModal
      :visible="showError"
      :message="errorMessage"
      @close="showError = false"
    />

    <SuccessModal
      :visible="showSuccess"
      :message="successMessage"
      @close="showSuccess = false"
    />
  </div>
</template>
