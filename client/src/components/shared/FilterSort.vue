<script setup>
import { Filter, Menu, SortAsc, SortDesc, X } from "lucide-vue-next";
import { ref, watch, onMounted, nextTick } from "vue";
import { useBrandStore } from "@/stores/useBrandStore";
import { useProductStore } from "@/stores/useProductStore";
import { useRouter } from "vue-router";

const router = useRouter();
const emit = defineEmits(["update:filters"]);
const showBrandDropdown = ref(false);
const selectedBrands = ref([]);
const allBrands = ref([]);
const size = ref(10);
const sortField = ref("createdOn");
const sortDirection = ref("asc");

const productStore = useProductStore();
const brandStore = useBrandStore();
const fetchBrands = async () => {
  try {
    await brandStore.loadBrands(); 
    allBrands.value = brandStore.filterBrands()

    const stored = sessionStorage.getItem("filterAndSort");
    if (stored) {
      const parsed = JSON.parse(stored);
      const storedBrands = parsed.filterBrands || [];

      const validSelectedBrands = storedBrands.filter(b => allBrands.value.includes(b));
      const removed = storedBrands.filter(b => !allBrands.value.includes(b));

      if (removed.length > 0) {
        console.warn("Some brands were removed from the list:", removed);

        selectedBrands.value = validSelectedBrands;
        // ไม่มี productStore แล้ว เลยเอา activePage ไว้ที่ sessionStorage ตรง ๆ
        sessionStorage.setItem("activePage", 1);

        if (validSelectedBrands.length > 0) {
          sessionStorage.setItem("filterAndSort", JSON.stringify({
            ...parsed,
            filterBrands: validSelectedBrands,
            activePage: 1
          }));
        } else {
          sessionStorage.removeItem("filterAndSort");
        }
      }
    }
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
  sortField.value = "brand.name";
  sortDirection.value = "asc";
  showBrandDropdown.value = true;
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", 1);
};

const removeBrand = (brand) => {
  selectedBrands.value = selectedBrands.value.filter((b) => b !== brand);
  productStore.setActivePage(1);
  sessionStorage.setItem("activePage", 1);
};

const clearBrands = () => {
  selectedBrands.value = [];
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
  [() => selectedBrands, size, sortField, sortDirection],
  async () => {
    if (!productStore.products || productStore.products.length === 0) {
      // console.warn("Product ยังไม่โหลด ไม่ทำ filter");
      return;
    }

    const hasProductForSelectedBrands = selectedBrands.value.some(brand =>
      productStore.products.some(product => product.brandName === brand)
    );


    if (selectedBrands.value.length > 0 && !hasProductForSelectedBrands) {
      // console.warn("ยังไม่มีสินค้าแบรนด์ที่เลือก อาจต้อง reload ก่อน");
      return;
    }

    const filters = {
      filterBrands: selectedBrands.value,
      size: size.value,
      sortField: sortField.value,
      sortDirection: sortDirection.value,
      activePage: Number(sessionStorage.getItem("activePage")) || 1,
    };
    sessionStorage.setItem("filterAndSort", JSON.stringify(filters));
    emit("update:filters", filters);
  },
  { deep: true, immediate: true }
);



const add = () => {
  router.push({ name: "product-add" });
};

onMounted(() => {
  fetchBrands();
});
</script>



<template>
  <div class="flex flex-col sm:flex-row sm:items-start justify-between gap-4 w-full text-gray-700 relative">
    <!-- LEFT: Brand Filter -->
    <div class="relative flex flex-wrap items-center max-w-full sm:max-w-2xl w-full">
      <!-- Selected Brands -->
      <div class=" flex flex-wrap items-center content-center flex-1 border border-gray-300 rounded-md rounded-r-none bg-white min-h-[42px] px-2">
        <div v-for="brand in selectedBrands" :key="brand"
             class=" bg-gray-200 text-sm rounded-full px-3 py-1 mr-2 flex items-center shadow-sm">
          {{ brand }}
          <button @click="removeBrand(brand)" class="itbms-filter-item-clear ml-2 text-gray-500 hover:text-red-500 transition">
            <X class="w-3 h-3" />
          </button>
        </div>
      </div>

      <div class="relative flex-shrink-0 flex">
        <button @click="toggleBrandDropdown"
                class="itbms-brand-filter px-4 py-2 bg-gray-300 border border-gray-300 hover:bg-gray-400 transition rounded-none">
          <Filter class="w-5 h-5" />
        </button>

        <button @click="clearBrands"
                class="itbms-brand-filter-clear px-4 py-2 bg-gray-300 border border-gray-300 rounded-r-md hover:bg-red-400 transition">
          Clear
        </button>

        <ul v-if="showBrandDropdown"
            class="absolute top-full right-0 bg-white border border-gray-300 rounded-md shadow-lg mt-1 z-50 max-h-200 overflow-y-auto w-[200px] sm:w-[300px]">
          <li v-for="brand in allBrands" :key="brand" @click="addBrand(brand)"
              class="itbms-filter-item px-4 py-2 hover:bg-gray-100 cursor-pointer text-sm">
            {{ brand }}
          </li>
        </ul>
      </div>
    </div>

    <!-- RIGHT: Sorting + Size -->
    <div class="flex items-center justify-center gap-4 ">
      <div class="flex justify-start items-center gap-2">
        <label for="page-size" class="text-sm font-medium">Show</label>
        <select name="page-size" id="page-size" v-model="size"
                class="itbms-page-size bg-gray-300 border border-gray-300 rounded-md px-3 py-2 text-sm cursor-pointer focus:outline-none">
          <option value="5">5</option>
          <option value="10">10</option>
          <option value="20">20</option>
        </select>

        <button @click="resetSort" title="No Sort"
                :class="['itbms-brand-none bg-gray-300 border border-gray-300 rounded-md p-2 hover:bg-gray-400 transition', 
                         sortField === 'createdOn' ? 'bg-gray-500 text-white font-medium' : '']">
          <Menu class="w-5 h-5" />
        </button>

        <button @click="sortByBrandAsc" title="Sort A-Z"
                :class="['itbms-brand-asc bg-gray-300 border border-gray-300 rounded-md p-2 hover:bg-gray-400 transition',
                         sortField === 'brand.name' && sortDirection === 'asc' ? 'bg-gray-500 text-white font-medium' : '']">
          <SortAsc class="w-5 h-5" />
        </button>

        <button @click="sortByBrandDesc" title="Sort Z-A"
                :class="['itbms-brand-desc bg-gray-300 border border-gray-300 rounded-md p-2 hover:bg-gray-400 transition',
                         sortField === 'brand.name' && sortDirection === 'desc' ? 'bg-gray-500 text-white font-medium' : '']">
          <SortDesc class="w-5 h-5" />
        </button>
      </div>
       <button @click="add"
                class="itbms-sale-item-add text-sm  bg-white text-black font-medium py-2 px-6 rounded-md transition-colors duration-300 hover:bg-gray-200">
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