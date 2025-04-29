<script setup>
import { useProductStore } from '@/stores/useProductStore';
import { onMounted } from 'vue';

const productStore = useProductStore()

onMounted(() => {
  productStore.loadProducts()
})
</script>

<template>
  <div
    class="relative min-h-screen bg-[linear-gradient(135deg,_#383838_3%,_#080808_48%)] text-white overflow-hidden"
  >
    <!-- Hero Section -->
    <div
      class="grid grid-cols-1 md:grid-cols-2 place-items-center h-screen w-full px-6 md:px-12 z-10 relative"
    >
      <!-- Left Text -->
      <div class="text-left space-y-6 z-10 pl-14 md:pl-26 ">
        <h1 class="text-4xl md:text-7xl font-extrabold tracking-wide leading-normal">
          Discover the Most<br />
          Affordable Phones<br />
        </h1>
        <p class="text-[#8B8E99] max-w-xl">
          Find the best, most reliable, and affordable smartphones right here. We don’t just focus on one brand — we’ve got every damn phone you can think of. From the latest flagships to rare classics you can’t even find in stores anymore. We care about quality, not just flashy names. So what the hell are you waiting for? Order now and thank us later!
        </p>
        <div class="flex items-center bg-white rounded-full px-4 py-2 w-full max-w-md">
          <input
            type="text"
            placeholder="Find the best product"
            class="flex-grow text-black focus:outline-none"
          />
          <button
            class="bg-[#3D5AFE] text-white px-4 py-2 rounded-full font-semibold"
          >
            Search
          </button>
        </div>
      </div>

      <!-- Right Image -->
      <div class="w-full h-full flex justify-center items-center">
        <img
          src="@/assets/apple/iphone16-for-homepage.webp"
          alt="iPhone"
          class="w-full h-full object-contain max-w-[1000px]"
        />
      </div>
    </div>

    <!-- Product List Section -->
    <section class="bg-white text-black py-12 px-6 md:px-12">
      <h2 class="text-3xl font-bold mb-8 text-center text-gray-900">📱 รายการสินค้าแนะนำ</h2>
      <div class="grid gap-8 grid-cols-1 md:grid-cols-2 lg:grid-cols-3">
        <div
          v-for="product in productStore.allProducts"
          :key="product.id"
          class="bg-gray-100 rounded-xl p-6 shadow-md"
        >
          <h3 class="text-xl font-semibold mb-2">{{ product.name }}</h3>
          <p class="text-gray-700 mb-1">{{ product.description }}</p>
          <p class="text-indigo-600 font-bold text-lg">{{ product.price.toLocaleString() }} บาท</p>
          <button
            class="mt-4 px-4 py-2 bg-blue-600 text-white rounded hover:bg-blue-700"
          >
            ดูรายละเอียด
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
</style>
