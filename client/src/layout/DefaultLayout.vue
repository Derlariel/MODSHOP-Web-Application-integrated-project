<script setup>
import { ref } from 'vue'
import Navbar from "@/components/shared/Navbar.vue"

</script>

<template>
  <div class="w-screen min-h-screen">
    <!-- Promotion Bar -->
    <div
      class="bg-black text-white text-center px-4 py-2 flex flex-col sm:flex-row sm:justify-center sm:items-center space-y-1 sm:space-y-0 sm:space-x-4">
      <p class="font-semibold text-xs sm:text-sm md:text-base lg:text-lg">
        Summer Sale For All Phones & Free Express Delivery - OFF 50%!
      </p>
      <p
        class="font-bold text-sm sm:text-base md:text-lg underline cursor-pointer hover:text-yellow-300 transition-colors">
        Shop Now!
      </p>
    </div>

    <!-- Navbar -->
    <Navbar />

    <!-- Page Content -->
    <router-view />
  </div>
</template>
