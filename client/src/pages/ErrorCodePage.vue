<script setup>
import { useRoute, useRouter } from "vue-router";
import { onMounted } from "vue";
import { useProductStore } from "@/stores/useProductStore";

const route = useRoute();
const router = useRouter();
const productStore = useProductStore();
const code = (route.query.code || "500").toUpperCase(); 

const errorMessages = {
  NODATA: "No products found", 
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

const images = import.meta.glob("@/assets/images/error/*.svg", {
  eager: true,
  import: "default",
});

const imageFileName = errorImages[code] || errorImages.default;
const imagePath = images[`/src/assets/images/error/${imageFileName}`];
const message = errorMessages[code] || errorMessages.default;

// Clear filters and reset page when NODATA error occurs
const clearFiltersAndGoBack = () => {
  // Clear all sessionStorage filters
  sessionStorage.removeItem("filterAndSort");
  sessionStorage.removeItem("activePage");
  
  // Reset product store page
  productStore.setActivePage(1);
  
  // Set a flag to indicate filters were cleared from error page
  sessionStorage.setItem("filters-cleared-from-error", "true");
  
  // Navigate back to product gallery
  router.push({ name: "product-gallery" });
};

// Auto-clear filters when NODATA error is displayed
onMounted(() => {
  if (code === 'NODATA') {
    // Clear filters immediately when NODATA error page loads
    sessionStorage.removeItem("filterAndSort");
    sessionStorage.removeItem("activePage");
    productStore.setActivePage(1);
    
    // Set a flag to indicate filters were cleared automatically
    sessionStorage.setItem("filters-cleared-from-error", "true");
  }
});

</script>

<template>
  <div class="flex flex-col items-center justify-center min-h-screen bg-gray-50">
    <img  :src="imagePath" alt="error image" class="w-96 mb-6" />
    <h1 v-if="code !== 'NODATA'" class="text-4xl font-bold text-red-600 mb-2">Error {{ code }}</h1>
    <h1 v-else class="text-4xl font-bold text-red-600 mb-2">{{ message }}</h1>
    <p v-if="code !== 'NODATA'" class="text-lg text-gray-700 text-center mb-6">{{ message }}</p>
    
    <!-- Show go back button for NODATA error -->
    <div v-if="code === 'NODATA'" class="text-center">
      <button
        @click="clearFiltersAndGoBack"
        class="px-6 py-3 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors duration-200 font-medium shadow-lg my-8"
      >
        Back
      </button>
    </div>
  </div>
</template>
