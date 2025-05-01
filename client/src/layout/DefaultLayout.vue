<script setup>
import Navbar from "@/components/shared/Navbar.vue";
</script>

<template>
  <div>
    <div
      class="bg-black w-screen h-12 text-white flex justify-center items-center space-x-5"
    >
      <p>Summer Sale For All Phone and Free Express Delivery - OFF 50%!</p>
      <p class="font-bold underline">Shop Now!</p>
    </div>
    <Navbar />
    <router-view />
  </div>
</template>

<style scoped></style>
