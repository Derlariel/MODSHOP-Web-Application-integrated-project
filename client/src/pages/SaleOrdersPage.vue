<script setup>
import { onMounted, onActivated, ref, computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/useAuthStore';
import { useSellerOrdersStore } from '@/stores/useSellerOrdersStore';
import Pagination from '@/components/shared/Pagination.vue';
import OrderFilterSearch from '@/components/shared/OrderFilterSearch.vue';
import { useBrandStore } from '@/stores/useBrandStore';
import Card from '@/components/UI/cart/Card.vue';
import CardHeader from '@/components/UI/cart/CardHeader.vue';
import CardContent from '@/components/UI/cart/CardContent.vue';
import DEFAULT_IMAGE from '@/assets/default.jpg';
import { getOrdersWithId, getSellerOrders, getBuyerNamesForSeller } from '@/api/orderAPI';


const BASE_IMG_URL = `${import.meta.env.VITE_BASE_URL}/sale-items-images/`;

const auth = useAuthStore();
const store = useSellerOrdersStore();
const brandStore = useBrandStore();
const router = useRouter();

const tabs = [
  { key: 'new', label: 'NEW ' },
  { key: 'cancelled', label: 'CANCELLED' },
  { key: 'all', label: 'ALL ORDERS' },
];

onMounted(async () => {
  // preload brands for dropdown
  try { await brandStore.loadBrands(); } catch (e) {}
  
  if (!auth.user || auth.user.role !== 'SELLER') {
    router.replace({ name: 'product-gallery' });
    return;
  }
  
  // preload buyer names for dropdown - only buyers who have ordered from this seller
  try {
    const buyersRes = await getBuyerNamesForSeller(auth.user.id);
    const list = Array.isArray(buyersRes?.data) ? buyersRes.data : buyersRes;
    allBuyerNames.value = (list || []).filter(x => typeof x === 'string').sort((a,b) => a.localeCompare(b));
  } catch (_) {
    allBuyerNames.value = [];
  }
  
  // Determine which tab to show based on available orders
  try {
    // Check for NEW orders first (priority 1)
    const newRes = await getSellerOrders(auth.user.id, {
      tab: 'new',
      page: 0,
      size: 1,
      sort: 'id,desc',
    });
    const hasNew = (newRes?.data?.totalElements ?? 0) > 0;
    
    if (hasNew) {
      store.setTab('new');
    } else {
      // Check for ALL orders (priority 2)
      const allRes = await getSellerOrders(auth.user.id, {
        tab: 'all',
        page: 0,
        size: 1,
        sort: 'id,desc',
      });
      const hasAll = (allRes?.data?.totalElements ?? 0) > 0;
      
      if (hasAll) {
        store.setTab('all');
      } else {
        // Check for CANCELLED orders (priority 3)
        const cancelledRes = await getSellerOrders(auth.user.id, {
          tab: 'cancelled',
          page: 0,
          size: 1,
          sort: 'id,desc',
        });
        const hasCancelled = (cancelledRes?.data?.totalElements ?? 0) > 0;
        
        if (hasCancelled) {
          store.setTab('cancelled');
        } else {
          // No orders at all, default to NEW
          store.setTab('new');
        }
      }
    }
  } catch (error) {
    console.error("Error checking seller order counts:", error);
    store.setTab('new'); // fallback to new on error
  }
  
  await store.fetchOrders(auth.user.id);
  // Refresh badge count when page loads
  store.refreshBadge(auth.user.id);
  
  // Restore search if it was active
  if (isSearchActive.value && savedFilters) {
    await handleSearch(searchFilters.value);
  }
});

const viewOrder = async (orderId) => {
  try {
    await getOrdersWithId(orderId);
    await store.fetchOrders(auth.user.id);
  } catch (e) {
  } finally {
    router.push({ name: 'YourOrderPage', params: { orderId } });
  }
}

onActivated(() => {
  if (auth.user?.id) {
    store.refreshBadge(auth.user.id);
  }
});

function selectTab(key) {
  store.setTab(key);
  if (isSearchActive.value) {
    handleSearch(searchFilters.value);
  } else {
    store.fetchOrders(auth.user.id);
  }
}

function changePage(p) {
  if (isSearchActive.value) {
    updateSearchPage(p);
  } else {
    store.setPage(p);
    store.fetchOrders(auth.user.id);
  }
}

// Search state with persistence
const STORAGE_KEY_FILTERS = 'saleOrdersFilters';
const STORAGE_KEY_SEARCH_ACTIVE = 'saleOrdersSearchActive';

const loadFiltersFromStorage = () => {
  try {
    const saved = sessionStorage.getItem(STORAGE_KEY_FILTERS);
    return saved ? JSON.parse(saved) : null;
  } catch {
    return null;
  }
};

const saveFiltersToStorage = (filters) => {
  try {
    sessionStorage.setItem(STORAGE_KEY_FILTERS, JSON.stringify(filters));
  } catch (e) {
    console.error('Failed to save filters:', e);
  }
};

const loadSearchActiveFromStorage = () => {
  try {
    const saved = sessionStorage.getItem(STORAGE_KEY_SEARCH_ACTIVE);
    return saved === 'true';
  } catch {
    return false;
  }
};

const saveSearchActiveToStorage = (active) => {
  try {
    sessionStorage.setItem(STORAGE_KEY_SEARCH_ACTIVE, active.toString());
  } catch (e) {
    console.error('Failed to save search active state:', e);
  }
};

const savedFilters = loadFiltersFromStorage();
const isSearchActive = ref(loadSearchActiveFromStorage());
const searchFilters = ref(savedFilters || {
  keyword: '',
  buyerName: '',
  sellerName: '',
  brandName: '',
  model: '',
  startDate: null,
  endDate: null
});
const searchResults = ref([]);
const searchTotalPages = ref(0);
const allBuyerNames = ref([]);

const brandOptions = computed(() => {
  try { return brandStore.filterBrands(); } catch { return []; }
});

const buyerOptions = computed(() => allBuyerNames.value);

const groupedOrders = computed(() => {
  const grouped = {};
  const source = isSearchActive.value ? searchResults.value : store.orders;
  (source || []).forEach((order) => {
    const date = new Date(order.orderDate).toLocaleDateString('th-TH');
    if (!grouped[date]) grouped[date] = [];
    grouped[date].push(order);
  });
  return grouped;
});

const displayTotalPages = computed(() => isSearchActive.value ? searchTotalPages.value : store.totalPages);

const mapTabToStatus = (tab) => {
  if (tab === 'new') return 'NEW';
  if (tab === 'cancelled') return 'CANCELLED';
  return null; // 'all' => no status filter
};

const handleSearch = async (filterData) => {
  if (!auth.user?.id) return;
  isSearchActive.value = true;
  saveSearchActiveToStorage(true);
  saveFiltersToStorage(filterData);
  store.setPage(1);
  try {
    const params = new URLSearchParams();
    params.append('sellerId', auth.user.id);
    params.append('page', 0);
    params.append('size', 10);
    params.append('sortField', 'orderDate');
    params.append('sortDirection', 'desc');

    const status = mapTabToStatus(store.tab);
    if (status) params.append('orderStatus', status);

    if (filterData.keyword?.trim()) params.append('keyword', filterData.keyword.trim());
  if (filterData.buyerName?.trim()) params.append('buyerName', filterData.buyerName.trim());
    if (filterData.brandName?.trim()) params.append('brandName', filterData.brandName.trim());
    if (filterData.model?.trim()) params.append('model', filterData.model.trim());
    if (filterData.startDate) params.append('startDate', filterData.startDate);
    if (filterData.endDate) params.append('endDate', filterData.endDate);

    const res = await fetch(`${import.meta.env.VITE_BASE_URL}/v2/orders/search?${params.toString()}`, { credentials: 'include' });
    if (!res.ok) throw new Error('Failed to search seller orders');
    const data = await res.json();
    searchResults.value = data.content || [];
    searchTotalPages.value = data.totalPages || 0;
  } catch (e) {
    console.error('Seller search error:', e);
    searchResults.value = [];
    searchTotalPages.value = 0;
  }
};

const handleClearSearch = () => {
  isSearchActive.value = false;
  saveSearchActiveToStorage(false);
  searchResults.value = [];
  searchTotalPages.value = 0;
  searchFilters.value = {
    keyword: '',
    buyerName: '',
    sellerName: '',
    brandName: '',
    model: '',
    startDate: null,
    endDate: null
  };
  saveFiltersToStorage(searchFilters.value);
  store.setPage(1);
  store.fetchOrders(auth.user.id);
};

const updateSearchPage = async (page) => {
  if (!auth.user?.id) return;
  try {
    const params = new URLSearchParams();
    params.append('sellerId', auth.user.id);
    params.append('page', page - 1);
    params.append('size', 10);
    params.append('sortField', 'orderDate');
    params.append('sortDirection', 'desc');

    const status = mapTabToStatus(store.tab);
    if (status) params.append('orderStatus', status);

    if (searchFilters.value.keyword?.trim()) params.append('keyword', searchFilters.value.keyword.trim());
  if (searchFilters.value.buyerName?.trim()) params.append('buyerName', searchFilters.value.buyerName.trim());
    if (searchFilters.value.brandName?.trim()) params.append('brandName', searchFilters.value.brandName.trim());
    if (searchFilters.value.model?.trim()) params.append('model', searchFilters.value.model.trim());
    if (searchFilters.value.startDate) params.append('startDate', searchFilters.value.startDate);
    if (searchFilters.value.endDate) params.append('endDate', searchFilters.value.endDate);

    const res = await fetch(`${import.meta.env.VITE_BASE_URL}/v2/orders/search?${params.toString()}`, { credentials: 'include' });
    if (!res.ok) throw new Error('Failed to search seller orders');
    const data = await res.json();
    searchResults.value = data.content || [];
    store.setPage(page);
  } catch (e) {
    console.error('Seller search page error:', e);
  }
};
</script>

<template>
  <div class="min-h-screen bg-black text-white pt-20 flex flex-col">
    <div class="text-center mb-8">
      <h1 class="text-4xl font-extrabold tracking-tight">🛍️ Sales Orders</h1>
      <p class="text-gray-400 mt-2">Manage your incoming and past orders</p>
    </div>

    <!-- Search Filter Component -->
    <div class="max-w-6xl mx-auto mb-8">
      <OrderFilterSearch
        v-model="searchFilters"
        :brand-options="brandOptions"
        :buyer-options="buyerOptions"
        :show-seller-filter="false"
        :show-buyer-filter="true"
        :keywordPlaceholder="'Search orders by buyer, brand, or model...'"
        @search="handleSearch"
        @clear="handleClearSearch"
      />
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

    <div v-else-if="(store.orders || []).length === 0" class="flex flex-col items-center justify-center text-center text-gray-400 py-20 gap-4 flex-1">
      <svg xmlns="http://www.w3.org/2000/svg" class="mx-auto h-16 w-16 text-purple-500/60" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
        <path stroke-linecap="round" stroke-linejoin="round" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
      </svg>
      <div>
        <div class="text-lg font-semibold text-gray-300">No orders found.</div>
        <div class="text-sm text-gray-400 mt-2">Your <span class="font-bold text-purple-400">{{ tabs.find(t => t.key === store.tab)?.label }}</span> will be displayed here.</div>
      </div>
    </div>

    <div v-else class="flex-1 flex flex-col">
      <div class="max-w-6xl mx-auto w-full space-y-10 flex-1">
        <div v-for="(orders, date) in groupedOrders" :key="date" class="space-y-6">
          <h2 class="text-2xl font-semibold text-purple-400 border-b border-neutral-700 pb-2">
            {{ date }}
          </h2>

          <div v-for="(order, idx) in orders" :key="order.id" class="itbms-row"
            :class="idx === orders.length - 1 ? 'mb-8' : ''">
            <Card class="bg-neutral-900/80 border border-neutral-700 hover:border-purple-500 transition">
              <CardHeader>
                <div class="flex justify-between items-center flex-wrap gap-4">
                  <div class="text-gray-300">
                    <div class="font-semibold">Order #{{ order.id }}</div>
                    <div class="text-sm">Date: {{ new Date(order.orderDate).toLocaleString('th-TH') }}</div>
                    <div class="text-sm">Buyer: {{ order?.buyerName || '-' }}</div>
                  </div>
                  <div class="flex items-center gap-3">
                    <span
                      class="px-3 py-1 text-xs rounded-full"
                      :class="{
                        'bg-green-600/20 text-green-400': order.orderStatus === 'COMPLETED',
                        'bg-red-600/20 text-red-400': order.orderStatus === 'CANCELLED',
                        'bg-blue-600/20 text-blue-400': order.orderStatus === 'NEW',
                      }"
                      >{{ order.orderStatus }}</span
                    >
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
                  <p class="mt-2 text-sm text-gray-300">Ship to: {{ order?.buyerName }}, {{ order?.shippingAddress }}</p>
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
    </div>

    <div class="flex justify-center items-end w-full flex-shrink-0" style="min-height:80px;">
      <div class="w-full">
        <Pagination v-if="store.totalPages > 1" :total-pages="store.totalPages" @send-pages="changePage" />
      </div>
    </div>
  </div>
</template>
