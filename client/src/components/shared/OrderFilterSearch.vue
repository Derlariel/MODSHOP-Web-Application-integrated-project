<script setup>
import { ref, watch, computed } from 'vue'
import { Search, X, Calendar, Filter } from 'lucide-vue-next'
import BaseInput from '@/components/shared/BaseInput.vue'

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({
      keyword: '',
      buyerName: '',
      sellerName: '',
      brandName: '',
      model: '',
      startDate: null,
      endDate: null
    })
  },
  sellerOptions: {
    type: Array,
    default: () => []
  },
  brandOptions: {
    type: Array,
    default: () => []
  },
  buyerOptions: {
    type: Array,
    default: () => []
  },
  showSellerFilter: {
    type: Boolean,
    default: true
  },
  showBuyerFilter: {
    type: Boolean,
    default: false
  },
    keywordPlaceholder: {
        type: String,
        default: 'Search orders by seller, brand, or model...'
    }


})

const emit = defineEmits(['update:modelValue', 'search', 'clear'])

const filters = ref({ ...props.modelValue })
const showAdvanced = ref(false)

// Watch for external changes
watch(() => props.modelValue, (newValue) => {
  filters.value = { ...newValue }
}, { deep: true })

// Watch for internal changes
watch(filters, (newValue) => {
  emit('update:modelValue', newValue)
}, { deep: true })

const handleSearch = () => {
  emit('search', filters.value)
}

const handleClear = () => {
  filters.value = {
    keyword: '',
    buyerName: '',
    sellerName: '',
    brandName: '',
    model: '',
    startDate: null,
    endDate: null
  }
  emit('clear')
  emit('search', filters.value)
}

const handleKeyDown = (event) => {
  if (event.key === 'Enter') {
    event.preventDefault()
    handleSearch()
  }
}

const hasActiveFilters = computed(() => {
  return filters.value.keyword ||
    filters.value.buyerName ||
    filters.value.sellerName ||
    filters.value.brandName ||
    filters.value.model ||
    filters.value.startDate ||
    filters.value.endDate
})

const toggleAdvanced = () => {
  showAdvanced.value = !showAdvanced.value
}
</script>

<template>
  <div class="w-full space-y-4">
    <!-- Main Search Bar -->
    <div class="flex flex-col sm:flex-row gap-2 items-stretch sm:items-center">
      <!-- Keyword Search -->
      <div class="flex-1 relative">
        <BaseInput
          v-model="filters.keyword"
          :placeholder="keywordPlaceholder"
          type="text"
          @keydown="handleKeyDown"
          class="w-full"
        />
        <Search 
          class="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" 
        />
      </div>

      <!-- Search Button -->
        <button
          @click="handleSearch"
          :disabled="!hasActiveFilters"
          class="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors duration-200 font-medium whitespace-nowrap"
          :class="!hasActiveFilters ? 'opacity-50 cursor-not-allowed' : ''"
          type="button"
        >
          Search
        </button>

      <!-- Advanced Filter Toggle -->
      <button
        @click="toggleAdvanced"
        class="px-4 py-2 border border-neutral-700 bg-neutral-900 text-white rounded-lg hover:bg-neutral-800 transition-colors duration-200 font-medium whitespace-nowrap flex items-center gap-2"
        type="button"
      >
        <Filter class="w-4 h-4" />
        Advanced
      </button>

      <!-- Clear Button (shown when filters are active) -->
      <button
        v-if="hasActiveFilters"
        @click="handleClear"
        class="px-4 py-2 border border-red-500 bg-neutral-900 text-red-400 rounded-lg hover:bg-red-600/20 transition-colors duration-200 font-medium whitespace-nowrap flex items-center gap-2"
        type="button"
      >
        <X class="w-4 h-4" />
        Clear
      </button>
    </div>

    <!-- Advanced Filters -->
    <div v-show="showAdvanced" class="bg-neutral-900 p-4 rounded-lg border border-neutral-700 space-y-4">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <!-- Seller Name Dropdown -->
        <div v-if="showSellerFilter">
          <label class="block text-sm font-medium text-gray-300 mb-1">Your Seller</label>
          <select
            v-model="filters.sellerName"
            class="w-full px-3 py-2 border border-neutral-700 bg-neutral-800 text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
          >
            <option value="">All Sellers</option>
            <option v-for="s in sellerOptions" :key="s" :value="s">{{ s }}</option>
          </select>
        </div>

        <!-- Buyer Name Text Input -->
        <div v-if="showBuyerFilter">
          <label class="block text-sm font-medium text-gray-300 mb-1">Your Buyer</label>
          <select
            v-model="filters.buyerName"
            class="w-full px-3 py-2 border border-neutral-700 bg-neutral-800 text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
          >
            <option value="">All Buyers</option>
            <option v-for="b in buyerOptions" :key="b" :value="b">{{ b }}</option>
          </select>
        </div>

        <!-- Brand Dropdown -->
        <div>
          <label class="block text-sm font-medium text-gray-300 mb-1">Brand</label>
          <select
            v-model="filters.brandName"
            class="w-full px-3 py-2 border border-neutral-700 bg-neutral-800 text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
          >
            <option value="">All Brands</option>
            <option v-for="b in brandOptions" :key="b" :value="b">{{ b }}</option>
          </select>
        </div>

        <!-- Model -->
        <div>
          <label class="block text-sm font-medium text-gray-300 mb-1">Model</label>
          <BaseInput
            v-model="filters.model"
            placeholder="Filter by model..."
            type="text"
            @keydown="handleKeyDown"
          />
        </div>

        <!-- Start Date -->
        <div>
          <label class="block text-sm font-medium text-gray-300 mb-1">Start Date</label>
          <div class="relative">
            <input
              v-model="filters.startDate"
              type="date"
              class="w-full px-3 py-2 border border-neutral-700 bg-neutral-800 text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
            />
            <Calendar class="absolute right-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
          </div>
        </div>

        <!-- End Date -->
        <div>
          <label class="block text-sm font-medium text-gray-300 mb-1">End Date</label>
          <div class="relative">
            <input
              v-model="filters.endDate"
              type="date"
              class="w-full px-3 py-2 border border-neutral-700 bg-neutral-800 text-white rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-500"
            />
            <Calendar class="absolute right-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400 pointer-events-none" />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
input[type="date"]::-webkit-calendar-picker-indicator {
  opacity: 0;
}

:deep(.w-full input) {
  padding-left: 2.5rem;
}
</style>
