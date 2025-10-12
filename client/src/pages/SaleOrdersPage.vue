<script setup>
import { onMounted, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/useAuthStore';
import { useSellerOrdersStore } from '@/stores/useSellerOrdersStore';
import Pagination from '@/components/shared/Pagination.vue';
import Card from '@/components/UI/cart/Card.vue';
import CardHeader from '@/components/UI/cart/CardHeader.vue';
import CardContent from '@/components/UI/cart/CardContent.vue';
import DEFAULT_IMAGE from '@/assets/default.jpg';

const BASE_IMG_URL = `${import.meta.env.VITE_BASE_URL}/sale-items-images/`;

const auth = useAuthStore();
const store = useSellerOrdersStore();
const router = useRouter();

const tabs = [
  { key: 'new', label: 'New Orders' },
  { key: 'cancelled', label: 'Cancelled Orders' },
  { key: 'all', label: 'All Orders' },
];

onMounted(async () => {
  if (!auth.user || auth.user.role !== 'SELLER') {
    router.replace({ name: 'product-gallery' });
    return;
  }
  await store.fetchOrders(auth.user.id);
});

function selectTab(key) {
  store.setTab(key);
  store.fetchOrders(auth.user.id);
}

function changePage(p) {
  store.setPage(p);
  store.fetchOrders(auth.user.id);
}

const groupedOrders = computed(() => {
  const grouped = {};
  (store.orders || []).forEach((order) => {
    const date = new Date(order.orderDate).toLocaleDateString('th-TH');
    if (!grouped[date]) grouped[date] = [];
    grouped[date].push(order);
  });
  return grouped;
});
</script>

<template>
  <div class="min-h-screen bg-black text-white px-6 py-20">
    <div class="text-center mb-8">
      <h1 class="text-4xl font-extrabold tracking-tight">🛍️ Sales Orders</h1>
      <p class="text-gray-400 mt-2">Manage your incoming and past orders</p>
    </div>

    <!-- Tabs -->
    <div class="flex gap-2 justify-center mb-6">
      <button
        v-for="t in tabs"
        :key="t.key"
        @click="selectTab(t.key)"
        class="py-1 px-4 text-sm md:text-base font-semibold rounded-lg border border-neutral-700"
        :class="store.tab === t.key ? 'bg-purple-600 text-white' : 'bg-neutral-900 text-gray-300'"
      >
        {{ t.label }}
      </button>
    </div>

    <div v-if="store.loading" class="text-gray-500 text-center py-20">
      Loading orders...
    </div>

    <div v-else-if="(store.orders || []).length === 0" class="text-center text-gray-400 py-20">
      No orders found.
    </div>

    <div v-else class="max-w-6xl mx-auto space-y-10">
      <div v-for="(orders, date) in groupedOrders" :key="date" class="space-y-6">
        <h2 class="text-2xl font-semibold text-purple-400 border-b border-neutral-700 pb-2">
          {{ date }}
        </h2>

        <div v-for="order in orders" :key="order.id" class="itbms-row">
          <Card class="bg-neutral-900/80 border border-neutral-700 hover:border-purple-500 transition">
            <CardHeader>
              <div class="flex justify-between items-center flex-wrap gap-4">
                <div class="text-gray-300">
                  <div class="font-semibold">Order #{{ order.id }}</div>
                  <div class="text-sm">Date: {{ new Date(order.orderDate).toLocaleString('th-TH') }}</div>
                  <div class="text-sm">Buyer: {{ order?.buyerName || '-' }}</div>
                </div>
                <div>
                  <span
                    class="px-3 py-1 text-xs rounded-full"
                    :class="{
                      'bg-green-600/20 text-green-400': order.orderStatus === 'COMPLETED',
                      'bg-red-600/20 text-red-400': order.orderStatus === 'CANCELLED',
                      'bg-blue-600/20 text-blue-400': order.orderStatus === 'NEW',
                    }"
                    >{{ order.orderStatus }}</span
                  >
                </div>
              </div>
              <p class="mt-2 text-sm text-gray-300">Ship to: {{ order.shippingAddress }}</p>
              <p class="mt-1 text-sm text-gray-300">Note: {{ order.orderNote || '-' }}</p>
            </CardHeader>

            <CardContent class="divide-y divide-neutral-800">
              <div v-for="item in order.orderItems" :key="item.saleItemId" class="flex items-center gap-4 py-4 itbms-item-row">
                <img
                  :src="`${BASE_IMG_URL}${item.saleItemId}.jpg`"
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

    <Pagination v-if="store.totalPages > 1" :total-pages="store.totalPages" @send-pages="changePage" />
  </div>
</template>
