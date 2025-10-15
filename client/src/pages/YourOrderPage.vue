<script setup>
import { onMounted, computed, ref, watch } from "vue";
import { useOrderStore } from "@/stores/useOrderStore";
import { useAuthStore } from "@/stores/useAuthStore";
import Card from "@/components/UI/cart/Card.vue";
import CardHeader from "@/components/UI/cart/CardHeader.vue";
import CardContent from "@/components/UI/cart/CardContent.vue";
import CardTitle from "@/components/UI/cart/CartTitle.vue";
import HistoryPath from "@/components/shared/HistoryPath.vue";
import { useRoute, useRouter } from "vue-router";

const route = useRoute()
const router = useRouter()

const orderStore = useOrderStore();
const userStore = useAuthStore();
const BASE_URL = import.meta.env.VITE_BASE_URL;

const DEFAULT_IMAGE = new URL("@/assets/default.jpg", import.meta.url).href;

onMounted(() => {
  const savedPage = Number(sessionStorage.getItem("ordersActivePage")) || 1;
  orderStore.setActivePage(savedPage);
  fetchOrders();
});

const fetchOrders = (
) => {
  orderStore.fetchOrderById(route.params.orderId)
};

const historyPathName = computed(() => {
  const user = userStore.user;
  let fromSaleOrders = false;
  if (window.history.state && typeof window.history.state.back === 'string') {
    fromSaleOrders = window.history.state.back.includes('/sale-orders');
  } else if (document.referrer && document.referrer.includes('/sale-orders')) {
    fromSaleOrders = true;
  }
  if (user && user.role === 'SELLER' && fromSaleOrders) {
    return 'Your Sale Orders';
  }
  return 'Your Orders';
});

</script>

<template>
  <div class="min-h-screen bg-black text-white px-6 py-20">
    <div class="text-center mb-12">
      <h1 class="text-5xl font-extrabold tracking-tight">📦 Order details</h1>
      <p class="text-gray-400 mt-2">Track and view your completed purchases</p>
    </div>


    <div v-if="orderStore.loading" class="text-gray-500 text-center py-20">
      Loading your orders...
    </div>


    <div
      v-if="!orderStore.order"
      class="text-center text-gray-400 py-20"
    >
      No orders found.
    </div>

    <div v-if="orderStore.order" class="max-w-5xl mx-auto space-y-6">
      <HistoryPath :main="historyPathName" name-path="Order Details" :previous="1" :name-path="title" />

      <!-- Order Summary Card with enhanced styling -->
      <div class="bg-gradient-to-br from-blue-900/20 to-neutral-900/80 border-2 border-blue-500/30 rounded-2xl shadow-2xl overflow-hidden">
        <!-- Header Section -->
        <div class="bg-blue-900/30 px-6 py-4 border-b border-blue-500/30">
          <div class="flex justify-between items-start flex-wrap gap-4">
            <div class="space-y-2">
              <h2 class="text-2xl font-bold text-blue-300 itbms-nickname">
                Seller: {{orderStore.order.seller.nickName }}
              </h2>
              <p class="text-sm text-gray-400 itbms-order-id">
                Order #{{orderStore.order.id }}
              </p>
              <div class="flex items-center gap-2">
                <span
                  class="px-3 py-1 text-sm rounded-full font-semibold"
                  :class="{
                    'bg-green-600/20 text-green-400 border border-green-500/30': orderStore.order.orderStatus === 'COMPLETED',
                    'bg-red-600/20 text-red-400 border border-red-500/30': orderStore.order.orderStatus === 'CANCELLED',
                    'bg-blue-600/20 text-blue-400 border border-blue-500/30': orderStore.order.orderStatus === 'NEW',
                  }"
                >
                  {{ orderStore.order.orderStatus }}
                </span>
              </div>
            </div>
            
            <div class="text-right space-y-2">
              <p class="text-4xl font-extrabold text-green-400 itbms-total-order-price">
                ฿{{
                  orderStore.order.orderItems
                    .reduce((sum, i) => sum + i.price * i.quantity, 0)
                    .toLocaleString()
                }}
              </p>
              <div class="text-sm text-gray-400 space-y-1">
                <div class="itbms-order-date">
                  <span class="text-blue-300">Order:</span>
                  {{ new Date(orderStore.order.orderDate).toLocaleDateString("th-TH") }}
                </div>
                <div class="itbms-payment-date">
                  <span class="text-blue-300">Payment:</span>
                  {{ new Date(orderStore.order.paymentDate).toLocaleDateString("th-TH") }}
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Shipping Info Section -->
        <div class="px-6 py-4 bg-neutral-900/50 border-b border-neutral-700">
          <div class="grid grid-cols-1 gap-3">
            <div class="itbms-shipping-address">
              <span class="text-blue-300 font-semibold">📦 Ship To:</span>
              <span class="text-gray-300 ml-2">
                <span v-if="orderStore.order.shippingAddress">
                  {{
                    orderStore.order?.buyerName
                      ? orderStore.order.buyerName + ", "
                      : ""
                  }}{{ orderStore.order.shippingAddress }}
                </span>
              </span>
            </div>
            <div class="itbms-order-note">
              <span class="text-blue-300 font-semibold">📝 Note:</span>
              <span class="text-gray-300 ml-2">{{ orderStore.order.orderNote || "—" }}</span>
            </div>
          </div>
        </div>

        <!-- Items Section -->
        <div class="px-6 py-4">
          <h3 class="text-xl font-bold text-blue-300 mb-4">Order Items</h3>
          <div class="space-y-4">
            <div
              v-for="item in orderStore.order.orderItems"
              :key="item.saleItemId"
              class="flex items-center gap-4 p-4 bg-neutral-800/50 rounded-xl border border-neutral-700 hover:border-blue-500/50 transition itbms-item-row"
            >
              <img
                :src="`${BASE_URL}/sale-items-images/${item.saleItemId}.jpg`"
                @error="(e) => (e.target.src = DEFAULT_IMAGE)"
                alt="Item Image"
                class="w-20 h-20 rounded-lg object-cover border-2 border-blue-500/30"
              />
              <div class="flex-1">
                <p class="font-bold text-xl text-white itbms-item-description">
                  {{ item.description }}
                </p>
                <p class="text-sm text-gray-400 itbms-item-quantity">
                  Quantity: <span class="text-blue-300 font-semibold">{{ item.quantity }}</span>
                </p>
                <p class="text-sm text-gray-400">
                  Unit Price: <span class="text-white">฿{{ item.price.toLocaleString() }}</span>
                </p>
              </div>
              <div class="text-right">
                <p class="text-2xl font-bold text-blue-300 itbms-item-total-price">
                  ฿{{ (item.price * item.quantity).toLocaleString() }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

  </div>
</template>
