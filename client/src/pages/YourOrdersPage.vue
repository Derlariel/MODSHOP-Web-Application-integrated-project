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

// Search state with persistence
const STORAGE_KEY_FILTERS = 'yourOrdersFilters';
const STORAGE_KEY_SEARCH_ACTIVE = 'yourOrdersSearchActive';

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

const status = ref("NEW");
const savedFilters = loadFiltersFromStorage();
const searchFilters = ref(savedFilters || {
  keyword: '',
  buyerName: '',
  sellerName: '',
  brandName: '',
  model: '',
  startDate: null,
  endDate: null
});
const isSearchActive = ref(loadSearchActiveFromStorage());
const searchResults = ref([]);
const searchTotalPages = ref(0);
const allSellerNames = ref([]);

onMounted(async () => {
  // Ensure brands are loaded for dropdown options
  try { await brandStore.loadBrands(); } catch (_) {}
  
  // Load seller names only for this user's orders
  if (userStore.user?.id) {
    try {
      const sellersRes = await getSellerNamesForUser(userStore.user.id);
      const list = Array.isArray(sellersRes?.data) ? sellersRes.data : sellersRes;
      allSellerNames.value = (list || []).filter(x => typeof x === 'string').sort((a,b) => a.localeCompare(b));
    } catch (_) {
      allSellerNames.value = [];
    }
  }
  
  const savedPage = Number(sessionStorage.getItem("ordersActivePage")) || 1;
  orderStore.setActivePage(savedPage);
  
  // Determine which tab to show based on available orders
  if (userStore.user?.id) {
    try {
      // Check for NEW orders first (priority 1)
      const newRes = await getOrdersWithStatus(userStore.user.id, 0, 1, "id,desc", "NEW");
      const hasNew = (newRes?.data?.totalElements ?? 0) > 0;
      
      if (hasNew) {
        status.value = "NEW";
      } else {
        // Check for COMPLETED orders (priority 2)
        const completedRes = await getOrdersWithStatus(userStore.user.id, 0, 1, "id,desc", "COMPLETED");
        const hasCompleted = (completedRes?.data?.totalElements ?? 0) > 0;
        
        if (hasCompleted) {
          status.value = "COMPLETED";
        } else {
          // Check for CANCELLED orders (priority 3)
          const cancelledRes = await getOrdersWithStatus(userStore.user.id, 0, 1, "id,desc", "CANCELLED");
          const hasCancelled = (cancelledRes?.data?.totalElements ?? 0) > 0;
          
          if (hasCancelled) {
            status.value = "CANCELLED";
          } else {
            // No orders at all, default to NEW
            status.value = "NEW";
          }
        }
      }
    } catch (error) {
      console.error("Error checking order counts:", error);
      status.value = "NEW"; // fallback to NEW on error
    }
    
    orderStore.refreshBadge(userStore.user.id);
  }
  
  fetchOrders();
  
  // Restore search if it was active
  if (isSearchActive.value && savedFilters) {
    await handleSearch(searchFilters.value);
  }
});

const fetchOrders = (statusValue = status.value, page = orderStore.activePage) => {
  console.log(`Fetching orders for user ${userStore.user.id}, status=${statusValue}, page=${page}`);
  orderStore.fetchOrders(userStore.user.id, statusValue, page);
};

const handleSearch = async (filterData) => {
  if (!userStore.user?.id) return;
  
  isSearchActive.value = true;
  saveSearchActiveToStorage(true);
  saveFiltersToStorage(filterData);
  orderStore.setActivePage(1);
  
  try {
    const params = new URLSearchParams();
    params.append('userId', userStore.user.id);
    params.append('page', 0);
    params.append('size', 10);
    params.append('sortField', 'orderDate');
    params.append('sortDirection', 'desc');
    
    // Always use current status tab
    params.append('orderStatus', status.value);
    
    if (filterData.keyword?.trim()) {
      params.append('keyword', filterData.keyword.trim());
    }
    if (filterData.sellerName?.trim()) {
      params.append('sellerName', filterData.sellerName.trim());
    }
    if (filterData.brandName?.trim()) {
      params.append('brandName', filterData.brandName.trim());
    }
    if (filterData.model?.trim()) {
      params.append('model', filterData.model.trim());
    }
    if (filterData.startDate) {
      params.append('startDate', filterData.startDate);
    }
    if (filterData.endDate) {
      params.append('endDate', filterData.endDate);
    }
    
    const response = await fetch(`${BASE_URL}/v2/orders/search?${params.toString()}`, {
      credentials: 'include'
    });
    
    if (!response.ok) {
      throw new Error('Failed to search orders');
    }
    
    const data = await response.json();
    searchResults.value = data.content || [];
    searchTotalPages.value = data.totalPages || 0;
  } catch (error) {
    console.error('Error searching orders:', error);
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
  orderStore.setActivePage(1);
  fetchOrders(status.value, 1);
};

const updateSearchPage = async (page) => {
  if (!userStore.user?.id) return;
  
  try {
    const params = new URLSearchParams();
    params.append('userId', userStore.user.id);
    params.append('page', page - 1); // Convert to 0-based
    params.append('size', 10);
    params.append('sortField', 'orderDate');
    params.append('sortDirection', 'desc');
    
    // Always use current status tab
    params.append('orderStatus', status.value);
    
    if (searchFilters.value.keyword?.trim()) {
      params.append('keyword', searchFilters.value.keyword.trim());
    }
    if (searchFilters.value.sellerName?.trim()) {
      params.append('sellerName', searchFilters.value.sellerName.trim());
    }
    if (searchFilters.value.brandName?.trim()) {
      params.append('brandName', searchFilters.value.brandName.trim());
    }
    if (searchFilters.value.model?.trim()) {
      params.append('model', searchFilters.value.model.trim());
    }
    if (searchFilters.value.startDate) {
      params.append('startDate', searchFilters.value.startDate);
    }
    if (searchFilters.value.endDate) {
      params.append('endDate', searchFilters.value.endDate);
    }
    
    const response = await fetch(`${BASE_URL}/v2/orders/search?${params.toString()}`, {
      credentials: 'include'
    });
    
    if (!response.ok) {
      throw new Error('Failed to search orders');
    }
    
    const data = await response.json();
    searchResults.value = data.content || [];
    orderStore.setActivePage(page);
  } catch (error) {
    console.error('Error searching orders:', error);
  }
};

const changeStatus = (newStatus) => {
  if (status.value === newStatus) return;
  status.value = newStatus;
  orderStore.setActivePage(1);
  sessionStorage.setItem("ordersActivePage", 1);
  
  // If search is active, re-run search with new status
  if (isSearchActive.value) {
    handleSearch(searchFilters.value);
  } else {
    fetchOrders(newStatus, 1);
  }
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
  const idx = orderStore.orders.findIndex(o => o.id === orderId);
  if (idx !== -1) {
    if (status.value === 'NEW') {
      orderStore.orders.splice(idx, 1);
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
  const orders = isSearchActive.value ? searchResults.value : orderStore.orders.slice();

  orders.forEach((order) => {
    const date = new Date(order.orderDate).toLocaleDateString("th-TH");
    if (!grouped[date]) grouped[date] = [];
    grouped[date].push(order);
  });
  console.log(grouped)
  return grouped;
});

// Dropdown options
const brandOptions = computed(() => {
  try {
    return Array.isArray(brandStore.filterBrands?.()) ? brandStore.filterBrands() : [];
  } catch {
    return [];
  }
});

const sellerOptions = computed(() => allSellerNames.value);

const displayTotalPages = computed(() => {
  return isSearchActive.value ? searchTotalPages.value : orderStore.allPages;
});

const handlePageUpdate = (page) => {
  if (isSearchActive.value) {
    updateSearchPage(page);
  } else {
    updatePages(page);
  }
};
</script>

<template>
  <div class="min-h-screen bg-black text-white px-6 py-20">
    <div class="text-center mb-12">
      <h1 class="text-5xl font-extrabold tracking-tight">📦 Your Orders</h1>
      <p class="text-gray-400 mt-2">Track and view your completed purchases</p>
    </div>

    <!-- Search Filter Component -->
    <div class="max-w-6xl mx-auto mb-8">
      <OrderFilterSearch
        v-model="searchFilters"
        :seller-options="sellerOptions"
        :brand-options="brandOptions"
        :keywordPlaceholder="'Search orders by seller, brand, or model...'"
        @search="handleSearch"
        @clear="handleClearSearch"
      />
    </div>

    <div v-if="orderStore.loading" class="text-gray-500 text-center py-20">
      Loading your orders...
    </div>

    <div v-else class="max-w-6xl mx-auto space-y-10">
      <!-- Tab Buttons (always visible) -->
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

      <!-- Search Results Info -->
      <div v-if="isSearchActive" class="text-center mb-4">
        <p class="text-gray-400">
          Found <span class="font-semibold text-purple-400">{{ searchResults.length }}</span> orders in <span class="font-semibold text-purple-400">{{ status }}</span> status
        </p>
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
    <div class="mt-8">
      <Pagination
        v-if="displayTotalPages > 1"
        :total-pages="displayTotalPages"
        @send-pages="handlePageUpdate"
      />
    </div>
  </div>
</template>
