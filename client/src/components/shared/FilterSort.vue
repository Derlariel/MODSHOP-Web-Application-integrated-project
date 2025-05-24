<script setup>
import { Filter, Menu, SortAsc, SortDesc, X } from "lucide-vue-next";
import { ref, watch, onMounted, computed } from "vue";

const emit = defineEmits(["update:filters"]);
const showBrandDropdown = ref(false);
const BASE_URL = import.meta.env.VITE_BASE_URL;
const selectedBrands = ref([]);
const allBrands = ref([]);
const size = ref(10);
const sortField = ref("createdOn");
const sortDirection = ref("asc");

import { useProductStore } from "@/stores/useProductStore";
const productStore = useProductStore();

const fetchBrands = async () => {
  try {
    const res = await fetch(`${BASE_URL}/v1/brands`);
    if (!res.ok) throw new Error("Failed to fetch brands");
    const data = await res.json();
    allBrands.value = data.map((b) => b.name);
  } catch (err) {
    console.error("Error fetching brands:", err);
  }
};

  const stored = sessionStorage.getItem("filterAndSort");
if (stored) {
  try {
    const parsed = JSON.parse(stored);
    selectedBrands.value = parsed.filterBrands || [];
    size.value = parsed.size || 10;
    sortField.value = parsed.sortField || "createdOn";
    sortDirection.value = parsed.sortDirection || "asc";
  } catch (e) {
    console.error("Invalid session data", e);
  }
}

const toggleBrandDropdown = () => {
  showBrandDropdown.value = !showBrandDropdown.value;
};

const addBrand = (brand) => {
  if (!selectedBrands.value.includes(brand)) {
    selectedBrands.value.push(brand);
    
  }
  showBrandDropdown.value = false;
};

const removeBrand = (brand) => {
  selectedBrands.value = selectedBrands.value.filter((b) => b !== brand);
};

const clearBrands = () => {
  selectedBrands.value = [...[]]; //
};

const resetSort = () => {
  sortField.value = "createdOn";
  sortDirection.value = "asc";
  productStore.setActivePage(1)
  sessionStorage.setItem("activePage", 1)
};

const sortByBrandAsc = () => {
  sortField.value = "brand.name";
  sortDirection.value = "asc";
   productStore.setActivePage(1)
   sessionStorage.setItem("activePage", 1)
};

const sortByBrandDesc = () => {
  sortField.value = "brand.name";
  sortDirection.value = "desc";
  productStore.setActivePage(1)
  sessionStorage.setItem("activePage", 1)
};



watch(
  [selectedBrands, size, sortField, sortDirection],
  () => {
    sessionStorage.setItem(
      "filterAndSort",
      JSON.stringify({
        filterBrands: selectedBrands.value,
        size: size.value,
        sortField: sortField.value,
        sortDirection: sortDirection.value,
      })
    );
    emit("update:filters", {
      filterBrands: selectedBrands.value,
      size: size.value,
      sortField: sortField.value,
      sortDirection: sortDirection.value,
    });
  },
  { deep: true, immediate: true }
);

onMounted(() => {
  fetchBrands();
});
</script>

<template>
  <div class="text-gray-700 flex justify-between items-center">
    <!-- Left: Input box + Filter button + Clear -->
    <div class="flex w-full max-w-2xl relative">
      <div
        class="itbms-brand-filter flex-1 border border-gray-300 rounded-l-md p-2 bg-white flex flex-wrap items-center min-h-[42px]"
      >
        <div
          v-for="brand in selectedBrands"
          :key="brand"
          class="itbms-filter-item bg-gray-200 text-sm rounded-full px-3 py-1 mr-2 mb-1 flex items-center"
        >
          {{ brand }}
          <button
            @click="removeBrand(brand)"
            class="itbms-filter-item-clear ml-2 text-gray-500 hover:text-red-500"
          >
            <X class="w-3 h-3" />
          </button>
        </div>
      </div>

      <!-- Filter Button -->
      <button
        @click="toggleBrandDropdown"
        class="px-4 py-2 bg-gray-300 border-y border-gray-300 hover:bg-gray-400 transition"
      >
        <Filter class="w-4 h-4 mr-2" />
      </button>

      <!-- Clear Button -->
      <button
        @click="clearBrands"
        class="px-4 py-2 bg-gray-300 border border-gray-300 border-l-0 rounded-r-md hover:bg-red-400 transition"
      >
        Clear
      </button>

      <!-- Dropdown -->
      <ul
        v-if="showBrandDropdown"
        class="absolute top-full left-0 w-full bg-white border border-gray-300 rounded-md shadow-lg mt-1 z-50 max-h-48 overflow-y-auto"
      >
        <li
          v-for="brand in allBrands"
          :key="brand"
          @click="addBrand(brand)"
          class="px-4 py-2 hover:bg-gray-100 cursor-pointer text-sm"
        >
          {{ brand }}
        </li>
      </ul>
    </div>

    <!-- Right: Sorting and page size -->
    <div class="flex items-center ml-4">
      <label for="page-size" class="mr-2 text-white font-medium">Show</label>
      <select
        name="page-size"
        id="page-size"
        v-model="size"
        class="itbms-page-size bg-gray-300 border-y border-gray-300 rounded-l-sm"
      >
        <option value="5">5</option>
        <option value="10">10</option>
        <option value="20">20</option>
      </select>

      <button
        @click="resetSort"
        title="No Sort"
        class="itbms-brand-none bg-gray-300 border border-gray-300 rounded-none border-l-0 hover:bg-gray-400 transition"
      >
        <Menu class="w-5 h-5" />
      </button>

      <button
        @click="sortByBrandAsc"
        class="itbms-brand-asc bg-gray-300 border border-gray-300 rounded-none border-l-0 hover:bg-gray-400 transition"
        title="Sort By Brand (A-Z)"
      >
        <SortAsc class="w-5 h-5" />
      </button>

      <button
        @click="sortByBrandDesc"
        class="itbms-brand-desc bg-gray-300 border border-gray-300 rounded-r-sm border-l-0 hover:bg-gray-400 transition"
        title="Sort By Brand (Z-A)"
      >
        <SortDesc class="w-5 h-5" />
      </button>
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
