<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRouter, useRoute } from "vue-router";
import { useProductStore } from "@/stores/useProductStore";

import ListModel from "@/components/shared/ListModel.vue";
import FilterSort from "@/components/shared/FilterSort.vue";
import SuccessModal from "@/components/shared/modal/SuccessModal.vue";
import Pagination from "@/components/shared/Pagination.vue";
import ErrorModal from "@/components/shared/modal/ErrorModal.vue";
import DEFAULT_IMAGE from "@/assets/default.jpg";
import SkeletonLoader from "@/components/shared/SkeletonLoader.vue";

const router = useRouter();
const route = useRoute();

const productStore = useProductStore();

const product = computed(() => productStore.allProducts);
const totalPages = ref(0);

const isLoading = ref(true);
const isModalOpen = ref(false);
const showSuccess = ref(false);
const alertMessage = ref("");
const adminMode = ref(false);
const noProductsFromFilter = ref(false);

const filters = ref({
  page: 0,
  filterBrands: [],
  lowerPrice: null,
  upperPrice: null,
  isExactPrice: false,
  storageSize: [],
  size: 10,
  sortField: "createdOn",
  sortDirection: "asc",
});

const trigger = ref(0)

const props = defineProps({
  viewType: {
    type: String,
    default: "gallery",
  },
});


const detail = (productId) => {
  router.push({
    name: "product-detail",
    params: { productId },
  });
};

async function initProducts() {
  isLoading.value = true;
  noProductsFromFilter.value = false;
  try {
    await productStore.loadAllPages(filters.value);
    await productStore.loadProductsPage(filters.value);
    totalPages.value = productStore.allPages;
    console.log(totalPages.value);

    // Check if no products found and filters are applied
    if (product.value.length === 0) {
      const hasFilters = filters.value.filterBrands.length > 0 ||
        filters.value.lowerPrice !== null ||
        filters.value.upperPrice !== null ||
        filters.value.storageSize.length > 0;

      
      if (hasFilters) {
        // If filters are applied and no results, show no products message
        noProductsFromFilter.value = true;
      } else {
        // If no filters and no results, show general error
        router.push({ name: "error-page", query: { code: "500" } });
        return;
      }
    }
  } catch (err) {
    console.error("Load products failed:", err);
    // On API error, redirect to error page
    router.push({ name: "error-page", query: { code: "500" } });
  } finally {
    isLoading.value = false;
  }
}

const updatePages = (pages) => {
  productStore.setActivePage(pages)
  ++trigger.value
}



const updateFilters = (newFilters) => {
  filters.value.page = newFilters.activePage - 1
  Object.assign(filters.value, newFilters)
}

function handleModalClose() {
  isModalOpen.value = false;
  sessionStorage.removeItem("error-message");
  router.push({ name: "sale-items" });
}



const add = () => {
  router.push({ name: "product-add" });
};

const salItemList = () => {
  router.push({ name: "product-list" });
}

onMounted(async () => {
  // Check if filters were cleared due to NODATA error
  const filtersCleared = sessionStorage.getItem("filters-cleared-from-error");
  if (filtersCleared) {
    sessionStorage.removeItem("filters-cleared-from-error");
    // Don't load products yet, let the cleared filters trigger reload
    return;
  }

  await initProducts();
  const savedPage = localStorage.getItem("activePage");
  if (savedPage) {
    productStore.setActivePage(parseInt(savedPage));
  }

  if (sessionStorage.getItem("error-message") === "true") {
    isModalOpen.value = true;
  }

  if (sessionStorage.getItem("add-success") === "true") {
    alertMessage.value = "The sale item has been successfully added.";
    showSuccess.value = true;
    sessionStorage.removeItem("add-success");
    setTimeout(() => {
      showSuccess.value = false;
    }, 2000);
  }

  if (sessionStorage.getItem("delete-success") === "true") {
    alertMessage.value = "The sale item has been deleted.";
    showSuccess.value = true;
    sessionStorage.removeItem("delete-success");
    productStore.setActivePage(1)
    localStorage.setItem("activePage", 1)
    setTimeout(() => {
      showSuccess.value = false;
    }, 2000);
  }
});

watch(filters, async () => {
  initProducts();
}, { deep: true, immediate: true })


watch(trigger, async () => {
  initProducts();
}, { deep: true, immediate: true })


</script>

<template>
  <div class="min-h-screen bg-black text-white">
    <div class="pt-24 pb-4 px-6 bg-gradient-to-b from-neutral-900 to-black">
      <div class="max-w-[1200px] mx-auto text-center">
        <h1 class="text-5xl font-semibold tracking-tight mb-2">
          Store The best way to buy the products you love.
        </h1>
        <p class="text-xl text-gray-400 mt-4 max-w-2xl mx-auto">
          Explore premium devices with cutting-edge technology and elegant design.
        </p>

        <SuccessModal :message="alertMessage" :visible="showSuccess" />

        <!-- Buttons Row -->
        <div class="mt-6 flex flex-row gap-2 xs:gap-3 w-full justify-center lg:justify-start">
          <button @click="add"
            class="itbms-sale-item-add text-xs xs:text-sm md:text-sm bg-white text-black font-medium py-2 xs:py-2.5 md:py-2 px-4 xs:px-6 md:px-5 rounded-lg transition-colors duration-300 hover:bg-gray-200 whitespace-nowrap">
            Add Sale Item
          </button>
          <button @click="salItemList"
            class="itbms-item-list text-xs xs:text-sm md:text-sm bg-white text-black font-medium py-2 xs:py-2.5 md:py-2 px-4 xs:px-6 md:px-5 rounded-lg transition-colors duration-300 hover:bg-gray-200 whitespace-nowrap">
            Sale Items List
          </button>
        </div>

        <!-- Filter and Sort Section -->
        <div class="mt-4 w-full">
          <FilterSort @update:filters="updateFilters" />
        </div>

      </div>
    </div>

    <div class="max-w-[1200px] mx-auto px-6 pb-20">
      <ErrorModal :visible="isModalOpen" message="The requested sale item does not exist." @close="handleModalClose" />

      <!-- Loading Skeletons -->
      <div v-if="isLoading" class="flex items-center justify-center ">
        <SkeletonLoader />
      </div>

      <div v-if="!isLoading && !isModalOpen && !noProductsFromFilter" class="flex-1 mb-12">
        <ListModel :saleItems="productStore.allProducts" :viewType="viewType" :adminMode="adminMode">
          <!-- Header -->
          <template #listHeader>
            <div v-if="viewType === 'list'"
              class="grid grid-cols-7 gap-4 py-3 px-4 border-b border-neutral-800 text-gray-400 text-sm font-medium">
              <span>IMAGE</span>
              <span>BRAND</span>
              <span>MODEL</span>
              <span>RAM</span>
              <span>STORAGE</span>
              <span>PRICE</span>
              <span>ACTION</span>
            </div>
          </template>

          <!-- Items -->
          <template #listItems="{ Item: product, viewType }">
            <!-- Gallery View -->
            <div v-if="viewType === 'gallery'" @click="detail(product.id)"
              class="itbms-row group cursor-pointer transform transition-all duration-500 hover:scale-[1.0] gap-8 rounded-md py-2">
              <div
                class="relative h-[300px] rounded-2xl overflow-hidden bg-gradient-to-br from-white to-neutral-100 mb-4 perspective group-hover:shadow-2xl group-hover:shadow-white/30 transition-shadow duration-700">
                <div class="absolute inset-0 flex items-center justify-center transition-transform duration-700 py-6 ">
                  <img :src="DEFAULT_IMAGE"
                    class="max-h-full max-w-full object-contain transform transition-transform duration-700 -mt-10 group-hover:scale-110"
                    alt="" />
                  <div class="absolute bottom-0 left-0 right-0 h-40 bg-gradient-to-t from-black/30 to-transparent">
                  </div>
                </div>

                <div class="absolute bottom-4 left-6 flex space-x-3 ">
                  <div
                    class="bg-gradient-to-r from-neutral-600 to-neutral-800 backdrop-blur-md px-3 py-1.5 rounded-full text-xs font-medium">
                    <span class="itbms-ramGb flex">
                      {{ product.ramGb === null ? "-" : product.ramGb }}
                      <p class="itbms-storageGb-unit">
                        {{ product.ramGb === null ? "-" : "GB" }}
                      </p>
                      <p class="ml-1">RAM</p>
                    </span>
                  </div>
                  <div
                    class="bg-gradient-to-r from-neutral-600 to-neutral-900 backdrop-blur-md px-3 py-1.5 rounded-full text-xs font-medium">
                    <span class="itbms-storageGb flex">
                      {{ product.storageGb === null ? "-" : product.storageGb }}
                      <p class="itbms-storageGb-unit">
                        {{ product.storageGb === null ? "-" : "GB" }}
                      </p>
                    </span>
                  </div>
                </div>
              </div>

              <div class="space-y-2 px-2">
                <div class="flex justify-between items-start">
                  <h2 class="itbms-brand text-lg font-semibold">
                    {{ product.brandName }}
                  </h2>
                  <p class="itbms-price text-lg font-medium">
                    ฿{{ product.price.toLocaleString() }}
                  </p>
                </div>
                <p class="itbms-model text-base text-gray-400">
                  {{ product.model }}
                </p>

                <div class="pt-2 opacity-0 group-hover:opacity-100 transition-opacity duration-300">
                  <button
                    class="w-full bg-white text-black rounded-full py-1 font-medium hover:bg-gray-200 transition-colors">
                    Buy
                  </button>
                </div>
              </div>
            </div>

            <!-- List View -->
            <div v-else @click="detail(product.id)"
              class="border-b relative border-neutral-800 hover:bg-neutral-900 transition-colors">
              <div class="grid grid-cols-7 items-center gap-4 py-6 px-4">
                <div
                  class="bg-gradient-to-br from-neutral-800 to-neutral-900 w-24 h-24 rounded-xl flex items-center justify-center overflow-hidden perspective">
                  <div
                    class="transform-style-3d hover:rotate-y-10 transition-transform duration-500 w-full h-full flex items-center justify-center">
                    <img :src="DEFAULT_IMAGE"
                      class="itbms-image max-h-full max-w-full object-contain hover:scale-105 transition-transform duration-500"
                      alt="" />
                  </div>
                </div>
                <div class="itbms-brand font-medium">{{ product.brandName }}</div>
                <div class="itbms-model text-gray-400">{{ product.model }}</div>
                <div class="itbms-ramGb text-gray-500">{{ product.ramGb }}GB</div>
                <div class="itbms-storageGb text-gray-500">{{ product.storageGb }}</div>
                <div class="itbms-price font-medium">฿{{ product.price.toLocaleString() }}</div>
                <div class="flex space-x-4">
                  <button class="text-sm text-blue-500 hover:underline">View</button>
                  <button v-if="adminMode" class="text-sm text-blue-500 hover:underline">Edit</button>
                  <button v-if="adminMode" class="text-sm text-blue-500 hover:underline">Delete</button>
                </div>
              </div>
            </div>
          </template>
        </ListModel>
      </div>

      <!-- No Products Found from Filter -->
      <div v-if="!isLoading && noProductsFromFilter" class="flex-1">
        <div class="min-h-[500px] flex flex-col items-center justify-center text-center px-2 animate-fadeInUp">
          <div class="space-y-6 max-w-lg mx-auto mb-8">
            <p
              class="text-4xl md:text-5xl font-bold bg-gradient-to-r from-white via-gray-200 to-gray-400 bg-clip-text text-transparent leading-tight animate-gradient uppercase">
              No Sale Item
          </p>

          </div>
          <div class="relative">

            <!-- Main Icon with gradient background -->
            <div class="relative mb-8 mx-auto animate-float">

              <div
                class="w-32 h-32 bg-gradient-to-br from-gray-800 via-gray-700 to-gray-900 rounded-full flex items-center justify-center shadow-2xl border border-gray-600 relative overflow-hidden">
                <!-- Shimmer effect -->
                <div
                  class="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent animate-shimmer">
                </div>

                <!-- Search icon -->
                <svg class="w-16 h-16 text-gray-300 relative z-10" fill="none" stroke="currentColor"
                  viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5"
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"></path>
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M13 13l2 2" opacity="0.4">
                  </path>
                </svg>
              </div>
            </div>

          </div>
        </div>
      </div>
    </div>
    <Pagination v-if="!isLoading && !noProductsFromFilter && product.length > 0" :totalPages="productStore.allPages"
      @sendPages="updatePages" />
  </div>
</template>

<style>
.perspective {
  perspective: 800px;
}

.transform-style-3d {
  transform-style: preserve-3d;
}

@keyframes float {

  0%,
  100% {
    transform: translateY(0px);
  }

  50% {
    transform: translateY(-10px);
  }
}

@keyframes shimmer {
  0% {
    transform: translateX(-100%);
  }

  100% {
    transform: translateX(100%);
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* Apply animations */
.animate-float {
  animation: float 3s ease-in-out infinite;
}

.animate-shimmer {
  animation: shimmer 2s infinite;
}

.animate-fadeInUp {
  animation: fadeInUp 0.8s ease-out;
}

/* Gradient text animation */
@keyframes gradient-shift {

  0%,
  100% {
    background-position: 0% 50%;
  }

  50% {
    background-position: 100% 50%;
  }
}

.animate-gradient {
  background-size: 200% 200%;
  animation: gradient-shift 4s ease infinite;
}
</style>