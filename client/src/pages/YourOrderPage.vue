<script setup>
import { onMounted, computed, ref, watch } from "vue";
import { useOrderStore } from "@/stores/useOrderStore";
import { useAuthStore } from "@/stores/useAuthStore";
import Card from "@/components/UI/cart/Card.vue";
import CardHeader from "@/components/UI/cart/CardHeader.vue";
import CardContent from "@/components/UI/cart/CardContent.vue";
import CardTitle from "@/components/UI/cart/CartTitle.vue";
import HistoryPath from "@/components/shared/HistoryPath.vue";
import { useRoute } from "vue-router";

const route = useRoute()

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

</script>

<template>
  <div class="min-h-screen bg-black text-white px-6 py-20">
    <div class="text-center mb-12">
      <h1 class="text-5xl font-extrabold tracking-tight">📦 Order details</h1>
      <p class="text-gray-400 mt-2">Track and view your completed purchases</p>
    </div>

     <HistoryPath main="Your Orders" name-path="Order Details" :previous="1" :name-path="title" />

    <div v-if="orderStore.loading" class="text-gray-500 text-center py-20">
      Loading your orders...
    </div>


    <div
      v-if="!orderStore.order"
      class="text-center text-gray-400 py-20"
    >
      No orders found.
    </div>

    <div v-if="orderStore.order" class="max-w-6xl mx-auto space-y-10">
      <div
        :key="date"
        class="space-y-6"
      >
        <h2
          class="text-2xl font-semibold text-purple-400 border-b border-neutral-700 pb-2"
        >
          {{ date }}
        </h2>

        <div class="itbms-row">
          <Card
            class="bg-neutral-900/80 border border-neutral-700 hover:border-purple-500 transition"
          >
            <CardHeader>
              <div class="flex justify-between items-center flex-wrap gap-4">
                <div class="space-y-1">
                  <CardTitle class="itbms-nickname"
                    >Seller: {{orderStore.order.seller.nickName }}</CardTitle
                  >
                  <p class="text-sm text-gray-400 itbms-order-id">
                    Order #: {{orderStore.order.id }}
                  </p>
                </div>
                <div class="text-sm text-gray-400 space-x-4">
                  <span class="itbms-order-date">
                    Order Date:
                    {{ new Date(orderStore.order.orderDate).toLocaleDateString("th-TH") }}
                  </span>
                  <span class="itbms-payment-date">
                    Payment Date:
                    {{
                      new Date(orderStore.order.paymentDate).toLocaleDateString("th-TH")
                    }}
                  </span>
                </div>
                <div class="text-right">
                  <p
                    class="text-green-400 font-bold text-lg itbms-total-order-price"
                  >
                    ฿{{
                      orderStore.order.orderItems
                        .reduce((sum, i) => sum + i.price * i.quantity, 0)
                        .toLocaleString()
                    }}
                  </p>
                  <p class="text-sm text-gray-400 itbms-order-status">
                    Status: {{ orderStore.order.orderStatus }}
                  </p>
                </div>
              </div>

              <p class="mt-2 text-sm text-gray-300 itbms-shipping-address">
                <b>Shipped To: </b>
                <span v-if="orderStore.order.shippingAddress">
                  {{
                    orderStore.order?.buyerName
                      ? orderStore.order.buyerName+ ", "
                      : ""
                  }}{{ orderStore.order.shippingAddress }}
                </span>
              </p>
              <p class="mt-1 text-sm text-gray-300 itbms-order-note">
                <b>Note:</b> {{ orderStore.order.orderNote || "—" }}
              </p>
            </CardHeader>

            <CardContent class="divide-y divide-neutral-800">
              <div
                v-for="item in orderStore.order.orderItems"
                :key="item.saleItemId"
                class="flex items-center gap-4 py-4 itbms-item-row"
              >
                <img
                  :src="`${BASE_URL}/sale-items-images/${item.saleItemId}.jpg`"
                  @error="(e) => (e.target.src = DEFAULT_IMAGE)"
                  alt="Item Image"
                  class="w-16 h-16 rounded object-cover"
                />
                <div class="flex-1">
                  <p class="font-semibold text-lg itbms-item-description">
                    {{ item.description }}
                  </p>
                  <p class="text-sm text-gray-400 itbms-item-quantity">
                    Qty: {{ item.quantity }}
                  </p>
                </div>
                <p class="text-right text-gray-300 itbms-item-total-price">
                  ฿{{ (item.price * item.quantity).toLocaleString() }}
                </p>
              </div>
            </CardContent>
          </Card>
        </div>
      </div>
    </div>

  </div>
</template>
