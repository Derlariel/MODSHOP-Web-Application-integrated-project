
<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useProductStore } from "@/stores/useProductStore";



const productStore = useProductStore();
// Props
const props = defineProps({
  totalPages: {
    type: Number,
    required: true,
    default: 1
  }
})

// Emits
const emit = defineEmits(['sendPages'])

// Reactive state
const currentPage = ref(1)
const startPage = ref(1)

// Computed properties
const maxVisiblePages = 10

// คำนวณหน้าที่จะแสดงในปัจจุบัน
const visiblePages = computed(() => {
  const total = props.totalPages
  
  if (total <= maxVisiblePages) {
    // ถ้าจำนวนหน้าน้อยกว่าหรือเท่ากับ 10 แสดงทั้งหมด
    return Array.from({ length: total }, (_, i) => i + 1)
  }
  
  // แสดง 10 หน้า เริ่มจาก startPage
  const end = Math.min(startPage.value + maxVisiblePages - 1, total)
  const start = Math.max(1, end - maxVisiblePages + 1)
  
  return Array.from({ length: end - start + 1 }, (_, i) => start + i)
})

// คำนวณตำแหน่งปัจจุบันในรายการที่แสดง (0-9)
const currentPosition = computed(() => {
  return visiblePages.value.indexOf(currentPage.value)
})

// ตรวจสอบว่าควร disabled ปุ่มหรือไม่
const isFirstDisabled = computed(() => currentPage.value === 1)
const isPrevDisabled = computed(() => currentPage.value === 1)
const isNextDisabled = computed(() => currentPage.value === props.totalPages)
const isLastDisabled = computed(() => currentPage.value === props.totalPages)


// Functions
const setActivePage = (page) => {
  if (page < 1 || page > props.totalPages) return
  
  currentPage.value = page
  emit('sendPages', page)

  localStorage.setItem('activePage', String(page))
  productStore.setActivePage(page)
}

const goToFirst = () => {
  currentPage.value = 1
  startPage.value = 1
  emit('sendPages', 1)
  localStorage.setItem('activePage', '1')
}

const goToLast = () => {
  currentPage.value = props.totalPages
  
  // คำนวณ startPage สำหรับหน้าสุดท้าย
  if (props.totalPages > maxVisiblePages) {
    startPage.value = props.totalPages - maxVisiblePages + 1
  } else {
    startPage.value = 1
  }
  
  emit('sendPages', props.totalPages)
  localStorage.setItem('activePage', String(props.totalPages))
}

const goToNext = () => {
  if (currentPage.value >= props.totalPages) return
  
  const nextPage = currentPage.value + 1
  const position = currentPosition.value
  
  // ถ้าอยู่ที่ตำแหน่งที่ 9 (ลำดับสุดท้าย) และยังมีหน้าถัดไป
  if (position === 9 && nextPage <= props.totalPages) {
    // เลื่อน startPage ไปข้างหน้า
    startPage.value = Math.min(startPage.value + 1, props.totalPages - maxVisiblePages + 1)
  }
  
  setActivePage(nextPage)
}

const goToPrev = () => {
  if (currentPage.value <= 1) return
  
  const prevPage = currentPage.value - 1
  const position = currentPosition.value
  
  // ถ้าอยู่ที่ตำแหน่งที่ 0 (ลำดับแรก) และยังมีหน้าก่อนหน้า
  if (position === 0 && prevPage >= 1) {
    // เลื่อน startPage ไปข้างหลัง
    startPage.value = Math.max(startPage.value - 1, 1)
  }
  
  setActivePage(prevPage)
}

// Watch for totalPages changes
watch(() => props.totalPages, (newTotal) => {
  if (currentPage.value > newTotal) {
    setActivePage(1)
    startPage.value = 1
  }
})

// Initialize from localStorage
onMounted(() => {
  const savedPage = localStorage.getItem('activePage')
  if (savedPage) {
    const page = parseInt(savedPage)
    if (page >= 1 && page <= props.totalPages) {
      currentPage.value = page
      
      // คำนวณ startPage ที่เหมาะสม
      if (props.totalPages > maxVisiblePages) {
        if (page <= maxVisiblePages) {
          startPage.value = 1
        } else {
          startPage.value = Math.max(1, page - maxVisiblePages + 1)
        }
      }
    }
  }
})
</script>

<template>
  <div 
    v-if="totalPages > 1"
    class="pagination-wrapper w-full py-6 px-4 bg-gray-950  overflow-x-auto"
  >
    <div class="pagination-container flex items-center space-x-2 min-w-max mx-auto">
      <!-- First Button -->
      <button
        @click="goToFirst"
        :disabled="isFirstDisabled"
        :class="[
          'itbms-page-first px-3 py-2 text-sm font-medium rounded-md transition-all duration-200 ease-out',
          isFirstDisabled
            ? 'text-gray-500 cursor-not-allowed'
            : 'text-gray-300 hover:text-white hover:bg-gray-800 active:bg-gray-700',
        ]"
      >
        First
      </button>

      <!-- Previous Button -->
      <button
        @click="goToPrev"
        :disabled="isPrevDisabled"
        :class="[
          'itbms-page-prev w-8 h-8 flex items-center justify-center rounded-md transition-all duration-200 ease-out',
          isPrevDisabled
            ? 'text-gray-500 cursor-not-allowed'
            : 'text-gray-300 hover:text-white hover:bg-gray-800 active:bg-gray-700',
        ]"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7" />
        </svg>
      </button>

      <!-- Page Numbers -->
      <div class="desktop-pagination flex items-center space-x-1">
        <button
          v-for="(page, index) in visiblePages"
          :key="page"
          @click="setActivePage(page)"
          :class="[
            `itbms-page-${index}`,
            'w-8 h-8 flex items-center justify-center text-sm font-medium rounded-md transition-all duration-200 ease-out',
            currentPage === page
              ? 'bg-blue-600 text-white shadow-sm'
              : 'text-gray-400 hover:text-white hover:bg-gray-800 active:bg-gray-700',
          ]"
          :aria-label="`Page ${page}`"
          :aria-current="currentPage === page ? 'page' : undefined"
        >
          {{ page }}
        </button>
      </div>

      <!-- Page Indicator (Mobile) -->
      <div class="mobile-pagination">
        <span class="px-3 py-1 bg-gray-800 rounded-md text-white text-sm">
          {{ currentPage }} / {{ totalPages }}
        </span>
      </div>

      <!-- Next Button -->
      <button
        @click="goToNext"
        :disabled="isNextDisabled"
        :class="[
          'itbms-page-next w-8 h-8 flex items-center justify-center rounded-md transition-all duration-200 ease-out',
          isNextDisabled
            ? 'text-gray-500 cursor-not-allowed'
            : 'text-gray-300 hover:text-white hover:bg-gray-800 active:bg-gray-700',
        ]"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7" />
        </svg>
      </button>

      <!-- Last Button -->
      <button
        @click="goToLast"
        :disabled="isLastDisabled"
        :class="[
          'itbms-page-last px-3 py-2 text-sm font-medium rounded-md transition-all duration-200 ease-out',
          isLastDisabled
            ? 'text-gray-500 cursor-not-allowed'
            : 'text-gray-300 hover:text-white hover:bg-gray-800 active:bg-gray-700',
        ]"
      >
        Last
      </button>
    </div>
  </div>
</template>

<style scoped>
.pagination-wrapper {
  display: flex;
  justify-content: center;
}

.pagination-container {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-wrap: nowrap;
  white-space: nowrap;
}

.desktop-pagination {
  display: flex;
}

.mobile-pagination {
  display: none;
}

@media (max-width: 640px) {
  .desktop-pagination {
    display: none;
  }

  .mobile-pagination {
    display: flex;
  }
}

button:hover:not(:disabled) {
  transform: translateY(-0.5px);
}

button:active:not(:disabled) {
  transform: translateY(0);
}

button:focus:not(:disabled) {
  outline: 2px solid rgba(59, 130, 246, 0.5);
  outline-offset: 2px;
}

button {
  will-change: transform, background-color, color;
  border: none;
  background: transparent;
}

button[aria-current="page"] {
  box-shadow: 0 0 0 1px rgba(59, 130, 246, 0.3);
}

@media (max-width: 768px) {
  .pagination-container {
    gap: 0.125rem;
  }

  button {
    font-size: 0.75rem;
  }

  .w-8 {
    width: 1.75rem;
    height: 1.75rem;
  }

  button:not(.w-8) {
    padding: 0.25rem 0.5rem;
  }
}

@media (max-width: 480px) {
  .w-8 {
    width: 1.5rem;
    height: 1.5rem;
  }

  button:not(.w-8) {
    padding: 0.25rem 0.375rem;
    font-size: 0.7rem;
  }
}

</style>


