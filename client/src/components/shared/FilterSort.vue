<script setup>
import { Menu, SortAsc, SortDesc } from "lucide-vue-next";
import { ref, watch, onMounted } from "vue";
import { useProductStore } from "@/stores/useProductStore";

import BrandSelector from "@/components/shared/BrandSelector.vue";
import PriceRangeSelector from "@/components/shared/PriceRangeSelector.vue";
import StorageSizeSelector from "@/components/shared/StorageSizeSelector.vue";

const emit = defineEmits(["update:filters"]);
const BASE_URL = import.meta.env.VITE_BASE_URL;
const selectedBrands = ref([]);
const allBrands = ref([]);
const lowerPrice = ref(null);
const upperPrice = ref(null);
const selectedStorageSizes = ref([]);
const size = ref(10);
const sortField = ref("createdOn");
const sortDirection = ref("asc");

const productStore = useProductStore();

const fetchBrands = async () => {
  try {
    const res = await fetch(`${BASE_URL}/v1/brands`);
    if (!res.ok) throw new Error("Failed to fetch brands");
    const data = await res.json();
    allBrands.value = data.map((b) => b.name).sort((a, b) => a.localeCompare(b));
  } catch (err) {
    console.error("Error fetching brands:", err);
  }
};

const stored = sessionStorage.getItem("filterAndSort");
if (stored) {
  try {
    const parsed = JSON.parse(stored);
    selectedBrands.value = parsed.filterBrands || [];
    lowerPrice.value = parsed.lowerPrice || null;
    upperPrice.value = parsed.upperPrice || null;
    selectedStorageSizes.value = parsed.storageSize || [];
    size.value = parsed.size || 10;
    sortField.value = parsed.sortField || "createdOn";
    sortDirection.value = parsed.sortDirection || "asc";
  } catch (e) {
    console.error("Invalid session data", e);
  }
}

const onBrandSelected = () => {
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", 1);
};

const onBrandRemoved = () => {
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", 1);
};

const onBrandsClear = () => {
  selectedBrands.value = [];
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", 1);
};

const onPriceSelected = () => {
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", 1);
};

const onPriceCleared = () => {
  lowerPrice.value = null;
  upperPrice.value = null;
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", 1);
};

const onStorageSelected = () => {
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", 1);
};

const onStorageRemoved = () => {
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", 1);
};

const onStorageClear = () => {
  selectedStorageSizes.value = [];
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", 1);
};

const clearAllFilters = () => {
  selectedBrands.value = [];
  lowerPrice.value = null;
  upperPrice.value = null;
  selectedStorageSizes.value = [];
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", 1);
};

const resetSort = () => {
  sortField.value = "createdOn";
  sortDirection.value = "asc";
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", 1);
};

const sortByBrandAsc = () => {
  sortField.value = "brand.name";
  sortDirection.value = "asc";
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", 1);
};

const sortByBrandDesc = () => {
  sortField.value = "brand.name";
  sortDirection.value = "desc";
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", 1);
};

watch(
  [selectedBrands, lowerPrice, upperPrice, selectedStorageSizes, size, sortField, sortDirection, () => productStore.activePage],
  () => {
    sessionStorage.setItem(
      "filterAndSort",
      JSON.stringify({
        filterBrands: selectedBrands.value,
        lowerPrice: lowerPrice.value,
        upperPrice: upperPrice.value,
        storageSize: selectedStorageSizes.value,
        size: size.value,
        sortField: sortField.value,
        sortDirection: sortDirection.value,
        activePage: productStore.activePage,
      })
    );
    emit("update:filters", {
      filterBrands: selectedBrands.value,
      lowerPrice: lowerPrice.value,
      upperPrice: upperPrice.value,
      storageSize: selectedStorageSizes.value,
      size: size.value,
      sortField: sortField.value,
      sortDirection: sortDirection.value,
      activePage: productStore.activePage,
    });
  },
  { deep: true, immediate: true }
);

// Simulate browser close by clearing sessionStorage for TC-4 Step 6
onMounted(() => {
  fetchBrands();
  // Check if this is a fresh session (simulating browser close)
  if (!sessionStorage.getItem("filterAndSort")) {
    selectedBrands.value = [];
    lowerPrice.value = null;
    upperPrice.value = null;
    selectedStorageSizes.value = [];
    sortField.value = "createdOn";
    sortDirection.value = "asc";
    productStore.setActivePage(1);
    sessionStorage.setItem("activePage", 1);
  }
});

// Expose clearAllFilters function for parent components
defineExpose({
  clearAllFilters
});
</script>

<template>
  <div class="w-full text-gray-700">
    <!-- Container for lg+ screens: side-by-side layout -->
    <div class="flex flex-col lg:flex-row lg:items-start lg:justify-between gap-4 w-full">
      
      <!-- Filter Section -->
      <div class="flex flex-col gap-2 lg:flex-1 lg:max-w-xl">
        <!-- Main Filter Row: Brand, Price, Storage + Clear Button -->
        <div class="flex flex-col md:flex-row items-stretch md:items-center justify-center gap-1.5 md:gap-2  w-full  px-2 md:px-3 py-1.5 md:py-2 rounded-xl md:rounded-2xl border bg-gray-200  lg:w-[38rem] xl:w-[48rem]">
          <!-- LEFT: Brand Filter -->
          <div class="w-full md:w-auto md:flex-1 lg:flex-none lg:w-[160px] xl:w-[200px]">
            <BrandSelector
              v-model="selectedBrands"
              :brands="allBrands"
              :multiple="true"
              @brand-selected="onBrandSelected"
              @brand-removed="onBrandRemoved"
              @clear-brands="onBrandsClear"
            />
          </div>

          <!-- CENTER: Price Filter -->
          <div class="w-full md:w-auto md:flex-1 lg:flex-none lg:w-[160px] xl:w-[200px]">
            <PriceRangeSelector
              v-model:lowerPrice="lowerPrice"
              v-model:upperPrice="upperPrice"
              @price-selected="onPriceSelected"
              @price-cleared="onPriceCleared"
            />
          </div>

          <!-- RIGHT: Storage Size Filter -->
          <div class="w-full md:w-auto md:flex-1 lg:flex-none lg:w-[160px] xl:w-[200px]">
            <StorageSizeSelector
              v-model="selectedStorageSizes"
              @storage-selected="onStorageSelected"
              @storage-removed="onStorageRemoved"
              @clear-storage="onStorageClear"
            />
          </div>

          <!-- Clear All Button -->
          <div class="w-full md:w-auto md:flex-shrink-0">
            <button 
              @click="clearAllFilters"
              class="itbms-brand-filter-clear w-full md:w-auto px-3 md:px-4 py-1.5 md:py-2 bg-gray-700 text-white rounded-full hover:bg-gray-500 transition-colors duration-200 font-medium text-xs md:text-sm h-[32px] md:h-[36px] whitespace-nowrap"
            >
              Clear
            </button>
          </div>
        </div>
      </div>

      <!-- Sort Controls Section -->
      <div class="flex flex-col sm:flex-row items-stretch sm:items-center justify-center sm:justify-end gap-2 sm:gap-3 w-full lg:w-auto lg:flex-shrink-0">
        <!-- Page Size -->
        <div class="flex items-center justify-center sm:justify-start gap-1.5 border rounded-xl md:rounded-2xl px-2 md:px-3 py-1.5 md:py-2 bg-gray-200">
          <div class="flex items-center gap-1.5">
            <label for="page-size" class="text-xs md:text-sm font-medium whitespace-nowrap">Show</label>
            <select name="page-size" id="page-size" v-model="size"
                    class="itbms-page-size bg-gray-400/70 text-black/70 border border-gray-300 rounded-md text-xs md:text-sm cursor-pointer focus:outline-none px-1.5 md:px-2 py-0.5 md:py-1">
              <option value="5">5</option>
              <option value="10">10</option>
              <option value="20">20</option>
            </select>
          </div>

          <!-- Sort Buttons -->
          <div class="flex items-center gap-1 md:gap-1.5 ml-1.5 md:ml-2">
            <button @click="resetSort" title="No Sort"
                    :class="['itbms-brand-none bg-gray-400/70 text-black/70 rounded-md px-1.5 py-0.5 md:px-2 md:py-1 hover:bg-gray-400 transition', 
                             sortField === 'createdOn' ? 'bg-gray-600 text-white font-medium' : '']">
              <Menu class="w-3 h-3 md:w-4 md:h-4" />
              <span class="ml-0.5 text-xs font-bold hidden sm:inline">DEFAULT</span>
            </button>

            <button @click="sortByBrandAsc" title="Sort A-Z"
              :class="['itbms-brand-asc bg-gray-400/70 text-black/70 rounded-md px-1.5 py-0.5 md:px-2 md:py-1 hover:bg-gray-400 transition',
                 sortField === 'brand.name' && sortDirection === 'asc' ? 'bg-gray-600 text-white font-medium' : '']">
              <SortAsc class="w-3 h-3 md:w-4 md:h-4"/>
              <span class="ml-0.5 text-xs font-bold hidden sm:inline">A-Z</span>
            </button>

            <button @click="sortByBrandDesc" title="Sort Z-A"
                    :class="['itbms-brand-desc bg-gray-400/70 text-black/70 rounded-md px-1.5 py-0.5 md:px-2 md:py-1 hover:bg-gray-400 transition',
                             sortField === 'brand.name' && sortDirection === 'desc' ? 'bg-gray-600 text-white font-medium' : '']">
              <SortDesc class="w-3 h-3 md:w-4 md:h-4" />
              <span class="ml-0.5 text-xs font-bold hidden sm:inline">Z-A</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.itbms-page-size {
  min-width: 45px;
  font-size: 0.75rem;
  line-height: 1.2;
  cursor: pointer;
}

@media (min-width: 768px) {
  .itbms-page-size {
    padding: 0.25rem 0.5rem;
    min-width: 60px;
    font-size: 0.875rem;
  }
}

.itbms-brand-none,
.itbms-brand-asc,
.itbms-brand-desc {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 28px;
  min-width: 28px;
}

@media (min-width: 768px) {
  .itbms-brand-none,
  .itbms-brand-asc,
  .itbms-brand-desc {
    padding: 0.25rem 0.5rem;
    min-height: 32px;
    min-width: auto;
  }
}

/* Mobile optimization */
@media (max-width: 640px) {
  .itbms-brand-filter-clear {
    font-size: 0.875rem;
  }
}
</style>