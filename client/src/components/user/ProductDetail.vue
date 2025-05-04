<script setup>
import { useRoute, useRouter } from 'vue-router';
import { useProductStore } from "@/stores/useProductStore";
import { computed, onMounted, ref } from 'vue';
import DEFAULT_IMAGE from "@/assets/default.jpg";
import ErrorModal from '../shared/modal/ErrorModal.vue';
const router = useRouter();
const route = useRoute();
const productId = Number(route.params.productId);
const productStore = useProductStore();

const isLoading = ref(true);
const product = ref(null);
const isData = ref(true)
const isModalOpen = ref(false)
onMounted(async () => {
  await productStore.loadProducts();
  const result = await productStore.fetchProductDetail(productId);
  product.value = result;

  if (product.value === null || !product.value) {
    isModalOpen.value = true;
    router.go(-1)
    return;
  }

  isLoading.value = false;
});

function handleModalClose() {
  isModalOpen.value = false;
  router.push('/sale-items');
}

</script>

<template>
  <div v-if="!isLoading && product && isData" class="max-w-6xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
    <div class="itbms-row grid grid-cols-1 md:grid-cols-2 gap-8 md:gap-12">
      <!-- Product Image -->
      <div class="flex flex-col gap-4">
        <div
          class="bg-gray-50 rounded-lg overflow-hidden aspect-square flex items-center justify-center border border-gray-200 shadow-xl">
          <img class="object-contain max-h-[300px] sm:max-h-[350px] md:max-h-[400px] transition duration-300"
            :src="productStore.productImages[product.id] || DEFAULT_IMAGE" alt="product image" />
        </div>

        <!-- Thumbnails -->
        <div class="flex flex-wrap gap-2 justify-center sm:gap-3">
          <img v-for="i in 4" :key="i" :src="productStore.productImages[product.id] || DEFAULT_IMAGE"
            class="h-14 w-14 sm:h-16 sm:w-16 object-contain border border-gray-200 shadow-md rounded-md hover:opacity-80 cursor-pointer transition"
            alt="thumbnail" />
        </div>
      </div>

      <!-- Product Info -->
      <div class="flex flex-col gap-4 sm:gap-6">
        <div class="itbms-row flex flex-wrap gap-x-2 gap-y-1">
          <h1 class="itbms-brand text-xl sm:text-2xl font-semibold text-gray-900 tracking-tight leading-tight">
            {{ product.brandName }} 
          </h1>
          <h1 class="itbms-model text-xl sm:text-2xl font-semibold text-gray-900 tracking-tight leading-tight">
            {{ product.model }}
          </h1>
          <h1 class="itbms-storageGb text-xl sm:text-2xl font-semibold text-gray-900 tracking-tight leading-tight">
            {{ product.storageGb === null ? '-' : product.storageGb }}
          </h1>
          <h1 class="itbms-storageGb-unit text-xl sm:text-2xl font-semibold text-gray-900 tracking-tight leading-tight">
            GB 
          </h1>
          <h1 class="itbms-color text-xl sm:text-2xl font-semibold text-gray-900 tracking-tight leading-tight">
            {{ product.color === null ? '-' : product.color }}
          </h1>
        </div>

        <div class="itbms-price text-2xl sm:text-3xl font-bold text-red-400">
          ฿{{ product.price.toLocaleString() }}
        </div>

        <div>
          <p class="itbms-description text-gray-700 leading-relaxed text-sm sm:text-base">{{ product.description }}</p>
          <ul class="mt-4 text-sm sm:text-base text-gray-600 space-y-1">
            <li class="itbms-ramGb">RAM: {{ product.ramGb === null ? '-' : product.ramGb }} <span class="itbms-ramGb-unit">GB</span></li>
            <li class="itbms-screenSizeInch">Screen: {{ product.screenSizeInch === null ? '-' : product.screenSizeInch }} <span class="itbms-screenSizeInch-unit">Inches</span></li>
            <li>Storage: {{ product.storageGb }} GB</li>
            <li>Color: {{ product.color === null ? '-' : product.color }}</li>
            <li class="itbms-quantity">Quantity: {{ product.quantity }}</li>
          </ul>
        </div>

        <button class="mt-6 bg-black text-white text-sm font-medium px-6 py-3 rounded-md hover:bg-gray-800 transition w-full sm:w-auto">
          Add to Cart
        </button>
      </div>
    </div>
  </div>
  <ErrorModal  :visible="isModalOpen" message="The requested sale item does not exist." @close="handleModalClose"/>
</template>
