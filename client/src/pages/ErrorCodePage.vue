<script setup>
import { useRoute } from "vue-router";

const BASE_URL = import.meta.env.BASE_URL
const route = useRoute();
const code = route.query.code || "500";

const errorMessages = {
  200: "No sale item",
  404: "The page you are looking for was not found",
  500: "Internal server error occurred",
  503: "The service is temporarily unavailable",
  default: "An unknown error has occurred",
};

const errorImages = {
  200: "default.svg",
  404: "404.svg",
  500: "500.svg",
  503: "503.svg",
  default: "default.svg",
};

const message = errorMessages[code] || errorMessages.default;
const imageFile = errorImages[code] || errorImages.default;
</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen bg-gray-50">
    <img :src="`${BASE_URL}/images/error/${imageFile}`" alt="error image" class="w-96 mb-6" />
    <h1 class="text-4xl font-bold text-red-600 mb-2">Error {{ code }}</h1>
    <p class="text-lg text-gray-700 text-center">{{ message }}</p>
  </div>
</template>
