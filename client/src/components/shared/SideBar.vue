<script setup>
import { useProductStore } from '@/stores/useProductStore';
import { computed, onMounted, ref } from 'vue';

const productStore = useProductStore()

onMounted(() => {
  productStore.loadProducts()
})

const products = computed(() => productStore.allProducts)

// Group products by brand
const groupedByBrand = computed(() => {
  const groups = {}
  for (const product of products.value) {
    const brand = product.brandName
    if (!groups[brand]) {
      groups[brand] = []
    }
    groups[brand].push(product)
  }
  return groups
})

// Toggle dropdown for each brand
const openBrands = ref({})

function toggleBrand(brand) {
  openBrands.value[brand] = !openBrands.value[brand]
}
</script>

<template>
  <div class="w-64 p-4 border-r border-gray-300 bg-gray-100 text-sm space-y-6">
    <!-- Check box -->
    <div class="flex items-center space-x-2">
      <input type="checkbox" id="availableOnly" class="w-4 h-4">
      <label for="availableOnly" class="text-black font-medium cursor-pointer hover:underline">
        Show available products
      </label>
    </div>

    <!-- Divider -->
    <hr class="border-gray-400" />

    <!-- Category header -->
    <div>
      <h2 class="text-black font-bold mb-2">Product Category</h2>

      <ul v-if="products.length">
        <li class="font-medium text-black mb-2">All Products</li>

        <!-- Loop by brand -->
        <li v-for="(models, brand) in groupedByBrand" :key="brand" class="mb-1">
          <div @click="toggleBrand(brand)" class="cursor-pointer font-semibold text-black hover:underline flex justify-between items-center">
            {{ brand }}
            <span>{{ openBrands[brand] ? '-' : '+' }}</span>
          </div>

          <!-- Dropdown list of models -->
          <ol v-show="openBrands[brand]" class="list-disc pl-6 mt-1">
            <li
              v-for="product in models"
              :key="product.id"
              class="text-gray-700"
            >
              {{ product.model }}
            </li>
          </ol>
        </li>
      </ul>
    </div>
  </div>
</template>
