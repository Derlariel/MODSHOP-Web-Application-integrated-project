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
const savedAddresses = ref([])
const showAddressDropdown = ref(false)

const buyerName = computed(() => {
  const u = auth.user || {}
  return (u.fullName && String(u.fullName).trim()) || ""
})

// Fetch previous order data and extract unique addresses on mount
onMounted(async () => {
  if (auth.isAuthenticated && auth.user?.id) {
    try {
      // Fetch more orders to get address history (up to 20 recent orders)
      const response = await getOrders(auth.user.id, 0, 20, "orderDate,desc")
      const orders = response?.data?.content || []
      
      // Extract unique shipping addresses from order history (limit to 4 most recent)
      const uniqueAddresses = [...new Set(
        orders
          .map(order => order.shippingAddress)
          .filter(addr => addr && addr.trim() && addr !== "123 Mock Street, Subdistrict, District, Province, 10100")
      )].slice(0, 4)
      
      savedAddresses.value = uniqueAddresses
      
      if (orders.length > 0) {
        const lastOrder = orders[0]
        // Auto-fill with most recent address if not mock address
        if (lastOrder.shippingAddress && lastOrder.shippingAddress !== "123 Mock Street, Subdistrict, District, Province, 10100") {
          shippingAddress.value = lastOrder.shippingAddress
          isDefaultAddress.value = false
        } else {
          // Use mock address for first-time users
          shippingAddress.value = "123 Mock Street, Subdistrict, District, Province, 10100"
          isDefaultAddress.value = true
        }
        
        if (lastOrder.orderNote) {
          orderNote.value = lastOrder.orderNote
        }
      } else {
        // First-time user - use mock address
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

// Function to select a saved address
function selectSavedAddress(address) {
  shippingAddress.value = address
  isDefaultAddress.value = false
  showAddressDropdown.value = false
}

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

function handleSuccessClose() {
  showSuccess.value = false
  setTimeout(() => {
    window.location.reload()
  }, 2000)
}

async function placeOrder() {
  if (placing.value || selectedItems.value.size === 0) return
  if (!auth.isAuthenticated) return

  if (isDefaultAddress.value) {
    errorMessage.value = "Please update your shipping address before placing your order"
    showError.value = true
    return
  }

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

// Clear shipping address function
function clearShippingAddress() {
  shippingAddress.value = ""
  isDefaultAddress.value = false
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
          <template v-if="cart.cart.length === 0">
            <Card>
                <CardContent>
                <div class="flex flex-col items-center justify-center py-16">
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-16 w-16 text-gray-500 mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
                  <path stroke-linecap="round" stroke-linejoin="round" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13l-1.35 2.7A1 1 0 007.6 17h8.8a1 1 0 00.95-.7L21 7M7 13V7m0 6h10" />
                  </svg>
                  <div class="text-2xl font-bold mb-2">No sale items in your cart</div>
                  <div class="text-gray-400 mb-6 text-center">Please select products to add to your cart<br>or click the button below to go back to the Product Gallery</div>
                  <router-link
                    to="/sale-items"
                    class="px-6 py-2 rounded-xl bg-white text-black font-semibold shadow hover:opacity-80 transition-all"
                  >
                    Browse Products
                  </router-link>
                </div>
                </CardContent>
            </Card>
          </template>
          <template v-else>
            <!-- ...existing code... -->
            <Card>
              <CardContent class="flex items-center justify-between">
                <div class="flex items-center">
                  <input
                    type="checkbox"
                    v-model="allSelected"
                    class="w-5 h-5 accent-blue-500 mr-3"
                  />
                  <span>Select All ({{ cart.totalItems }} items)</span>
                </div>
                <!-- Quick Select Actions -->
                <div class="flex gap-2">
                  <button
                    @click="allSelected = true"
                    class="text-xs px-3 py-1 rounded-lg bg-blue-600/20 text-blue-400 hover:bg-blue-600/40 transition"
                  >
                    Select All
                  </button>
                </div>
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
          </template>
        </div>

      <!-- RIGHT: Summary -->
      <Card class="h-fit sticky top-24">
        <CardHeader>
          <CardTitle>Cart Summary</CardTitle>
        </CardHeader>
        <CardContent>
          <div class="mb-4">
            <div class="space-y-3">
              <!-- Buyer Name Display -->
              <div class="text-sm bg-neutral-800/50 rounded-lg p-3 border border-neutral-700">
                <span class="text-gray-400">👤 Buyer: </span>
                <span class="text-white font-medium">{{ buyerName || 'Not set' }}</span>
              </div>
              
              <!-- Shipping Address Section with Visual Indicator -->
              <div class="space-y-2">
                <div class="flex items-center justify-between">
                  <label class="block text-sm font-medium text-gray-300">
                    📍 Shipping Address<span class="text-red-500">*</span>
                  </label>
                  
                  <!-- Previous Addresses Dropdown Button -->
                  <button
                    v-if="savedAddresses.length > 0"
                    @click="showAddressDropdown = !showAddressDropdown"
                    class="text-xs px-3 py-1.5 rounded-lg bg-blue-600/20 text-blue-400 hover:bg-blue-600/40 transition flex items-center gap-1"
                  >
                    <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                    </svg>
                    Previous Addresses ({{ savedAddresses.length }})
                  </button>
                </div>
                
                <!-- Previous Addresses Dropdown -->
                <div v-if="showAddressDropdown && savedAddresses.length > 0" 
                     class="bg-neutral-800/90 border border-blue-500/50 rounded-xl p-3 space-y-2 max-h-48 overflow-y-auto shadow-lg previous-addresses-scroll">
                  <p class="text-xs text-blue-300 font-semibold mb-2">📋 Select from your previous addresses:</p>
                  <button
                    v-for="(addr, index) in savedAddresses"
                    :key="index"
                    @click="selectSavedAddress(addr)"
                    class="w-full text-left text-sm px-3 py-2 rounded-lg bg-neutral-700/50 hover:bg-blue-600/30 text-gray-300 hover:text-white transition border border-transparent hover:border-blue-500/50"
                  >
                    {{ addr }}
                  </button>
                </div>
                
                <!-- Default Address Warning with improved visibility -->
                <div v-if="isDefaultAddress" class="bg-gradient-to-r from-yellow-900/30 to-orange-900/30 border-l-4 border-yellow-500 rounded-lg px-4 py-3 flex items-start gap-3">
                  <span class="text-2xl">⚠️</span>
                  <div class="flex-1">
                    <p class="text-sm font-semibold text-yellow-300">Default Address in Use</p>
                    <p class="text-xs text-yellow-200/80 mt-1">Please update with your real shipping address before placing order</p>
                  </div>
                </div>
                
                <textarea
                  class="itbms-shipping-address w-full px-4 py-3.5 rounded-xl border focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all bg-neutral-800 text-white border-neutral-700 placeholder:text-gray-500"
                  :class="isDefaultAddress ? 'border-yellow-500 ring-2 ring-yellow-500/20' : ''"
                  rows="3"
                  v-model="shippingAddress"
                  placeholder="Address No, Street, Subdistrict, District, Province, Postal Code"
                  @input="isDefaultAddress = false"
                ></textarea>
                  <!-- Clear button for mock address -->
                  <div class="flex justify-end mt-2" v-if="isDefaultAddress">
                    <button
                      type="button"
                      class="text-xs px-3 py-1 rounded-lg bg-red-600/20 text-red-400 hover:bg-red-600/40 transition"
                      @click="clearShippingAddress"
                    >
                      Clear Default Address
                    </button>
                  </div>
                <p class="text-xs text-gray-400">
                  💡 Tip: {{ savedAddresses.length > 0 ? 'Click "Previous Addresses" to quickly select a previous address' : 'Your address will be saved for faster checkout next time' }}
                </p>
              </div>

              <!-- Order Note Section -->
              <div class="space-y-2">
                <label class="block text-sm font-medium text-gray-300">📝 Order Note (Optional)</label>
                <textarea
                  class="itbms-order-note w-full px-4 py-3.5 rounded-xl border focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-all bg-neutral-800 text-white border-neutral-700 placeholder:text-gray-500"
                  rows="2"
                  v-model="orderNote"
                  placeholder="Special instructions, delivery preferences, etc."
                ></textarea>
              </div>
            </div>
          </div>

          <p class="text-gray-300 text-lg">Selected Items: <b>{{ selectedTotalItems }}</b></p>
          <p class="text-gray-300 text-lg mb-2">
            Total Price:
            <b class="text-white">{{ selectedTotalPrice.toLocaleString() }} THB</b>
          </p>
          
          <!-- Enhanced Place Order Button with Clear States -->
          <BaseInput
            isButton
            :buttonText="placing ? 'Processing...' : 'Place Order'"
            variant="primary"
            :disabled="selectedTotalItems === 0 || placing || !shippingAddress || !shippingAddress.trim()"
            class="w-[45%] py-2 text-lg font-semibold rounded-xl transition-all duration-300"
            :class="{
              'opacity-50 cursor-not-allowed': selectedTotalItems === 0 || placing || !shippingAddress || !shippingAddress.trim(),
              'hover:scale-[1.02]': selectedTotalItems > 0 && !placing && shippingAddress && shippingAddress.trim()
            }"
            @click="placeOrder"
          />
          
          <!-- Helpful hints when button is disabled -->
          <div v-if="selectedTotalItems === 0" class="mt-3 text-xs text-yellow-400 text-center">
            ⚠️ Please select at least one item
          </div>
          <div v-else-if="!shippingAddress || !shippingAddress.trim()" class="mt-3 text-xs text-yellow-400 text-center">
            ⚠️ Please enter shipping address
          </div>
        </CardContent>
      </Card>
    </div>

    <ConfirmModal
      :visible="showConfirm"
      title="Remove Item"
      message="Do you want to remove"
      :itemName="pendingRemove ? `&quot;${pendingRemove.name}&quot; from the cart?` : ''"
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
      @close="handleSuccessClose"
    />
  </div>
</template>

<style scoped>
/* Custom scrollbar for previous addresses dropdown */
.previous-addresses-scroll {
  scrollbar-width: thin;
  scrollbar-color: #ffffff #232323;
}
.previous-addresses-scroll::-webkit-scrollbar {
  width: 8px;
  background: #232323;
  border-radius: 8px;
}
.previous-addresses-scroll::-webkit-scrollbar-thumb {
  background: linear-gradient(90deg, #7c3aed 40%, #6366f1 100%);
  border-radius: 8px;
}
.previous-addresses-scroll::-webkit-scrollbar-thumb:hover {
  background: linear-gradient(90deg, #a78bfa 40%, #6366f1 100%);
}
</style>