```vue
<script setup>
<<<<<<< HEAD

=======
>>>>>>> e04638aef388dc62bd983d7cc55b27191fb73b67
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

const shouldShowPagination = computed(() => {
  return storePages.value > 1;
});

const emit = defineEmits(["sendPages"]);

const visiblePages = computed(() => {
  const total = productStore.allPages;
  const current = productStore.getActivePage;
  const maxVisible = 10; // Maximum number of visible pages

  // If total pages are less than or equal to maxVisible, show all pages
  if (total <= maxVisible) {
    return Array.from({ length: total }, (_, i) => i + 1);
  }

  // Keep range fixed at 1 to maxVisible until current page exceeds maxVisible
  let start = 1;
  let end = maxVisible;

  // Start shifting the range when current page exceeds maxVisible
  if (current > maxVisible) {
    start = current - maxVisible + 1;
    end = current;

    // Ensure the range doesn't exceed total pages
    if (end > total) {
      end = total;
      start = Math.max(1, end - maxVisible + 1);
    }
  }

  return Array.from({ length: end - start + 1 }, (_, i) => start + i);
});


const setActivePage = (page) => {
  const maxPage = storePages.value;

  if (page > maxPage) {
    productStore.setActivePage(1);
  } else {
    productStore.setActivePage(page);
  }

  sessionStorage.setItem("activePage", String(productStore.getActivePage));
  console.log("Active Page:", productStore.getActivePage, "Visible Pages:", visiblePages.value);
  emit("sendPages", productStore.getActivePage - 1);
};

const first = () => {
  setActivePage(1);
};

const last = () => {
  console.log("last", productStore.allPages);
  setActivePage(productStore.allPages);
  console.log("last-set", productStore.getActivePage);
  console.log("session", sessionStorage.getItem("activePage"));
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
      setActivePage(1);
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
  <div
    v-show="shouldShowPagination"
    class="pagination-wrapper w-full py-4 px-2 bg-gray-950 flex justify-center"
  >
    <div class="pagination-container flex items-center gap-1">
      <!-- First Button -->
      <button
        @click="first"
        :disabled="activePage === 1"
        class="pagination-btn itbms-page-first"
        :class="{ 'pagination-btn-disabled': activePage === 1 }"
        aria-label="First"
      >⏮</button>

      <!-- Previous Button -->
      <button
        @click="prev"
        :disabled="activePage === 1"
        class="pagination-btn itbms-page-prev"
        :class="{ 'pagination-btn-disabled': activePage === 1 }"
        aria-label="Previous"
      >‹</button>

      <!-- Page Numbers -->
      <template v-for="page in visiblePages" :key="page">
        <button
          @click="setActivePage(page)"
          :class="[
            `pagination-btn itbms-page-${page - 1}`,
            {
              'pagination-btn-active': activePage === page,
              'pagination-btn-disabled': activePage === page
            }
          ]"
          :aria-current="activePage === page ? 'page' : undefined"
        >
<<<<<<< HEAD
          {{ page }}
        </button>
      </template>
=======
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M15 19l-7-7 7-7"
          />
        </svg>
      </button>

      <!-- Page Numbers (Desktop) -->
      <div class="desktop-pagination flex items-center space-x-1 no-wrap">
        <button
          v-for="(page, index) in visiblePages"
          :key="page"
          @click="setActivePage(page)"
          :class="[
            `itbms-page-${index}`,
            'w-8 h-8 flex items-center justify-center text-sm font-medium rounded-md transition-all duration-200 ease-out',
            activePage === page
              ? 'bg-white text-white shadow-sm'
              : 'text-gray-400 hover:text-white hover:bg-gray-800 active:bg-gray-700',
          ]"
          :aria-label="`Page ${page}`"
          :aria-current="activePage === page ? 'page' : undefined"
        >
          {{ page }}
        </button>
      </div>

      <!-- Page Indicator (Mobile) -->
      <div class="mobile-pagination page-indicator">
        <span class="px-3 py-1 bg-gray-800 rounded-md text-white">
          {{ activePage }} / {{ storePages }}
        </span>
      </div>
>>>>>>> e04638aef388dc62bd983d7cc55b27191fb73b67

      <!-- Next Button -->
      <button
        @click="next"
        :disabled="activePage >= storePages"
        class="pagination-btn itbms-page-next"
        :class="{ 'pagination-btn-disabled': activePage >= storePages }"
        aria-label="Next"
      >›</button>

      <!-- Last Button -->
      <button
        @click="last"
        :disabled="activePage >= storePages"
        class="pagination-btn itbms-page-last"
        :class="{ 'pagination-btn-disabled': activePage >= storePages }"
        aria-label="Last"
      >⏭</button>
    </div>
  </div>
</template>

<style scoped>
.pagination-wrapper {
  background: #090c13;
  padding: 1rem 0.5rem;
  width: 100%;
  overflow-x: auto;
}
.pagination-container {
  gap: 0.25rem;
  flex-wrap: nowrap;
  overflow-x: auto;
  min-width: max-content;
}
.pagination-btn {
  width: 2.2rem;
  min-width: 2.2rem;
  height: 2.2rem;
  border-radius: 50%;
  background: transparent;
  color: #cbd5e1;
  border: 1.5px solid #222;
  font-weight: 500;
  font-size: 1rem;
  transition: all 0.15s;
  display: flex;
  align-items: center;
  justify-content: center;
}
.pagination-btn:hover:not(:disabled),
.pagination-btn:focus:not(:disabled) {
  background: #1e293b;
  color: #fff;
  border-color: #fff;
  box-shadow: 0 2px 8px 0 #0002;
}
.pagination-btn-active {
  background: #2563eb;
  color: #fff;
  border-color: #2563eb;
  box-shadow: 0 2px 8px 0 #2563eb44;
}
.pagination-btn-disabled {
  color: #64748b;
  background: #181e29;
  border-color: #181e29;
  cursor: not-allowed;
  pointer-events: none;
}

@media (max-width: 640px) {
  .pagination-btn {
    width: 1.7rem;
    min-width: 1.7rem;
    height: 1.7rem;
    font-size: 0.9rem;
    padding: 0.25rem;
  }
  .pagination-container {
    gap: 0.15rem;
  }
}
</style>
```