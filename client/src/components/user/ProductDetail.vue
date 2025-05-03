<script setup>
import { useRoute, useRouter } from 'vue-router';
import { useProductStore } from "@/stores/useProductStore";
import { computed, onMounted, ref } from 'vue';
import DEFAULT_IMAGE from "@/assets/default.jpg";

const router = useRouter();
const route = useRoute();
const productId = Number(route.params.productId);
const productStore = useProductStore();

const isLoading = ref(true);

const product = ref(null);
const isData = ref(true)

onMounted(async () => {
  await productStore.loadProducts();

  const result = await productStore.fetchProductDetail(productId);
  product.value = result;

if (productStore.products === null || !product.value) {
  sessionStorage.setItem('error-message', 'The requested sale item does not exist.');
  router.push('/sale-items');
  return;
}

  isLoading.value = false;

});
</script>
<template>
  <div v-if="!isLoading && product && isData" class="max-w-6xl mx-auto px-6 py-10">
    <div class="itbms-row grid grid-cols-1 md:grid-cols-2 gap-12">
      <!-- Product Image -->
      <div class="flex flex-col gap-4">
        <div
          class="bg-gray-50 rounded-lg overflow-hidden aspect-square flex items-center justify-center border border-gray-200 shadow-xl">
          <img class="object-contain max-h-[400px] transition duration-300"
            :src="productStore.productImages[product.id] || DEFAULT_IMAGE" alt="product image" />
        </div>

        <!-- Thumbnails -->
        <div class="flex gap-3 justify-center">
          <img v-for="i in 4" :key="i" :src="productStore.productImages[product.id] || DEFAULT_IMAGE"
            class="h-16 w-16 object-contain border border-gray-200 shadow-md rounded-md hover:opacity-80 cursor-pointer transition"
            alt="thumbnail" />
        </div>
      </div>

      <!-- Product Info -->
      <div class="flex flex-col gap-6">
        <div>
          <h1 class="itbms-brand text-2xl font-semibold text-gray-900 tracking-tight leading-tight">
            {{ product.brandName }} 
          </h1>
          <h1 class="itbms-model text-2xl font-semibold text-gray-900 tracking-tight leading-tight">
            {{ product.model }}
          </h1>
          <h1 class="itbms-storageGb text-2xl font-semibold text-gray-900 tracking-tight leading-tight">
            {{ product.storageGb === null ? '-' : product.storageGb }}GB 
          </h1>
          <h1 class="itbms-storageGb-unit text-2xl font-semibold text-gray-900 tracking-tight leading-tight">
            GB 
          </h1>
          <h1 class="itbms-color text-2xl font-semibold text-gray-900 tracking-tight leading-tight">
            {{ product.color === null ? '-' : product.color }}
          </h1>
         
          <p class="text-sm text-gray-500 mt-1">Brand: {{ product.brandName }}</p>
        </div>

        <div class="itbms-price text-3xl font-bold text-red-400">฿{{ product.price.toLocaleString() }}</div>

        <div>
          <p class="itbms-description text-gray-700 leading-relaxed">{{ product.description }}</p>
          <ul class="mt-4 text-sm text-gray-600 space-y-1">
            <li class="itbms-ramGb">RAM: {{ product.ramGb === null ? '-' : product.ramGb }} <span class="itbms-ramGb-unit">GB</span></li>
            <li class="itbms-screenSizeInch">Screen: {{ product.screenSizeInch === null ? '-' : product.screenSizeInch }}  <span class="itbms-screenSizeInch-unit">Inches</span></li>
            <li>Storage: {{ product.storageGb }} GB</li>
            <li>Color: {{ product.color === null ? '-' : product.color }}</li>
            <li class="itbms-quantity">Quantity: {{ product.quantity }}</li>
          </ul>
        </div>

        <button class="mt-6 bg-black text-white text-sm font-medium px-6 py-3 rounded-md hover:bg-gray-800 transition">
          Add to Cart
        </button>
      </div>
    </div>
  </div>
</template>
