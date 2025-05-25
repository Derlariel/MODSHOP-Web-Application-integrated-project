<script setup>
import { Menu, SortAsc, SortDesc } from "lucide-vue-next";
import { ref, watch, onMounted } from "vue";
import { useProductStore } from "@/stores/useProductStore";
import { useRouter } from "vue-router";
import BrandSelector from "@/components/shared/BrandSelector.vue";
import { useBrandStore } from "@/stores/useBrandStore";

const router = useRouter();
const emit = defineEmits(["update:filters"]);
const BASE_URL = import.meta.env.VITE_BASE_URL;
const selectedBrands = ref([]);
const allBrands = ref([]);
const size = ref(10);
const sortField = ref("createdOn");
const sortDirection = ref("asc");
const brandStore = useBrandStore();
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

// Load stored filters and sorting
const stored = sessionStorage.getItem("filterAndSort");
if (stored) {
  try {
    const parsed = JSON.parse(stored);
    selectedBrands.value = parsed.filterBrands || [];
    size.value = parsed.size || 10;
    sortField.value = parsed.sortField || "createdOn";
    sortDirection.value = parsed.sortDirection || "asc";
    productStore.setActivePage(parsed.activePage || 1);
  } catch (e) {
    console.error("Invalid session data", e);
  }
}

const onBrandSelected = async () => {
  sortField.value = "brand.name";
  sortDirection.value = "asc";
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", "1");
  await productStore.loadProducts({
    filterBrands: selectedBrands.value,
    size: size.value,
    sortField: sortField.value,
    sortDirection: sortDirection.value,
    activePage: 1,
  });
  console.log("onBrandSelected: Loaded products for brands", selectedBrands.value);
};

const onBrandRemoved = async () => {
  sortField.value = "brand.name";
  sortDirection.value = "asc";
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", "1");
  await productStore.loadProducts({
    filterBrands: selectedBrands.value,
    size: size.value,
    sortField: sortField.value,
    sortDirection: sortDirection.value,
    activePage: 1,
  });
  console.log("onBrandRemoved: Loaded products for brands", selectedBrands.value);
};

const onBrandsClear = async () => {
  selectedBrands.value = [];
  sortField.value = "brand.name";
  sortDirection.value = "asc";
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", "1");
  await productStore.loadProducts({
    filterBrands: selectedBrands.value,
    size: size.value,
    sortField: sortField.value,
    sortDirection: sortDirection.value,
    activePage: 1,
  });
  console.log("onBrandsClear: Loaded products with no brand filter");
};

const resetSort = async () => {
  sortField.value = "createdOn";
  sortDirection.value = "asc";
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", "1");
  await productStore.loadProducts({
    filterBrands: selectedBrands.value,
    size: size.value,
    sortField: sortField.value,
    sortDirection: sortDirection.value,
    activePage: 1,
  });
  console.log("resetSort: Loaded products with createdOn sorting");
};

const sortByBrandAsc = async () => {
  sortField.value = "brand.name";
  sortDirection.value = "asc";
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", "1");
  await productStore.loadProducts({
    filterBrands: selectedBrands.value,
    size: size.value,
    sortField: sortField.value,
    sortDirection: sortDirection.value,
    activePage: 1,
  });
  console.log("sortByBrandAsc: Loaded products with brand.name asc sorting");
};

const sortByBrandDesc = async () => {
  sortField.value = "brand.name";
  sortDirection.value = "desc";
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", "1");
  await productStore.loadProducts({
    filterBrands: selectedBrands.value,
    size: size.value,
    sortField: sortField.value,
    sortDirection: sortDirection.value,
    activePage: 1,
  });
  console.log("sortByBrandDesc: Loaded products with brand.name desc sorting");
};

watch(
  [selectedBrands, size, sortField, sortDirection, () => productStore.activePage],
  async () => {
    sessionStorage.setItem(
      "filterAndSort",
      JSON.stringify({
        filterBrands: selectedBrands.value,
        size: size.value,
        sortField: sortField.value,
        sortDirection: sortDirection.value,
        activePage: productStore.activePage,
      })
    );
    emit("update:filters", {
      filterBrands: selectedBrands.value,
      size: size.value,
      sortField: sortField.value,
      sortDirection: sortDirection.value,
      activePage: productStore.activePage,
    });
    await productStore.loadProducts({
      filterBrands: selectedBrands.value,
      size: size.value,
      sortField: sortField.value,
      sortDirection: sortDirection.value,
      activePage: productStore.activePage,
    });
    console.log(
      "watch: Loaded products with",
      JSON.stringify({
        filterBrands: selectedBrands.value,
        size: size.value,
        sortField: sortField.value,
        sortDirection: sortDirection.value,
        activePage: productStore.activePage,
      })
    );
  },
  { deep: true }
);

const add = () => {
  router.push({ name: "product-add" });
};

onMounted(async () => {
  await fetchBrands();
  // Simulate browser close by resetting if no stored session
  if (!sessionStorage.getItem("filterAndSort")) {
    selectedBrands.value = [];
    sortField.value = "createdOn";
    sortDirection.value = "asc";
    productStore.setActivePage(1);
    sessionStorage.setItem("activePage", "1");
  }
  await productStore.loadProducts({
    filterBrands: selectedBrands.value,
    size: size.value,
    sortField: sortField.value,
    sortDirection: sortDirection.value,
    activePage: productStore.activePage,
  });
  console.log("onMounted: Loaded products with", {
    filterBrands: selectedBrands.value,
    size: size.value,
    sortField: sortField.value,
    sortDirection: sortDirection.value,
    activePage: productStore.activePage,
  });
});
</script>

<template>
  <div class="flex flex-col sm:flex-row sm:items-start justify-center gap-4 w-full text-gray-700 relative">
    <!-- LEFT: Brand Filter -->
    <BrandSelector
      v-model="selectedBrands"
      :brands="allBrands"
      :multiple="true"
      @brand-selected="onBrandSelected"
      @brand-removed="onBrandRemoved"
      @clear-brands="onBrandsClear"
    />

    <!-- RIGHT: Sorting + Size -->
    <div class="flex items-center justify-center gap-4">
      <div class="flex justify-start items-center gap-2">
        <label for="page-size" class="text-sm font-medium">Show</label>
        <select
          name="page-size"
          id="page-size"
          v-model="size"
          class="itbms-page-size bg-gray-300 border border-gray-300 rounded-md px-3 py-2 text-sm cursor-pointer focus:outline-none"
        >
          <option value="5">5</option>
          <option value="10">10</option>
          <option value="20">20</option>
        </select>

        <button
          @click="resetSort"
          title="No Sort"
          :class="[
            'itbms-brand-none bg-gray-300 border border-gray-300 rounded-md p-2 hover:bg-gray-400 transition',
            sortField === 'createdOn' ? 'bg-gray-500 text-white font-medium' : '',
          ]"
        >
          <Menu class="w-5 h-5" />
        </button>

        <button
          @click="sortByBrandAsc"
          title="Sort A-Z"
          :class="[
            'itbms-brand-asc bg-gray-300 border border-gray-300 rounded-md p-2 hover:bg-gray-400 transition',
            sortField === 'brand.name' && sortDirection === 'asc' ? 'bg-gray-500 text-white font-medium' : '',
          ]"
        >
          <SortAsc class="w-5 h-5" />
        </button>

        <button
          @click="sortByBrandDesc"
          title="Sort Z-A"
          :class="[
            'itbms-brand-desc bg-gray-300 border border-gray-300 rounded-md p-2 hover:bg-gray-400 transition',
            sortField === 'brand.name' && sortDirection === 'desc' ? 'bg-gray-500 text-white font-medium' : '',
          ]"
        >
          <SortDesc class="w-5 h-5" />
        </button>
      </div>
      <button
        @click="add"
        class="itbms-sale-item-add text-sm bg-white text-black font-medium py-2 px-6 rounded-md transition-colors duration-300 hover:bg-gray-200"
      >
        Add Product
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