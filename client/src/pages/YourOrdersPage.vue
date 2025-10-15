<script setup>
import { onMounted, onActivated, computed, ref, watch } from "vue";
import { useOrderStore } from "@/stores/useOrderStore";
import { useAuthStore } from "@/stores/useAuthStore";
import Card from "@/components/UI/cart/Card.vue";
import CardHeader from "@/components/UI/cart/CardHeader.vue";
import CardContent from "@/components/UI/cart/CardContent.vue";
import CardTitle from "@/components/UI/cart/CartTitle.vue";
import Pagination from "@/components/shared/Pagination.vue";
import { useRouter } from "vue-router";

const router = useRouter()

const orderStore = useOrderStore();
const userStore = useAuthStore();
const BASE_URL = import.meta.env.VITE_BASE_URL;

const DEFAULT_IMAGE = new URL("@/assets/default.jpg", import.meta.url).href;

const status = ref("NEW");

onMounted(() => {
  const savedPage = Number(sessionStorage.getItem("ordersActivePage")) || 1;
  orderStore.setActivePage(savedPage);
  fetchOrders();
  if (userStore.user?.id) {
    orderStore.refreshBadge(userStore.user.id);
  }
});

const fetchOrders = (statusValue = status.value, page = orderStore.activePage) => {
  console.log(`Fetching orders for user ${userStore.user.id}, status=${statusValue}, page=${page}`);
  orderStore.fetchOrders(userStore.user.id, statusValue, page);
};

const changeStatus = (newStatus) => {
  if (status.value === newStatus) return;
  status.value = newStatus;
  orderStore.setActivePage(1);
  sessionStorage.setItem("ordersActivePage", 1);
  fetchOrders(newStatus, 1);
};

const updatePages = (page) => {
  orderStore.setActivePage(page);
  sessionStorage.setItem("ordersActivePage", page);
  fetchOrders(status.value, page);
};
watch(() => orderStore.orders, (newOrders) => {
  console.log(status)
  console.log("orderStore.orders changed:", newOrders);
});

watch(status, (newStatus) => {
  orderStore.setActivePage(1);
  sessionStorage.setItem("ordersActivePage", 1);
  fetchOrders(newStatus, 1);
});

const viewOrder = (orderId) => {
  // Optimistically update UI: when buyer views, show as COMPLETED
  const idx = orderStore.orders.findIndex(o => o.id === orderId);
  if (idx !== -1) {
    // If currently on NEW tab, remove the order from this list so it disappears immediately
    if (status.value === 'NEW') {
      orderStore.orders.splice(idx, 1);
      // Decrement badge when order moves from NEW status
      orderStore.decrementBadge();
    } else {
      orderStore.orders[idx] = { ...orderStore.orders[idx], orderStatus: 'COMPLETED' };
    }
  }
  router.push({ name: 'YourOrderPage', params: { orderId } });
}

// Refresh badge when component is activated (e.g., returning from another page)
onActivated(() => {
  if (userStore.user?.id) {
    orderStore.refreshBadge(userStore.user.id);
  }
});

watch(() => orderStore.activePage, (newPage) => {
  sessionStorage.setItem("ordersActivePage", newPage);
  fetchOrders(status.value, newPage);
});

const groupedOrders = computed(() => {
  console.log('Recomputing groupedOrders...');
  const grouped = {};
  const orders = orderStore.orders.slice();

  orders.forEach((order) => {
    const date = new Date(order.orderDate).toLocaleDateString("th-TH");
    if (!grouped[date]) grouped[date] = [];
    grouped[date].push(order);
  });
  console.log(grouped)
  return grouped;
});
</script>

<template>
  <div class="min-h-screen bg-black text-white px-6 py-20">
    <div class="text-center mb-12">
      <h1 class="text-5xl font-extrabold tracking-tight">📦 Your Orders</h1>
      <p class="text-gray-400 mt-2">Track and view your completed purchases</p>
    </div>

    <div v-if="orderStore.loading" class="text-gray-500 text-center py-20">
      Loading your orders...
    </div>

    <div v-else class="max-w-6xl mx-auto space-y-10">
      <div class="mb-6 flex gap-4 justify-center">
        <button
          @click="changeStatus('NEW')"
          class="py-1 px-4 text-lg font-semibold rounded-lg"
          :class="status === 'NEW' ? 'text-white bg-purple-500' : 'bg-neutral-900 text-gray-300'"
        >
          New
        </button>
        <button
          @click="changeStatus('COMPLETED')"
          class="py-1 px-4 text-lg font-semibold rounded-lg"
          :class="status === 'COMPLETED' ? 'text-white bg-purple-500' : 'bg-neutral-900 text-gray-300'"
        >
          Completed
        </button>
        <button
          @click="changeStatus('CANCELLED')"
          class="py-1 px-4 text-lg font-semibold rounded-lg"
          :class="status === 'CANCELLED' ? 'text-white bg-purple-500' : 'bg-neutral-900 text-gray-300'"
        >
          Cancelled
        </button>
      </div>

      <div
      v-if="Object.keys(groupedOrders).length === 0"
      class="text-center text-gray-400 py-20"
    >
      No orders found.
    </div>

    
      <div
        v-for="(orders, date) in groupedOrders"
        :key="date"
        class="space-y-6"
      >
        <h2
          class="text-2xl font-semibold text-purple-400 border-b border-neutral-700 pb-2"
        >
          {{ date }}
        </h2>

        <div v-for="order in orders" :key="order.id" class="itbms-row">
          <Card
            class="bg-neutral-900/80 border border-neutral-700 hover:border-purple-500 transition"
          >
            <CardHeader>
              <div class="flex justify-between items-center flex-wrap gap-4">
                <div class="space-y-1">
                  <CardTitle class="itbms-nickname"
                    >Seller: {{ order.seller.nickName }}</CardTitle
                  >
                  <p class="text-sm text-gray-400 itbms-order-id">
                    Order #: {{ order.id }}
                  </p>
                </div>
                <div class="text-sm text-gray-400 space-x-4">
                  <span class="itbms-order-date">
                    Order Date:
                    {{ new Date(order.orderDate).toLocaleDateString("th-TH") }}
                  </span>
                  <span class="itbms-payment-date">
                    Payment Date:
                    {{
                      new Date(order.paymentDate).toLocaleDateString("th-TH")
                    }}
                  </span>
                </div>
                <div class="text-right">
                  <p
                    class="text-green-400 font-bold text-lg itbms-total-order-price"
                  >
                    ฿{{
                      order.orderItems
                        .reduce((sum, i) => sum + i.price * i.quantity, 0)
                        .toLocaleString()
                    }}
                  </p>
                  <p class="text-sm text-gray-400 itbms-order-status">
                    Status: {{ order.orderStatus }}
                  </p>
                  <div class="mt-2">
                    <button
                      type="button"
                      @click.stop="viewOrder(order.id)"
                      class="text-xs md:text-sm font-semibold px-3 py-1 rounded-lg border border-purple-500 text-purple-300 hover:bg-purple-600/20 focus:outline-none focus:ring-2 focus:ring-purple-600"
                      aria-label="View order details"
                    >
                      VIEW
                    </button>
                  </div>
                </div>
              </div>

              <p class="mt-2 text-sm text-gray-300 itbms-shipping-address">
                <b>Shipped To: </b>
                <span v-if="order.shippingAddress">
                  {{
                    userStore.user?.fullName
                      ? userStore.user.fullName + ", "
                      : ""
                  }}{{ order.shippingAddress }}
                </span>
              </p>
              <p class="mt-1 text-sm text-gray-300 itbms-order-note">
                <b>Note:</b> {{ order.orderNote || "—" }}
              </p>
            </CardHeader>

            <CardContent class="divide-y divide-neutral-800">
              <div
                v-for="item in order.orderItems"
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

    <!-- Pagination -->
    <Pagination
      v-if="orderStore.allPages > 1"
      :total-pages="orderStore.allPages"
      @send-pages="updatePages"
    />
  </div>
</template>
