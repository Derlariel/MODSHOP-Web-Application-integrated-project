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
const priceRange = ref({ min: null, max: null });
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
    priceRange.value = parsed.priceRange || { min: null, max: null };
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
  priceRange.value = { min: null, max: null };
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
  priceRange.value = { min: null, max: null };
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
  [selectedBrands, priceRange, selectedStorageSizes, size, sortField, sortDirection, () => productStore.activePage],
  () => {
    sessionStorage.setItem(
      "filterAndSort",
      JSON.stringify({
        filterBrands: selectedBrands.value,
        priceRange: priceRange.value,
        storageSize: selectedStorageSizes.value,
        size: size.value,
        sortField: sortField.value,
        sortDirection: sortDirection.value,
        activePage: productStore.activePage,
      })
    );
    emit("update:filters", {
      filterBrands: selectedBrands.value,
      priceRange: priceRange.value,
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
    priceRange.value = { min: null, max: null };
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
  <div class="flex flex-col gap-3 md:gap-4 w-full text-gray-700">
    <!-- Main Filter Row: Brand, Price, Storage + Clear Button -->
    <div class="flex flex-col md:flex-row items-stretch md:items-center justify-center gap-2 md:gap-3 w-full px-3 md:px-4  py-2 md:py-3 rounded-2xl md:rounded-3xl">
      <!-- LEFT: Brand Filter -->
      <div class="w-full md:w-auto md:flex-1 lg:flex-none lg:w-[160px] xl:w-[180px]">
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
      <div class="w-full md:w-auto md:flex-1 lg:flex-none lg:w-[1ุ60px] xl:w-[180px]">
        <PriceRangeSelector
          v-model="priceRange"
          @price-selected="onPriceSelected"
          @price-cleared="onPriceCleared"
        />
      </div>

      <!-- RIGHT: Storage Size Filter -->
      <div class="w-full md:w-auto md:flex-1 lg:flex-none lg:w-[1ุ60px] xl:w-[180px]">
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
          class="itbms-brand-filter-clear w-full md:w-auto px-4 md:px-6 py-2 md:py-2.5 bg-gray-700 text-white rounded-full hover:bg-gray-500 transition-colors duration-200 font-medium text-sm md:text-base h-[38px] md:h-[42px] whitespace-nowrap"
        >
          Clear
        </button>
      </div>
    </div>

    <!-- Sort Controls Row -->
    <div class="flex flex-col sm:flex-row items-stretch sm:items-center justify-center sm:justify-end gap-3 sm:gap-4 w-full">
      <!-- Page Size -->
      <div class="flex items-center justify-center sm:justify-start gap-2 border rounded-2xl md:rounded-3xl px-3 md:px-4 py-2 md:py-2.5 bg-gray-200">
        <div class="flex items-center gap-2">
          <label for="page-size" class="text-xs md:text-sm font-medium whitespace-nowrap">Show</label>
          <select name="page-size" id="page-size" v-model="size"
                  class="itbms-page-size bg-gray-400/70 text-black/70 border border-gray-300 rounded-md text-xs md:text-sm cursor-pointer focus:outline-none px-2 md:px-3 py-1 md:py-2">
            <option value="5">5</option>
            <option value="10">10</option>
            <option value="20">20</option>
          </select>
        </div>

        <!-- Sort Buttons -->
        <div class="flex items-center gap-1 md:gap-2 ml-2 md:ml-3">
          <button @click="resetSort" title="No Sort"
                  :class="['itbms-brand-none bg-gray-400/70 text-black/70 rounded-md px-2 py-1 md:p-2 hover:bg-gray-400 transition', 
                           sortField === 'createdOn' ? 'bg-gray-600 text-white font-medium' : '']">
            <Menu class="w-4 h-4 md:w-5 md:h-5" />
            <span class="ml-1 text-xs font-bold hidden sm:inline">DEFAULT</span>
          </button>

          <button @click="sortByBrandAsc" title="Sort A-Z"
            :class="['itbms-brand-asc bg-gray-400/70 text-black/70 rounded-md px-2 py-1 md:p-2 hover:bg-gray-400 transition',
               sortField === 'brand.name' && sortDirection === 'asc' ? 'bg-gray-600 text-white font-medium' : '']">
            <SortAsc class="w-4 h-4 md:w-5 md:h-5"/>
            <span class="ml-1 text-xs font-bold hidden sm:inline">A-Z</span>
          </button>

          <button @click="sortByBrandDesc" title="Sort Z-A"
                  :class="['itbms-brand-desc bg-gray-400/70 text-black/70 rounded-md px-2 py-1 md:p-2 hover:bg-gray-400 transition',
                           sortField === 'brand.name' && sortDirection === 'desc' ? 'bg-gray-600 text-white font-medium' : '']">
            <SortDesc class="w-4 h-4 md:w-5 md:h-5" />
            <span class="ml-1 text-xs font-bold hidden sm:inline">Z-A</span>
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.itbms-page-size {
  min-width: 50px;
  font-size: 0.75rem;
  line-height: 1.2;
  cursor: pointer;
}

@media (min-width: 768px) {
  .itbms-page-size {
    padding: 0.5rem 0.75rem;
    min-width: 70px;
    font-size: 0.875rem;
  }
}

.itbms-brand-none,
.itbms-brand-asc,
.itbms-brand-desc {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 32px;
  min-width: 32px;
}

@media (min-width: 768px) {
  .itbms-brand-none,
  .itbms-brand-asc,
  .itbms-brand-desc {
    padding: 0.5rem 0.75rem;
    min-height: 40px;
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