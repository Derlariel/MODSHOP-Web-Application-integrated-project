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
</script>

<template>
  <div class="flex flex-col gap-4 w-full text-gray-700 ">
    <!-- Main Filter Row: Brand, Price, Storage + Clear Button -->
    <div class="flex flex-col sm:flex-row items-center justify-center gap-3 w-full border px-4 bg-gray-200 py-2  rounded-4xl ">
      <!-- LEFT: Brand Filter -->
      <div class="w-full sm:w-auto">
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
      <div class="w-full sm:w-auto">
        <PriceRangeSelector
          v-model="priceRange"
          @price-selected="onPriceSelected"
          @price-cleared="onPriceCleared"
        />
      </div>

      <!-- RIGHT: Storage Size Filter -->
      <div class="w-full sm:w-auto">
        <StorageSizeSelector
          v-model="selectedStorageSizes"
          @storage-selected="onStorageSelected"
          @storage-removed="onStorageRemoved"
          @clear-storage="onStorageClear"
        />
      </div>

      <!-- Clear All Button -->
      <div class="w-full sm:w-auto">
        <button 
          @click="clearAllFilters"
          class="itbms-brand-filter-clear w-full sm:w-auto px-6 py-2 bg-gray-700 text-white rounded-full hover:bg-gray-500 transition-colors duration-200 font-medium h-[42px]"
        >
          Clear
        </button>
      </div>
    </div>

    <!-- Sort Controls Row -->
    <div class="flex flex-col sm:flex-row items-center justify-end gap-4 w-full">
      <!-- Page Size -->
       <div class="flex items-center gap-2 border rounded-4xl px-4 py-2 bg-gray-200">
      <div>
        <label for="page-size" class="text-sm font-medium mx-2">Show</label>
        <select name="page-size" id="page-size" v-model="size"
                class="itbms-page-size bg-gray-400 text-black/80 border border-gray-300 rounded-md  text-sm cursor-pointer focus:outline-none">
          <option value="5">5</option>
          <option value="10">10</option>
          <option value="20">20</option>
        </select>
      </div>

      <!-- Sort Buttons -->
      <div class="flex items-center gap-2">
        <button @click="resetSort" title="No Sort"
                :class="['itbms-brand-none bg-gray-400 text-black rounded-md p-2 hover:bg-gray-400 transition', 
                         sortField === 'createdOn' ? 'bg-gray-600 text-white font-medium' : '']">
          <Menu class="w-5 h-5" />
        </button>

        <button @click="sortByBrandAsc" title="Sort A-Z"
                :class="['itbms-brand-asc bg-gray-400 text-black rounded-md p-2 hover:bg-gray-400 transition',
                         sortField === 'brand.name' && sortDirection === 'asc' ? 'bg-gray-600 text-white font-medium' : '']">
          <SortAsc class="w-5 h-5" />
        </button>

        <button @click="sortByBrandDesc" title="Sort Z-A"
                :class="['itbms-brand-desc bg-gray-400 text-black rounded-md p-2 hover:bg-gray-400 transition',
                         sortField === 'brand.name' && sortDirection === 'desc' ? 'bg-gray-600 text-white font-medium' : '']">
          <SortDesc class="w-5 h-5" />
        </button>
      </div>
    </div>
    </div>
  </div>
</template>

<style scoped>
.itbms-page-size {
  padding: 0.5rem 0.75rem;
  height: 40px;
  min-width: 70px;
  font-size: 1rem;
  line-height: 1.2;
  cursor: pointer;
}

.itbms-brand-none,
.itbms-brand-asc,
.itbms-brand-desc {
  padding: 0.5rem 0.75rem;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
}
</style>