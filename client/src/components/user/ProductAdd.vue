<script setup>
import { useProductStore } from "@/stores/useProductStore";
import { useBrandStore } from "@/stores/useBrandStore";
import { computed, onMounted, ref } from "vue";
import ProductPicture from "./ProductPicture.vue";

const productStore = useProductStore();
const brandStore = useBrandStore();
const brands = computed(() => brandStore.getBrands());
const selectedImage = ref(1);

onMounted(() => {
  brandStore.loadBrands();
  console.log(brands.value);
});

const setSelectedImage = (index) => {
  selectedImage.value = index;
};
</script>

<template>
  <div class="min-h-screen bg-black text-white">

    <!-- Main content -->
    <div class="pt-24 pb-20">
      <div class="max-w-[1200px] mx-auto px-6">
        <h1 class="text-3xl md:text-4xl font-semibold tracking-tight mb-8">Create New Product</h1>
        
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16">
          <ProductPicture />
          
          
          <!-- Form Section -->
          <div>
            <form class="space-y-8">
              <!-- Brand Selection -->
              <div class="space-y-3">
                <label for="brand" class="block text-sm font-medium text-gray-300">Brand</label>
                <div class="relative">
                  <select 
                    id="brand"
                    class="w-full px-4 py-3.5 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white appearance-none"
                  >
                    <option value="" disabled selected>Select a brand</option>
                    <option v-for="(brand, index) in brands" :key="index" :value="brand.id">
                      {{ brand.name }}
                    </option>
                  </select>
                  <div class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none">
                    <svg class="h-5 w-5 text-gray-400" xmlns="http://www.w3.org/2000/svg" viewBox="0 0 20 20" fill="currentColor">
                      <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
                    </svg>
                  </div>
                </div>
              </div>
              
              <!-- Model Input -->
              <div class="space-y-3">
                <label for="model" class="block text-sm font-medium text-gray-300">Model</label>
                <input 
                  type="text" 
                  id="model"
                  class="w-full px-4 py-3.5 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white"
                  placeholder="Enter model name"
                >
              </div>
              
              <!-- Price Input -->
              <div class="space-y-3">
                <label for="price" class="block text-sm font-medium text-gray-300">Price (Baht)</label>
                <div class="relative">
                  <input 
                    type="text" 
                    id="price"
                    class="w-full pl-12 pr-4 py-3.5 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white"
                    placeholder="0.00"
                  >
                  <div class="absolute inset-y-0 left-0 flex items-center pl-4 pointer-events-none">
                    <span class="text-gray-400 font-medium">฿</span>
                  </div>
                </div>
              </div>
              
              <!-- Description Textarea -->
              <div class="space-y-3">
                <label for="description" class="block text-sm font-medium text-gray-300">Description</label>
                <textarea 
                  id="description"
                  rows="4"
                  class="w-full px-4 py-3.5 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white"
                  placeholder="Enter product description"
                ></textarea>
              </div>
              
              <!-- Specifications Section -->
              <div class="pt-2">
                <h3 class="text-lg font-medium text-white mb-4">Technical Specifications</h3>
                <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
                  <!-- RAM -->
                  <div class="space-y-3">
                    <label for="ram" class="block text-sm font-medium text-gray-300">RAM (GB)</label>
                    <input 
                      type="text" 
                      id="ram"
                      class="w-full px-4 py-3.5 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white"
                      placeholder="8"
                    >
                  </div>
                  
                  <!-- Screen Size -->
                  <div class="space-y-3">
                    <label for="screenSize" class="block text-sm font-medium text-gray-300">Screen Size (Inches)</label>
                    <input 
                      type="text" 
                      id="screenSize"
                      class="w-full px-4 py-3.5 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white"
                      placeholder="6.1"
                    >
                  </div>
                  
                  <!-- Storage -->
                  <div class="space-y-3">
                    <label for="storage" class="block text-sm font-medium text-gray-300">Storage (GB)</label>
                    <input 
                      type="text" 
                      id="storage"
                      class="w-full px-4 py-3.5 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white"
                      placeholder="128"
                    >
                  </div>
                  
                  <!-- Color -->
                  <div class="space-y-3">
                    <label for="color" class="block text-sm font-medium text-gray-300">Color</label>
                    <input 
                      type="text" 
                      id="color"
                      class="w-full px-4 py-3.5 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white"
                      placeholder="Black"
                    >
                  </div>
                </div>
              </div>
              
              <!-- Inventory Section -->
              <div class="space-y-3 pt-6 border-t border-neutral-800">
                <h3 class="text-lg font-medium text-white mb-4">Inventory</h3>
                <div class="space-y-3">
                  <label for="quantity" class="block text-sm font-medium text-gray-300">Quantity</label>
                  <div class="relative">
                    <input 
                      type="number" 
                      id="quantity"
                      min="1"
                      class="w-full px-4 py-3.5 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white"
                      placeholder="10"
                    >
                    <div class="absolute inset-y-0 right-0 flex">
                      <button type="button" class="px-4 border-l border-neutral-700 text-gray-400 hover:bg-neutral-700 transition-colors rounded-r-xl">-</button>
                      <button type="button" class="px-4 border-l border-neutral-700 text-gray-400 hover:bg-neutral-700 transition-colors rounded-r-xl">+</button>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- Submit Buttons -->
              <div class="pt-8 flex flex-col sm:flex-row gap-4">
                <button 
                  type="submit"
                  class="flex-1 bg-white text-black py-4 px-6 rounded-full hover:bg-gray-200 transition-colors duration-300 font-medium"
                >
                  Save Product
                </button>
                <button 
                  type="button"
                  class="flex-1 bg-neutral-800 text-white py-4 px-6 rounded-full hover:bg-neutral-700 transition-colors duration-300 font-medium"
                >
                  Cancel
                </button>
              </div>
              <p class="text-center text-gray-500 text-sm">
                All changes will be saved to the inventory system
              </p>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
/* CSS for 3D effects */
.perspective {
  perspective: 1000px;
}
.transform-style-3d {
  transform-style: preserve-3d;
}
.rotate-y-5 {
  transform: rotateY(5deg);
}
.rotate-y-10 {
  transform: rotateY(10deg);
}
.rotate-x-3 {
  transform: rotateX(3deg);
}
.translate-z-20 {
  transform: translateZ(20px);
}
</style>