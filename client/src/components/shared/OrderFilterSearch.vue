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


// refs สำหรับ input date
import { onMounted, nextTick } from 'vue'
const startDateInputRef = ref(null)
const endDateInputRef = ref(null)

const openStartDatePicker = () => {
  if (startDateInputRef.value && startDateInputRef.value.showPicker) {
    startDateInputRef.value.showPicker()
  }
}
const openEndDatePicker = () => {
  if (endDateInputRef.value && endDateInputRef.value.showPicker) {
    endDateInputRef.value.showPicker()
  }
}

let swapTimeout = null
watch(
  () => [filters.value.startDate, filters.value.endDate],
  ([start, end]) => {
    if (start && end && start > end) {
      clearTimeout(swapTimeout)
      swapTimeout = setTimeout(() => {
        if (filters.value.startDate > filters.value.endDate) {
          [filters.value.startDate, filters.value.endDate] = [filters.value.endDate, filters.value.startDate]
        }
      }, 2000)
    } else {
      clearTimeout(swapTimeout)
    }
  }
)

watch(() => props.modelValue, (newValue) => {
  const isDifferent = JSON.stringify(filters.value) !== JSON.stringify(newValue)
  if (isDifferent) {
    filters.value = { ...newValue }
  }
}, { deep: true })

const handleSearch = () => {
  emit('update:modelValue', { ...filters.value })
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
  emit('update:modelValue', { ...filters.value })
  emit('clear')
}

const handleKeyDown = (event) => {
  if (event.key === 'Enter') {
    event.preventDefault()
    handleSearch()
  }
}

const hasActiveFilters = computed(() =>
  filters.value.keyword ||
  filters.value.buyerName ||
  filters.value.sellerName ||
  filters.value.brandName ||
  filters.value.model ||
  filters.value.startDate ||
  filters.value.endDate
)

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
        <Search class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
      </div>

      <!-- Search Button -->
      <button
        @click="handleSearch"
        :disabled="!hasActiveFilters"
        class="px-4 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700 transition-colors duration-200 font-medium whitespace-nowrap"
        :class="!hasActiveFilters ? 'opacity-50 cursor-not-allowed' : ''"
      >
        Search
      </button>

      <!-- Advanced Toggle -->
      <button
        @click="toggleAdvanced"
        class="px-4 py-2 border border-neutral-700 bg-neutral-900 text-white rounded-lg hover:bg-neutral-800 transition-colors duration-200 font-medium flex items-center gap-2"
      >
        <Filter class="w-4 h-4" /> Advanced
      </button>

      <!-- Clear -->
      <button
        v-if="hasActiveFilters"
        @click="handleClear"
        class="px-4 py-2 border border-red-500 bg-neutral-900 text-red-400 rounded-lg hover:bg-red-600/20 transition-colors duration-200 font-medium flex items-center gap-2"
      >
        <X class="w-4 h-4" /> Clear
      </button>
    </div>

    <!-- Advanced Filters -->
    <div v-show="showAdvanced" class="bg-neutral-900 p-4 rounded-lg border border-neutral-700 space-y-4">
      <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">

        <div v-if="showSellerFilter">
          <label class="block text-sm font-medium text-gray-300 mb-1">Seller</label>
          <select
            v-model="filters.sellerName"
            class="w-full px-3 py-2 border border-neutral-700 bg-neutral-800 text-white rounded-lg focus:ring-purple-500"
          >
            <option value="">All Sellers</option>
            <option v-for="s in sellerOptions" :key="s" :value="s">{{ s }}</option>
          </select>
        </div>

        <div v-if="showBuyerFilter">
          <label class="block text-sm font-medium text-gray-300 mb-1">Buyer</label>
          <select
            v-model="filters.buyerName"
            class="w-full px-3 py-2 border border-neutral-700 bg-neutral-800 text-white rounded-lg focus:ring-purple-500"
          >
            <option value="">All Buyers</option>
            <option v-for="b in buyerOptions" :key="b" :value="b">{{ b }}</option>
          </select>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-300 mb-1">Brand</label>
          <select
            v-model="filters.brandName"
            class="w-full px-3 py-2 border border-neutral-700 bg-neutral-800 text-white rounded-lg focus:ring-purple-500"
          >
            <option value="">All Brands</option>
            <option v-for="b in brandOptions" :key="b" :value="b">{{ b }}</option>
          </select>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-300 mb-1">Model</label>
          <BaseInput
            v-model="filters.model"
            placeholder="Filter by model..."
            type="text"
            @keydown="handleKeyDown"
          />
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-300 mb-1">Start Date</label>
          <div class="relative group">
            <input
              v-model="filters.startDate"
              type="date"
              ref="startDateInputRef"
              class="w-full px-3 py-2 border border-neutral-700 bg-neutral-800 text-white rounded-lg focus:ring-purple-500 pr-10"
            />
            <span
              class="calendar-icon absolute right-3 top-1/2 -translate-y-1/2 w-6 h-6 flex items-center justify-center cursor-pointer transition group-hover:bg-neutral-700 rounded-full"
              @click="openStartDatePicker"
              @mousedown.prevent
            >
              <Calendar class="w-5 h-5 text-gray-400 transition group-hover:text-purple-400" />
            </span>
          </div>
        </div>

        <div>
          <label class="block text-sm font-medium text-gray-300 mb-1">End Date</label>
          <div class="relative group">
            <input
              v-model="filters.endDate"
              type="date"
              ref="endDateInputRef"
              class="w-full px-3 py-2 border border-neutral-700 bg-neutral-800 text-white rounded-lg focus:ring-purple-500 pr-10"
            />
            <span
              class="calendar-icon absolute right-3 top-1/2 -translate-y-1/2 w-6 h-6 flex items-center justify-center cursor-pointer transition group-hover:bg-neutral-700 rounded-full"
              @click="openEndDatePicker"
              @mousedown.prevent
            >
              <Calendar class="w-5 h-5 text-gray-400 transition group-hover:text-purple-400" />
            </span>
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


.group:hover .calendar-icon,
.group:focus-within .calendar-icon {
  background: #6d28d9;
  color: #a78bfa; 
}
</style>