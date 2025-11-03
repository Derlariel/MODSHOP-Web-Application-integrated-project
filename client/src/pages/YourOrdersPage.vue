<script setup>
import { onMounted, onActivated, computed, ref, watch } from "vue";
import { useOrderStore } from "@/stores/useOrderStore";
import { useAuthStore } from "@/stores/useAuthStore";
import Card from "@/components/UI/cart/Card.vue";
import CardHeader from "@/components/UI/cart/CardHeader.vue";
import CardContent from "@/components/UI/cart/CardContent.vue";
import CardTitle from "@/components/UI/cart/CartTitle.vue";
import Pagination from "@/components/shared/Pagination.vue";
import OrderFilterSearch from "@/components/shared/OrderFilterSearch.vue";
import { useBrandStore } from "@/stores/useBrandStore";
import { useRouter } from "vue-router";
import { getOrdersWithStatus, getSellerNamesForUser } from "@/api/orderAPI";

const router = useRouter()

const orderStore = useOrderStore();
const userStore = useAuthStore();
const brandStore = useBrandStore();
const BASE_URL = import.meta.env.VITE_BASE_URL;
const DEFAULT_IMAGE = new URL("@/assets/default.jpg", import.meta.url).href;

// Local storage keys (per-user, session-only)
const getUserStorageKey = (key) => {
  const userId = userStore.user?.id;
  return userId ? `${key}_user_${userId}` : key;
};

// Load filters
const loadFiltersFromStorage = () => {
  try {
    const saved = sessionStorage.getItem(getUserStorageKey('yourOrdersFilters'));
    return saved ? JSON.parse(saved) : null;
  } catch {
    return null;
  }
};
const saveFiltersToStorage = (filters) => sessionStorage.setItem(getUserStorageKey('yourOrdersFilters'), JSON.stringify(filters));
const loadSearchActiveFromStorage = () => sessionStorage.getItem(getUserStorageKey('yourOrdersSearchActive')) === 'true';
const saveSearchActiveToStorage = (active) => sessionStorage.setItem(getUserStorageKey('yourOrdersSearchActive'), active.toString());

const loadPageFromStorage = () => {
  try {
    const saved = sessionStorage.getItem(getUserStorageKey('yourOrdersPage'));
    return saved ? parseInt(saved) : 1;
  } catch {
    return 1;
  }
};

const savePageToStorage = (page) => {
  try {
    sessionStorage.setItem(getUserStorageKey('yourOrdersPage'), page.toString());
  } catch (e) {
    console.error('Failed to save page:', e);
  }
};

const loadStatusFromStorage = () => {
  try {
    return sessionStorage.getItem(getUserStorageKey('yourOrdersStatus')) || 'NEW';
  } catch {
    return 'NEW';
  }
};

const saveStatusToStorage = (statusValue) => {
  try {
    sessionStorage.setItem(getUserStorageKey('yourOrdersStatus'), statusValue);
  } catch (e) {
    console.error('Failed to save status:', e);
  }
};

const savedFilters = loadFiltersFromStorage();
const status = ref(loadStatusFromStorage());
const searchFilters = ref(savedFilters || { keyword:'', buyerName:'', sellerName:'', brandName:'', model:'', startDate:null, endDate:null });
const isSearchActive = ref(loadSearchActiveFromStorage());
const searchResults = ref([]);
const searchTotalPages = ref(0);
const allSellerNames = ref([]);

// Fetch orders
const fetchOrders = (statusValue = status.value, page = orderStore.activePage) => {
  orderStore.fetchOrders(userStore.user.id, statusValue, page);
};

// Search
const handleSearch = async (filterData) => {
  if (!userStore.user?.id) return;
  isSearchActive.value = true;
  saveSearchActiveToStorage(true);
  saveFiltersToStorage(filterData);
  
  // Set page to 1 without triggering watchers
  const targetPage = 1;
  orderStore.activePage = targetPage;
  savePageToStorage(targetPage);

  try {
    const params = new URLSearchParams({
      userId: userStore.user.id,
      page: 0,
      size: 10,
      sortField: 'orderDate',
      sortDirection: 'desc',
      orderStatus: status.value
    });

    if (filterData.keyword?.trim()) params.append('keyword', filterData.keyword.trim());
    if (filterData.sellerName?.trim()) params.append('sellerName', filterData.sellerName.trim());
    if (filterData.brandName?.trim()) params.append('brandName', filterData.brandName.trim());
    if (filterData.model?.trim()) params.append('model', filterData.model.trim());
    if (filterData.startDate) params.append('startDate', filterData.startDate);
    if (filterData.endDate) params.append('endDate', filterData.endDate);

    const res = await fetch(`${BASE_URL}/v2/orders/search?${params}`, { credentials: 'include' });
    const data = await res.json();
    searchResults.value = data.content || [];
    searchTotalPages.value = data.totalPages || 0;
  } catch {
    searchResults.value = [];
    searchTotalPages.value = 0;
  }
};

// Clear search
const handleClearSearch = () => {
  isSearchActive.value = false;
  saveSearchActiveToStorage(false);
  searchResults.value = [];
  searchTotalPages.value = 0;
  searchFilters.value = { keyword:'', buyerName:'', sellerName:'', brandName:'', model:'', startDate:null, endDate:null };
  saveFiltersToStorage(searchFilters.value);
  
  // Set page to 1 and fetch
  orderStore.activePage = 1;
  savePageToStorage(1);
  fetchOrders(status.value, 1);
};

// Pagination for search
const updateSearchPage = async (page) => {
  if (!userStore.user?.id) return;

  savePageToStorage(page);

  try {
    const params = new URLSearchParams({
      userId: userStore.user.id,
      page: page - 1,
      size: 10,
      sortField: 'orderDate',
      sortDirection: 'desc',
      orderStatus: status.value
    });

    if (searchFilters.value.keyword?.trim()) params.append('keyword', searchFilters.value.keyword.trim());
    if (searchFilters.value.sellerName?.trim()) params.append('sellerName', searchFilters.value.sellerName.trim());
    if (searchFilters.value.brandName?.trim()) params.append('brandName', searchFilters.value.brandName.trim());
    if (searchFilters.value.model?.trim()) params.append('model', searchFilters.value.model.trim());
    if (searchFilters.value.startDate) params.append('startDate', searchFilters.value.startDate);
    if (searchFilters.value.endDate) params.append('endDate', searchFilters.value.endDate);

    const res = await fetch(`${BASE_URL}/v2/orders/search?${params}`, { credentials: 'include' });
    const data = await res.json();
    searchResults.value = data.content || [];
    
    // Update page without triggering watcher
    orderStore.activePage = page;
  } catch (_) {}
};

const changeStatus = (newStatus) => {
  if (status.value === newStatus) return;
  status.value = newStatus;
  saveStatusToStorage(newStatus);
  
  // Set page to 1
  orderStore.activePage = 1;
  savePageToStorage(1);

  // If search is active, re-run search with new status
  if (isSearchActive.value) {
    handleSearch(searchFilters.value);
  } else {
    fetchOrders(newStatus, 1);
  }
};

// Pagination (normal)
const updatePages = (page) => {
  orderStore.setActivePage(page);
  sessionStorage.setItem("ordersActivePage", page);
};

const viewOrder = async (orderId) => {
  router.push({ name: 'YourOrderPage', params: { orderId } });
}

// ❌ REMOVED: watch that causes infinite loop
// The page changes are now handled directly by handlePageUpdate

// Group orders by date
const groupedOrders = computed(() => {
  const grouped = {};
  const orders = isSearchActive.value ? searchResults.value : orderStore.orders.slice();
  orders.forEach((order) => {
    const date = new Date(order.orderDate).toLocaleDateString("th-TH");
    if (!grouped[date]) grouped[date] = [];
    grouped[date].push(order);
  });
  return grouped;
});

// Brands / Sellers / Pages
const brandOptions = computed(() => brandStore.filterBrands?.() || []);
const sellerOptions = computed(() => allSellerNames.value);
const displayTotalPages = computed(() => isSearchActive.value ? searchTotalPages.value : orderStore.allPages);

const handlePageUpdate = (page) => {
  savePageToStorage(page);
  
  if (isSearchActive.value) {
    updateSearchPage(page);
  } else {
    orderStore.activePage = page;
    fetchOrders(status.value, page);
  }
};

// On mount
onMounted(async () => {
  try { await brandStore.loadBrands(); } catch (_) {}
  if (userStore.user?.id) {
    try {
      const sellersRes = await getSellerNamesForUser(userStore.user.id);
      const list = Array.isArray(sellersRes?.data) ? sellersRes.data : sellersRes;
      allSellerNames.value = (list || []).filter(x => typeof x === 'string').sort((a,b) => a.localeCompare(b));
    } catch (_) { allSellerNames.value = []; }
  }

  const savedPage = loadPageFromStorage();
  orderStore.activePage = savedPage; // Set directly without triggering watchers
  
  if (isSearchActive.value && savedFilters) {
    await handleSearch(searchFilters.value);
  } else {
    fetchOrders();
  }
});

onActivated(() => {
  if (userStore.user?.id) orderStore.refreshBadge(userStore.user.id);
});
</script>

<template>
  <div class="min-h-screen bg-black text-white flex flex-col px-4 sm:px-6 lg:px-8 pt-16 sm:pt-20">
    <div class="text-center mb-8 sm:mb-12">
      <h1 class="text-3xl sm:text-4xl lg:text-5xl font-extrabold tracking-tight">📦 Your Orders</h1>
      <p class="text-gray-400 mt-2 text-sm sm:text-base">Track and view your completed purchases</p>
    </div>

    <!-- Search Filter Component -->
    <div class="w-full sm:w-[90%] lg:w-[70%] xl:w-[60%] 2xl:w-[50%] mx-auto mb-6 sm:mb-8">
      <OrderFilterSearch
        v-model="searchFilters"
        :seller-options="sellerOptions"
        :brand-options="brandOptions"
        :keywordPlaceholder="'Search orders by seller, brand, or model...'"
        @search="handleSearch"
        @clear="handleClearSearch"
      />
    </div>

    <div v-if="orderStore.loading" class="text-gray-500 text-center py-12 sm:py-20">
      Loading your orders...
    </div>

    <div v-else class="flex-1 flex flex-col">
      <div class="w-full sm:w-[90%] lg:w-[70%] xl:w-[60%] 2xl:w-[50%] mx-auto space-y-6 sm:space-y-10 flex-1">
        <div class="mb-4 sm:mb-6 flex flex-wrap gap-2 sm:gap-4 justify-center">
            <button
              @click="changeStatus('NEW')"
              class="py-2 px-3 sm:py-1 sm:px-4 text-sm sm:text-base font-semibold rounded-lg border border-neutral-700 transition-colors"
              :class="status === 'NEW' ? 'bg-purple-600 text-white' : 'bg-neutral-900 text-gray-300 hover:bg-neutral-800'"
            >
              NEW
            </button>
            <button
              @click="changeStatus('COMPLETED')"
              class="py-2 px-3 sm:py-1 sm:px-4 text-sm sm:text-base font-semibold rounded-lg border border-neutral-700 transition-colors"
              :class="status === 'COMPLETED' ? 'bg-purple-600 text-white' : 'bg-neutral-900 text-gray-300 hover:bg-neutral-800'"
            >
              COMPLETED
            </button>
            <button
              @click="changeStatus('CANCELLED')"
              class="py-2 px-3 sm:py-1 sm:px-4 text-sm sm:text-base font-semibold rounded-lg border border-neutral-700 transition-colors"
              :class="status === 'CANCELLED' ? 'bg-purple-600 text-white' : 'bg-neutral-900 text-gray-300 hover:bg-neutral-800'"
            >
              CANCELLED
            </button>
        </div>

        <div
          v-if="Object.keys(groupedOrders).length === 0"
          class="flex flex-col items-center justify-center text-center text-gray-400 py-12 sm:py-20 gap-4 flex-1 px-4"
        >
          <svg xmlns="http://www.w3.org/2000/svg" class="mx-auto h-12 w-12 sm:h-16 sm:w-16 text-purple-500/60" fill="none" viewBox="0 0 24 24" stroke="currentColor" stroke-width="2">
            <path stroke-linecap="round" stroke-linejoin="round" d="M16 11V7a4 4 0 00-8 0v4M5 9h14l1 12H4L5 9z" />
          </svg>
          <div>
            <div class="text-base sm:text-lg font-semibold text-gray-300">No orders found.</div>
            <div class="text-xs sm:text-sm text-gray-400 mt-2">Your <span class="font-bold text-purple-400">{{ status }}</span> orders will be displayed here.</div>
          </div>
          <button
            v-if="status === 'NEW'"
            type="button"
            class="mt-4 sm:mt-6 px-4 sm:px-5 py-2 text-sm sm:text-base rounded-lg bg-white text-black font-semibold shadow hover:opacity-80 border border-black transition"
            @click="router.push('/sale-items')"
          >
            Browse Products & Add to Cart
          </button>
        </div>

        <div
          v-for="(orders, date) in groupedOrders"
          :key="date"
          class="space-y-4 sm:space-y-6"
        >
          <h2
            class="text-xl sm:text-2xl font-semibold text-purple-400 border-b border-neutral-700 pb-2"
          >
            {{ date }}
          </h2>

          <div v-for="(order, idx) in orders" :key="order.id" class="itbms-row"
            :class="idx === orders.length - 1 ? 'mb-6 sm:mb-8' : ''">
            <Card
              class="bg-neutral-900/80 border border-neutral-700 hover:border-purple-500 transition"
            >
              <CardHeader>
                <div class="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-3 sm:gap-4">
                  <div class="space-y-1 w-full sm:w-auto">
                    <CardTitle class="itbms-nickname text-base sm:text-lg"
                      >Seller: {{ order.seller.nickName }}</CardTitle
                    >
                    <p class="text-xs sm:text-sm text-gray-400 itbms-order-id">
                      Order #: {{ order.id }}
                    </p>
                  </div>
                  <div class="text-xs sm:text-sm text-gray-400 flex flex-col sm:flex-row gap-1 sm:gap-4 w-full sm:w-auto">
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
                  <div class="flex justify-between sm:justify-end items-center w-full sm:w-auto gap-4 sm:text-right">
                    <div>
                      <p
                        class="text-green-400 font-bold text-base sm:text-lg itbms-total-order-price"
                      >
                        ฿{{
                          order.orderItems
                            .reduce((sum, i) => sum + i.price * i.quantity, 0)
                            .toLocaleString()
                        }}
                      </p>
                      <p class="text-xs sm:text-sm text-gray-400 itbms-order-status">
                        Status: {{ order.orderStatus }}
                      </p>
                    </div>
                    <div class="flex-shrink-0">
                      <button
                        type="button"
                        @click.stop="viewOrder(order.id)"
                        class="text-xs sm:text-sm font-semibold px-3 py-1.5 sm:py-1 rounded-lg border border-purple-500 text-purple-300 hover:bg-purple-600/20 focus:outline-none focus:ring-2 focus:ring-purple-600 transition"
                        aria-label="View order details"
                      >
                        VIEW
                      </button>
                    </div>
                  </div>
                </div>

                <p class="mt-3 sm:mt-2 text-xs sm:text-sm text-gray-300 itbms-shipping-address">
                  <b>Shipped To: </b>
                  <span v-if="order.shippingAddress">
                    {{
                      userStore.user?.fullName
                        ? userStore.user.fullName + ", "
                        : ""
                    }}{{ order.shippingAddress }}
                  </span>
                </p>
                <p class="mt-1 text-xs sm:text-sm text-gray-300 itbms-order-note">
                  <b>Note:</b> {{ order.orderNote || "—" }}
                </p>
              </CardHeader>

              <CardContent class="divide-y divide-neutral-800">
                <div
                  v-for="item in order.orderItems"
                  :key="item.saleItemId"
                  class="flex items-center gap-3 sm:gap-4 py-3 sm:py-4 itbms-item-row"
                >
                  <img
                    :src="`${BASE_URL}/sale-items-images/${item.saleItemId}.jpg`"
                    @error="(e) => (e.target.src = DEFAULT_IMAGE)"
                    alt="Item Image"
                    class="w-14 h-14 sm:w-16 sm:h-16 rounded object-cover flex-shrink-0"
                  />
                  <div class="flex-1 min-w-0">
                    <p class="font-semibold text-sm sm:text-base lg:text-lg itbms-item-description truncate sm:whitespace-normal">
                      {{ item.description }}
                    </p>
                    <p class="text-xs sm:text-sm text-gray-400 itbms-item-quantity">
                      Qty: {{ item.quantity }}
                    </p>
                  </div>
                  <p class="text-right text-gray-300 text-sm sm:text-base itbms-item-total-price flex-shrink-0">
                    ฿{{ (item.price * item.quantity).toLocaleString() }}
                  </p>
                </div>
              </CardContent>
            </Card>
          </div>
        </div>
      </div>
    </div>

    <div class="flex justify-center items-end w-full flex-shrink-0 pb-4 sm:pb-0" style="min-height:80px;">
      <div class="w-full px-2 sm:px-0">
        <Pagination
          v-if="displayTotalPages > 1"
          :total-pages="displayTotalPages"
          :current-page-prop="orderStore.activePage"
          @send-pages="handlePageUpdate"
        />
      </div>
    </div>
  </div>
</template>
