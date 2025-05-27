<script setup>
import { useRoute } from "vue-router";

const route = useRoute();
const code = (route.query.code || "500").toUpperCase(); 
const BASE_URL = import.meta.env.BASE_URL;

const errorMessages = {
  NODATA: "No sale items", 
  404: "The requested sale item does not exist.",
  500: "Internal server error occurred",
  503: "The service is temporarily unavailable",
  default: "An unknown error has occurred",
};

const errorImages = {
  404: "404.svg",
  500: "500.svg",
  503: "503.svg",
  NODATA: "nodata.svg", 
  default: "default.svg",
};

const message = errorMessages[code] || errorMessages.default;
const imageFile = errorImages[code] || errorImages.default;
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen bg-gray-50">
    <img :src="`/kk1/images/error/${imageFile}`" alt="error image" class="w-96 mb-6" />
    <!-- ใช้ v-if ตรวจสอบค่า code แทนที่ Nodata -->
    <h1 v-if="code !== 'NODATA'" class="text-4xl font-bold text-red-600 mb-2">Error {{ code }}</h1>
    <h1 v-else class="text-4xl font-bold text-red-600 mb-2">{{ message }}</h1>
    <p v-if="code !== 'NODATA'" class="text-lg text-gray-700 text-center">{{ message }}</p>
  </div>
</template>
