<script setup>
import { computed, onMounted, ref, watch } from "vue";
import { useRouter } from "vue-router";
import { useProductStore } from "@/stores/useProductStore";

import ListModel from "@/components/shared/ListModel.vue";
import FilterSort from "@/components/shared/FilterSort.vue";
import SuccessModal from "@/components/shared/modal/SuccessModal.vue";
import Pagination from "@/components/shared/Pagination.vue";
import ErrorModal from "@/components/shared/modal/ErrorModal.vue";
import DEFAULT_IMAGE from "@/assets/default.jpg";
import SkeletonLoader from "@/components/shared/SkeletonLoader.vue";

const router = useRouter();

const productStore = useProductStore();
const productImages = productStore.productImages;
const product = computed(() => productStore.allProducts);
const totalPages = ref(0)


const isLoading = ref(true);
const isModalOpen = ref(false);
const showSuccess = ref(false);
const alertMessage = ref("");
const adminMode = ref(false);

const filters = ref({
  page: 0,
  filterBrands: [],
  size: 10,
  sortField: "createdOn",
  sortDirection: "asc",
});


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
    await productStore.loadAllPages(filters.value);
  try {
    await productStore.loadProductsPage(filters.value);
    totalPages.value = productStore.allPages
    console.log(totalPages.value);
    if (product.value.length === 0) {
      router.push({ name: "error-page", query: { code: "NODATA" } });
    }
  } catch (err) {
    console.error("Load products failed:", err);
  } finally {
    isLoading.value = false;
  }
}

const updatePages = (pages) => {
  filters.value.page = pages  
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

const filteredProducts = computed(() => {
  if (!filters.value.filterBrands || filters.value.filterBrands.length === 0) {
    return productStore.allProducts
  }

  const selectedBrandNames = filters.value.filterBrands.map(b => b.name)

  return productStore.allProducts.filter(p =>
    selectedBrandNames.includes(p.brandName)
  )
})

onMounted(async () => {
  await initProducts();

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
    sessionStorage.setItem("activePage", 1)
    setTimeout(() => {
      showSuccess.value = false;
    }, 2000);
  }
});

watch(filters, async () => {
  initProducts();
}, { deep: true, immediate: true })
</script>

<template>
  <div class="min-h-screen bg-black text-white">
    <div class="pt-24 pb-8 px-6 bg-gradient-to-b from-neutral-900 to-black">
      <div class="max-w-[1200px] mx-auto text-center">
        <h1 class="text-5xl font-semibold tracking-tight mb-2">
          Store The best way to buy the products you love.
        </h1>
        <p class="text-xl text-gray-400 mt-4 max-w-2xl mx-auto">
          Explore premium devices with cutting-edge technology and elegant design.
        </p>

        <SuccessModal :message="alertMessage" :visible="showSuccess" />

        <div class="flex flex-wrap items-center justify-center gap-4 mt-4">
          <div class="flex-1 min-w-[250px]">
            <FilterSort @update:filters="updateFilters" />
          </div>
        </div>
          

      </div>
    </div>

    <div class="max-w-[1200px] mx-auto px-6 pb-20">
      <ErrorModal
        :visible="isModalOpen"
        message="The requested sale item does not exist."
        @close="handleModalClose"
      />

      <!-- Loading Skeletons -->
      <div v-if="isLoading" class="flex items-center justify-center ">
        <SkeletonLoader />
      </div>

      <div v-if="!isLoading && !isModalOpen" class="flex-1 mb-12">
        <ListModel :saleItems="productStore.allProducts" :viewType="viewType" :adminMode="adminMode">
          <!-- Header -->
          <template #listHeader>
            <div
              v-if="viewType === 'list'"
              class="grid grid-cols-7 gap-4 py-3 px-4 border-b border-neutral-800 text-gray-400 text-sm font-medium"
            >
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
            <div
              v-if="viewType === 'gallery'"
              @click="detail(product.id)"
              class="itbms-row group cursor-pointer transform transition-all duration-500 hover:scale-[1.02] gap-8"
            >
              <div class="relative h-[300px] rounded-2xl overflow-hidden bg-gradient-to-br from-white to-neutral-100 mb-4 perspective group-hover:shadow-2xl group-hover:shadow-white/30 transition-shadow duration-700">
                <div class="absolute inset-0 flex items-center justify-center transition-transform duration-700">
                  <img
                    :src="productImages[Number(product.id)] || DEFAULT_IMAGE"
                    class="max-h-full max-w-full object-contain transform transition-transform duration-700 -mt-10 group-hover:scale-110"
                    alt=""
                  />
                  <div class="absolute bottom-0 left-0 right-0 h-40 bg-gradient-to-t from-black/30 to-transparent"></div>
                </div>

                <div class="absolute bottom-6 left-6 flex space-x-3">
                  <div class="bg-gradient-to-r from-neutral-600 to-neutral-800 backdrop-blur-md px-3 py-1.5 rounded-full text-xs font-medium">
                    <span class="itbms-ramGb flex">
                      {{ product.ramGb === null ? "-" : product.ramGb }}
                      <p class="itbms-storageGb-unit">
                        {{ product.ramGb === null ? "-" : "GB" }}
                      </p>
                      <p class="ml-1">RAM</p>
                    </span>
                  </div>
                  <div class="bg-gradient-to-r from-neutral-600 to-neutral-900 backdrop-blur-md px-3 py-1.5 rounded-full text-xs font-medium">
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
                  <button class="w-full bg-white text-black rounded-full py-1 font-medium hover:bg-gray-200 transition-colors">
                    Buy
                  </button>
                </div>
              </div>
            </div>

            <!-- List View -->
            <div
              v-else
              @click="detail(product.id)"
              class="border-b relative border-neutral-800 hover:bg-neutral-900 transition-colors"
            >
              <div class="grid grid-cols-7 items-center gap-4 py-6 px-4">
                <div class="bg-gradient-to-br from-neutral-800 to-neutral-900 w-24 h-24 rounded-xl flex items-center justify-center overflow-hidden perspective">
                  <div class="transform-style-3d hover:rotate-y-10 transition-transform duration-500 w-full h-full flex items-center justify-center">
                    <img
                      :src="productImages[Number(product.id)] || DEFAULT_IMAGE"
                      class="itbms-image max-h-full max-w-full object-contain hover:scale-105 transition-transform duration-500"
                      alt=""
                    />
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
    </div>
    <Pagination v-if="!isLoading && product.length > 0" :totalPages="productStore.allPages" @sendPages="updatePages" />
  </div>

  <div v-if="!isLoading && product.length === 0" class="min-h-screen flex items-center justify-center text-white text-xl">
    No sale items found
  </div>
</template>

<style>
.perspective {
  perspective: 800px;
}

.transform-style-3d {
  transform-style: preserve-3d;
}
</style>