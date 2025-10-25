<script setup>
import { ref, onMounted } from 'vue'
import OrderFilterSearch from '@/components/shared/OrderFilterSearch.vue'
import { useOrderFilters } from '@/composables/useOrderFilters'

const BASE_URL = import.meta.env.VITE_BASE_URL

const { filters, buildQueryParams, clearFilters, setPage } = useOrderFilters()

const orders = ref([])
const totalPages = ref(0)
const totalElements = ref(0)
const loading = ref(false)
const error = ref(null)

const searchFilters = ref({
  keyword: '',
  sellerName: '',
  brandName: '',
  model: '',
  startDate: null,
  endDate: null,
  orderStatus: null
})

const fetchOrders = async () => {
  loading.value = true
  error.value = null

  try {
    const queryString = buildQueryParams()
    const response = await fetch(`${BASE_URL}/v2/orders/search?${queryString}`, {
      credentials: 'include'
    })

    if (!response.ok) {
      throw new Error('Failed to fetch orders')
    }

    const data = await response.json()
    orders.value = data.content || []
    totalPages.value = data.totalPages || 0
    totalElements.value = data.totalElements || 0
  } catch (err) {
    error.value = err.message
    console.error('Error fetching orders:', err)
  } finally {
    loading.value = false
  }
}

const handleSearch = (filterData) => {
  // Update filters from search component
  filters.value.keyword = filterData.keyword || ''
  filters.value.sellerName = filterData.sellerName || ''
  filters.value.brandName = filterData.brandName || ''
  filters.value.model = filterData.model || ''
  filters.value.startDate = filterData.startDate || null
  filters.value.endDate = filterData.endDate || null
  filters.value.orderStatus = filterData.orderStatus || null
  filters.value.page = 0 // Reset to first page

  fetchOrders()
}

const handleClear = () => {
  clearFilters()
  searchFilters.value = {
    keyword: '',
    sellerName: '',
    brandName: '',
    model: '',
    startDate: null,
    endDate: null,
    orderStatus: null
  }
  fetchOrders()
}

const handlePageChange = (newPage) => {
  setPage(newPage)
  fetchOrders()
}

const formatDate = (dateString) => {
  if (!dateString) return 'N/A'
  return new Date(dateString).toLocaleDateString('en-US', {
    year: 'numeric',
    month: 'short',
    day: 'numeric'
  })
}

const formatPrice = (price) => {
  return new Intl.NumberFormat('en-US', {
    style: 'currency',
    currency: 'THB'
  }).format(price || 0)
}

const getStatusClass = (status) => {
  switch (status) {
    case 'NEW':
      return 'bg-blue-100 text-blue-800'
    case 'COMPLETED':
      return 'bg-green-100 text-green-800'
    case 'CANCELLED':
      return 'bg-red-100 text-red-800'
    default:
      return 'bg-gray-100 text-gray-800'
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<template>
  <div class="container mx-auto px-4 py-8">
    <h1 class="text-3xl font-bold mb-6">Order Management</h1>

    <!-- Filter/Search Component -->
    <div class="mb-6">
      <OrderFilterSearch
        v-model="searchFilters"
        @search="handleSearch"
        @clear="handleClear"
      />
    </div>

    <!-- Results Summary -->
    <div class="mb-4 flex items-center justify-between">
      <p class="text-gray-600">
        Found <span class="font-semibold">{{ totalElements }}</span> orders
      </p>
      <div class="flex items-center gap-2">
        <label class="text-sm text-gray-600">Show:</label>
        <select
          v-model="filters.size"
          @change="fetchOrders"
          class="px-3 py-1 border border-gray-300 rounded-lg text-sm"
        >
          <option :value="5">5</option>
          <option :value="10">10</option>
          <option :value="20">20</option>
          <option :value="50">50</option>
        </select>
      </div>
    </div>

    <!-- Loading State -->
    <div v-if="loading" class="text-center py-12">
      <div class="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      <p class="mt-2 text-gray-600">Loading orders...</p>
    </div>

    <!-- Error State -->
    <div v-else-if="error" class="bg-red-50 border border-red-200 rounded-lg p-4">
      <p class="text-red-600">{{ error }}</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="orders.length === 0" class="text-center py-12">
      <p class="text-gray-500 text-lg">No orders found</p>
      <p class="text-gray-400 text-sm mt-2">Try adjusting your search filters</p>
    </div>

    <!-- Orders List -->
    <div v-else class="space-y-4">
      <div
        v-for="order in orders"
        :key="order.id"
        class="bg-white border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow duration-200"
      >
        <div class="flex items-start justify-between mb-3">
          <div>
            <h3 class="font-semibold text-lg">Order #{{ order.id }}</h3>
            <p class="text-sm text-gray-500">{{ formatDate(order.orderDate) }}</p>
          </div>
          <span
            :class="['px-3 py-1 rounded-full text-xs font-medium', getStatusClass(order.orderStatus)]"
          >
            {{ order.orderStatus }}
          </span>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4 mb-3">
          <div>
            <p class="text-sm text-gray-600">Seller</p>
            <p class="font-medium">{{ order.seller?.fullName || 'N/A' }}</p>
          </div>
          <div>
            <p class="text-sm text-gray-600">Buyer</p>
            <p class="font-medium">{{ order.buyerName || 'N/A' }}</p>
          </div>
        </div>

        <div v-if="order.orderItems && order.orderItems.length > 0" class="border-t pt-3">
          <p class="text-sm text-gray-600 mb-2">Items ({{ order.orderItems.length }})</p>
          <div class="space-y-2">
            <div
              v-for="item in order.orderItems"
              :key="item.no"
              class="flex items-center justify-between text-sm"
            >
              <div class="flex-1">
                <span class="font-medium">{{ item.brandName }}</span>
                <span class="text-gray-600"> - {{ item.model }}</span>
                <span class="text-gray-500"> ({{ item.color }})</span>
              </div>
              <div class="text-right">
                <p class="text-gray-600">Qty: {{ item.quantity }}</p>
                <p class="font-medium">{{ formatPrice(item.price) }}</p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Pagination -->
    <div v-if="totalPages > 1" class="mt-6 flex justify-center gap-2">
      <button
        :disabled="filters.page === 0"
        @click="handlePageChange(filters.page - 1)"
        class="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        Previous
      </button>
      
      <div class="flex items-center gap-2">
        <template v-for="page in totalPages" :key="page">
          <button
            v-if="page - 1 < 3 || page - 1 > totalPages - 4 || Math.abs(page - 1 - filters.page) < 2"
            @click="handlePageChange(page - 1)"
            :class="[
              'px-3 py-2 rounded-lg',
              page - 1 === filters.page
                ? 'bg-blue-600 text-white'
                : 'border border-gray-300 hover:bg-gray-50'
            ]"
          >
            {{ page }}
          </button>
          <span v-else-if="page === 4 || page === totalPages - 3" class="px-2">...</span>
        </template>
      </div>

      <button
        :disabled="filters.page >= totalPages - 1"
        @click="handlePageChange(filters.page + 1)"
        class="px-4 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
      >
        Next
      </button>
    </div>
  </div>
</template>

<style scoped>
</style>
