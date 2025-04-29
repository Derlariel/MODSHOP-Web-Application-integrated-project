<script setup>
import { useProductStore } from '@/stores/useProductStore.js'
import Navbar from '../shared/Navbar.vue';
import { ref, onMounted } from 'vue'
import I14PROMAX from '@/assets/apple/iPhone-14-Pro-Max-Space-Black.webp'
import I14 from '@/assets/apple/iPhone_14_Midnight.png'
import I13PRO from '@/assets/apple/iphone-13-pro-blue-select.png'
import SE2020 from '@/assets/apple/iPhone_SE3_Starlight.webp'
import I14PLUS from '@/assets/apple/iPhone_14_Plus_Blue-square_medium.webp'
import S23ULTRA from '@/assets/samsung/Samsung-Galaxy-S23-Ultra.webp'
const productStore = useProductStore()

onMounted(() => {
  productStore.loadProducts()
})

const productImages = {
  1: I14PROMAX,
  2: I14,
  3: I13PRO,
  7: SE2020,
  8: I14PLUS,
  16: S23ULTRA,
}

</script>

<template>
  <div>
    <Navbar />
    <h1 class="text-2xl font-bold mb-6 py-2 text-center">Product List</h1>
    <ul v-if="productStore.allProducts.length"
      class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6 px-6">
      <li v-for="product in productStore.allProducts" :key="product.id"
        class="bg-white shadow-lg rounded-2xl overflow-hidden hover:shadow-xl transition-shadow ">
        <img :src="productImages[product.id] || '/images/default.jpg'" class="w-full h-60 object-contain bg-gray-100" />

        <div class="p-4">
          <h2 class="text-lg font-semibold text-gray-800">{{ product.brandName }}</h2>
          <p class="text-gray-700 font-medium mb-1">Model: {{ product.model }}</p>
          <p class="text-sm text-gray-600 mb-2">RAM: {{ product.ramGb }} GB / {{ product.storageGb }} GB</p>
          <p class="text-green-600 font-bold text-xl">฿{{ product.price }}</p>
          <div class=" flex justify-end px-4 py-2">
            <button class="itmbs-btn-view bg-blue-500 px-2 py-1 rounded-lg text-white hover:bg-blue-700">View</button>
          </div>
        </div>

      </li>
    </ul>
    <p v-else class="text-center text-gray-500 py-10">No item sale</p>
  </div>
</template>

<style scoped></style>
