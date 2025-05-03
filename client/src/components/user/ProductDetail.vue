<script setup>
import { useRoute, useRouter } from 'vue-router';
import { useProductStore } from "@/stores/useProductStore";
import { computed, onMounted, ref } from 'vue';
import DEFAULT_IMAGE from "@/assets/default.jpg"
const router = useRouter();
const route = useRoute();
const productId = Number(route.params.productId);

const productStore = useProductStore();

const isLoading = ref(true);
const product = computed(() => productStore.getProductById(productId));

onMounted(async () => {
  await productStore.loadProducts();

  if (productStore.products.length === 0) {
    router.push({ name: "error-page", query: { code: 200 } });
    return;
  }

  if (!product.value) {
    router.push({ name: "error-page", query: { code: 200 } });
    return;
  }

  isLoading.value = false;
});
</script>

<template>
  <div v-if="!isLoading && product">
    <div class="grid grid-cols-2">
      <div class="w-full">
        <div class="flex justify-center items-center">
          <img
            class="h-100 object-contain"
            :src="productStore.productImages[product.id] || DEFAULT_IMAGE"
            alt="product image"
          />
        </div>

        <div class="flex justify-center gap-4 mt-4">
          <div v-for="i in 5" :key="i">
            <img
              class="h-20 w-20 object-contain"
              :src="productStore.productImages[product.id] || DEFAULT_IMAGE"
              alt="product thumbnail"
            />
          </div>
        </div>
      </div>

      <!-- ข้อมูลสินค้า -->
      <div class="mt-10 space-y-4">
        <div class="text-2xl font-semibold">
          <span>{{ product.brandName }}</span>
          <span> {{ product.model }}</span>
          <span> {{ product.storageGb }}GB</span>
          <span> {{ product.color }}</span>
        </div>

        <p class="text-gray-600">Brand: {{ product.brandName }}</p>

        <h2 class="text-[#E46A6A] font-bold text-3xl">฿{{ product.price }}</h2>

        <div class="text-gray-700 space-y-2">
          <p>{{ product.description }}</p>
          <ul class="list-disc list-inside text-sm text-gray-600">
            <li>RAM: {{ product.ramGb }} GB</li>
            <li>หน้าจอ: {{ product.screenSizeInch }} นิ้ว</li>
            <li>Storage: {{ product.storageGb }} GB</li>
            <li>สี: {{ product.color }}</li>
          </ul>
        </div>
      </div>
    </div>
  </div>
</template>
