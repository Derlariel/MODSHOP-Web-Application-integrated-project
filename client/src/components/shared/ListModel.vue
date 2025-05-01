<script setup>
import { List, GalleryThumbnails } from "lucide-vue-next";
import { ref } from "vue";
import SideBar from "../shared/SideBar.vue";

const isListView = ref(false);

const props = defineProps({
  products: Object,
  productImages: Object,
});
</script>

<template>
  <div class="flex">
    
    <!-- Sidebar -->
    <SideBar />

    <!-- Main Content -->
    <div class="flex-1 p-6">
  <p class="text-center font-extrabold text-2xl">Product List</p>

      <!-- View Toggle -->
      

      <!-- Product Gallery View -->
      <ul
        v-if="products.length && !isListView"
        class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6"
      >
        <li
          v-for="product in products"
          :key="product.id"
          class="bg-white shadow-lg rounded-2xl overflow-hidden hover:shadow-xl transition-shadow"
        >
          <!-- Image -->
          <img
            :src="productImages[product.id]"
            class="w-full h-60 object-contain bg-gray-100"
          />
          <!-- Details -->
          <div class="p-4">
            <h2 class="itmbs-brand text-lg font-semibold text-gray-800">
              {{ product.brandName }}
            </h2>
            <p class="itmbs-model text-gray-700 font-medium mb-1">
              Model: {{ product.model }}
            </p>
            <p class="itmbs-ram/itmbs-storage text-sm text-gray-600 mb-2">
              RAM: {{ product.ramGb }} GB / {{ product.storageGb }} GB
            </p>
            <p class="itmbs-price text-green-600 font-bold text-xl">
              ฿{{ product.price }}
            </p>
            <div class="flex justify-end pt-2">
              <button class="bg-blue-500 px-3 py-1 rounded-lg text-white hover:bg-blue-700">
                View
              </button>
            </div>
          </div>
        </li>
      </ul>      
      
      <!-- Product List View -->
      
      <!-- No Products -->
      <p v-else class="itmbs-message text-center  text-gray-400 py-10">No items found</p>
    </div>
  </div>
</template>

<style scoped></style>
