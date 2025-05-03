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
const product = computed(() => productStore.getProductById(productId));

onMounted(async () => {
  await productStore.loadProducts();

  if (productStore.products.length === 0 || !product.value) {
    router.push({ name: "error-page", query: { code: 200 } });
    return;
  }

  isLoading.value = false;
});
</script>
<template>
  <div v-if="!isLoading && product" class="max-w-6xl mx-auto px-6 py-10">
    <div class="grid grid-cols-1 md:grid-cols-2 gap-12">
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
          <h1 class="text-2xl font-semibold text-gray-900 tracking-tight leading-tight">
            {{ product.brandName }} {{ product.model }} {{ product.storageGb }}GB {{ product.color }}
          </h1>
          <p class="text-sm text-gray-500 mt-1">Brand: {{ product.brandName }}</p>
        </div>

        <div class="text-3xl font-bold text-red-400">฿{{ product.price }}</div>

        <div>
          <p class="text-gray-700 leading-relaxed">{{ product.description }}</p>
          <ul class="mt-4 text-sm text-gray-600 space-y-1">
            <li>RAM: {{ product.ramGb }} GB</li>
            <li>Screen: {{ product.screenSizeInch }} นิ้ว</li>
            <li>Storage: {{ product.storageGb }} GB</li>
            <li>Color: {{ product.color }}</li>
          </ul>
        </div>

        <button class="mt-6 bg-black text-white text-sm font-medium px-6 py-3 rounded-md hover:bg-gray-800 transition">
          Add to Cart
        </button>
      </div>
    </div>
  </div>
</template>
