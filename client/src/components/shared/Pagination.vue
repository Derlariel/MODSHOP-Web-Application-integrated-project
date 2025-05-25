<script setup>
import ListModel from "./ListModel.vue";
import { ref, defineProps, defineEmits, computed, watch, onMounted } from "vue";

const props = defineProps({
  totalPages: {
    type: Number,
    required: true,
  },
});

import { useProductStore } from "@/stores/useProductStore";
const productStore = useProductStore();
const storePages = computed(() => productStore.allPages);
const activePage = computed(() => productStore.getActivePage);

const emit = defineEmits(["sendPages"]);

const visiblePages = computed(() => {
  const total = productStore.allPages;
  const current = productStore.getActivePage;

  const maxVisible = 10;
  let start = Math.max(1, current - Math.floor(maxVisible / 2));
  let end = start + maxVisible - 1;

  if (end > total) {
    end = total;
    start = Math.max(1, end - maxVisible + 1);
  }

  return Array.from({ length: end - start + 1 }, (_, i) => start + i);
});

const paging = ref("paging");

const setActivePage = (page) => {
  const maxPage = storePages.value;

  if (page > maxPage) {
    productStore.setActivePage(1);
  } else {
    productStore.setActivePage(page);
  }

  sessionStorage.setItem("activePage", String(productStore.getActivePage));
  emit("sendPages", productStore.getActivePage - 1);
};

const first = () => {
  setActivePage(1); 
};

const last = () => {
  setActivePage(storePages.value); 
};

const next = () => {
  if (productStore.getActivePage < storePages.value) {
    setActivePage(productStore.getActivePage + 1);
  }
};

const prev = () => {
  if (productStore.getActivePage > 1) {
    setActivePage(productStore.getActivePage - 1); 
  }
};

watch(
  () => productStore.allPages,
  (newVal) => {
    console.log("max-emit", newVal);
    console.log("before-emit", productStore.allPages);
    if (productStore.allPages < productStore.getActivePage) {
      setActivePage(1)
    }
  }
);

onMounted(() => {
  const savedPage = sessionStorage.getItem("activePage");
  if (savedPage) {
    setActivePage(parseInt(savedPage));
  }
});
</script>

<template>
  <div class="pagination-wrapper w-full py-6 px-4 bg-gray-950 overflow-x-auto">
    <div class="pagination-container flex items-center space-x-2 min-w-max mx-auto">
      <!-- First Button -->
      <button
        @click="first"
        :disabled="activePage === 1"
        :class="[
          'itbms-page-first px-2 py-1 text-sm font-medium rounded-md transition-all duration-200 ease-out',
          activePage === 1
            ? 'text-gray-500 cursor-not-allowed'
            : 'text-gray-300 hover:text-white hover:bg-gray-800 active:bg-gray-700'
        ]"
      >
        First
      </button>

      <!-- Previous Button -->
      <button
        @click="prev"
        :disabled="activePage === 1"
        :class="[
          'itbms-page-prev w-8 h-8 flex items-center justify-center rounded-md transition-all duration-200 ease-out',
          activePage === 1
            ? 'text-gray-500 cursor-not-allowed'
            : 'text-gray-300 hover:text-white hover:bg-gray-800 active:bg-gray-700'
        ]"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
        </svg>
      </button>

      <!-- Page Numbers (Desktop) -->
      <div class="desktop-pagination flex items-center space-x-1 no-wrap">
        <ListModel :saleItems="visiblePages" :paging="paging">
          <template #listItems="{ Item: page }">
            <button
              @click="setActivePage(page)"
              :class="[
                `itbms-page-${page - 1}`,
                'w-8 h-8 flex items-center justify-center text-sm font-medium rounded-md transition-all duration-200 ease-out',
                activePage === page
                  ? 'bg-white text-white shadow-sm'
                  : 'text-gray-400 hover:text-white hover:bg-gray-800 active:bg-gray-700'
              ]"
              :aria-label="`Page ${page}`"
              :aria-current="activePage === page ? 'page' : undefined"
            >
              {{ page }}
            </button>
          </template>
        </ListModel>
      </div>

      <!-- Page Indicator (Mobile) -->
      <div class="mobile-pagination page-indicator">
        <span class="px-3 py-1 bg-gray-800 rounded-md text-white">
          {{ activePage }} / {{ storePages }}
        </span>
      </div>

      <!-- Next Button -->
      <button
        @click="next"
        :disabled="activePage >= storePages"
        :class="[
          'itbms-page-next w-8 h-8 flex items-center justify-center rounded-md transition-all duration-200 ease-out',
          activePage >= storePages
            ? 'text-gray-500 cursor-not-allowed'
            : 'text-gray-300 hover:text-white hover:bg-gray-800 active:bg-gray-700'
        ]"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5l7 7-7 7"/>
        </svg>
      </button>

      <!-- Last Button -->
      <button
        @click="last"
        :disabled="activePage >= storePages"
        :class="[
          'itbms-page-last px-2 py-1 text-sm font-medium rounded-md transition-all duration-200 ease-out',
          activePage >= storePages
            ? 'text-gray-500 cursor-not-allowed'
            : 'text-gray-300 hover:text-white hover:bg-gray-800 active:bg-gray-700'
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

.page-indicator {
  color: white;
  font-size: 0.875rem;
  font-weight: 500;
}

button:hover:not(:disabled) {
  transform: translateY(-0.5px);
}

button:active:not(:disabled) {
  transform: translateY(0);
}

button:focus:not(:disabled) {
  outline: 2px solid rgba(255, 255, 255, 0.1);
  outline-offset: 2px;
}

button {
  will-change: transform, background-color, color;
  border: none;
  background: transparent;
}

button[aria-current="page"] {
  box-shadow: 0 0 0 1px rgba(255, 255, 255, 0.1);
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