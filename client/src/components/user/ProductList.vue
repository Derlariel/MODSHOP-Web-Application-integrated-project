<script setup>
import ListModel from "../shared/ListModel.vue";
import Navbar from "../shared/Navbar.vue";
import SideBar from "../shared/SideBar.vue";
import { computed, onMounted, ref } from "vue";
import { useProductStore } from "@/stores/useProductStore";
import ErrorModal from '../shared/modal/ErrorModal.vue';
import DEFAULT_IMAGE from "@/assets/default.jpg";
import { useRouter } from "vue-router";

const router = useRouter();
const isLoading = ref(true);
const adminMode = ref(false);
const isModalOpen = ref(sessionStorage.getItem('productStatus'))
console.log(isModalOpen);

const props = defineProps({
  viewType: {
    type: String,
    default: "gallery",
  },
});

function handleModalClose() {
  isModalOpen.value = false;
  sessionStorage.removeItem('productStatus')
  router.push({name: 'sale-items'});
}

const productStore = useProductStore();
const productImages = productStore.productImages;
const product = computed(() => productStore.allProducts);

onMounted(async () => {
  await productStore.loadProducts();

  if (product.value.length === 0) {
    router.push({ name: "error-page", query: { code: 200 } });
    return;
  }

  isLoading.value = false;
});

const detail = (productId) => {
  router.push({
    name: "product-detail",
    params: { productId },
  });
};
</script>

<template>

  <div class="flex h-screen">
    <SideBar />
    <ErrorModal  :visible="isModalOpen" message="The requested sale item does not exist." @close="handleModalClose"/>


    <!-- Content (ListModel) -->
    <div v-if="!isModalOpen" class="flex-1 overflow-y-auto p-4">
      <ListModel :saleItems="productStore.allProducts" :viewType="viewType" :adminMode="adminMode">

        <template #listHeader>
          <span>IMAGE</span>
          <span>BRAND</span>
          <span>MODEL</span>
          <span>RAM</span>
          <span>STORAGE</span>
          <span>PRICE</span>
          <span>ACTION</span>
        </template>

        <template #listItems="{ Item: product, viewType }">
          <!-- Gallery view -->
          <div v-if="viewType === 'gallery'" @click="detail(product.id)"
            class=" itbms-row bg-white rounded-md overflow-hidden hover:shadow-md transition-shadow cursor-pointer flex flex-col h-full">
            <div class="bg-gray-200 w-full h-72 flex items-center justify-center overflow-hidden">
              <img :src="productImages[Number(product.id)] || DEFAULT_IMAGE" class="max-h-full object-contain" />
            </div>
            <div class="p-4 flex flex-col justify-between flex-1">
              <div>
                <h2 class="itbms-brand text-base font-semibold text-gray-800">{{ product.brandName }}</h2>
                <p class="itbms-model text-sm text-gray-700 font-medium mb-1">Model: {{ product.model }}</p>
                <p class="itbms-ramGb text-sm text-gray-600 mb-2">
                  RAM: {{ product.ramGb === null ? '-' : product.ramGb }} GB
                </p>
                <div class="flex text-sm text-gray-600 gap-1">
                  <p class="itbms-storageGb">
                    Storage {{ product.storageGb === null ? '-' : product.storageGb }}
                  </p>
                  <p class="itbms-storageGb-unit">GB</p>
                </div>

              </div>
              <p class="itbms-price text-[#E46A6A] font-bold text-lg mt-auto">฿{{ product.price.toLocaleString() }}</p>
            </div>
          </div>

          <!-- List (table row) -->
          <template v-else>
            <div class="grid grid-cols-7 items-center gap-4 py-2 px-2 rounded hover:bg-gray-50 cursor-pointer">
              <div class="w-20 h-20 bg-gray-100 flex items-center justify-center rounded">
                <img :src="productImages[Number(product.id)] || DEFAULT_IMAGE"
                  class="max-h-full max-w-full object-contain" />
              </div>
              <div class="itbms-brand  text-gray-800 font-medium">
                {{ product.brandName }}
              </div>
              <div class="itbms-model text-gray-600">
                {{ product.model }}
              </div>
              <div class="itbms-ramGb text-gray-600">
                {{ product.ramGb }} GB 
              </div>
              <div class="itbms-storageGb text-gray-600">
                {{ product.storageGb }} GB
              </div>
              <div class="text-[#E46A6A] font-bold text-lg">
                ฿{{ product.price }}
              </div>
              <div class="text-white grid space-y-2 w-32">
                <button @click.stop="detail(product.id)" class="bg-blue-500 px-2 rounded-lg">
                  View Detail
                </button>
                <button @click.stop="detail(product.id)" class="bg-yellow-500 px-2 rounded-lg">
                  Edit
                </button>
                <button @click.stop="detail(product.id)" class="bg-red-500 px-2 rounded-lg">
                  Delete
                </button>
              </div>
            </div>
          </template>
        </template>
      </ListModel>
    </div>
  </div>
</template>
