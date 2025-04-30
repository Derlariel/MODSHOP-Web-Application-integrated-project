<script setup>
import ListI16 from '@/assets/apple/list-iphone16.png'
</script>

<template>
  <div class="relative bg-[#000101] w-full min-h-screen py-24 text-center space-y-10 px-4">
    <!-- Title -->
    <h2 class="text-4xl md:text-5xl font-extrabold text-white">phonezy</h2>

    <!-- Subtitle -->
    <h3 class="text-lg md:text-xl font-semibold text-gray-300">
      From ฿5,000 to ฿90,000 — Every Model Ever Built
    </h3>

    <!-- Action buttons -->
    <div class="flex justify-center items-center space-x-6">
        <router-link to="/v1/sale-items">
            <button
        class="text-lg font-bold text-white bg-blue-600 px-10 py-1 rounded-xl transition-transform duration-300 hover:scale-105 hover:bg-blue-700"
      >
        Buy
      </button>
        </router-link>
     
      <button class="text-xl font-bold text-blue-500 hover:underline">
        Learn More
      </button>
    </div>

    <!-- Image -->
    <div class="flex justify-center">
      <img :src="ListI16" alt="Phone list" class="absolute bottom-0 left-1/2 -translate-x-1/2 w-full max-w-5xl object-contain" />
    </div>
  </div>
</template>

<style scoped></style>
