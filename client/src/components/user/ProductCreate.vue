<script setup>
import { } from "vue";
import ProductPicture from "./ProductPicture.vue";
import ProductForm from "./ProductForm.vue";
import { useProductStore } from "@/stores/useProductStore";

const productStore = useProductStore()

const add = (data) => {
  productStore.createProduct(data)
  console.log(data);
  
}

</script>

<template>
  <div class="min-h-screen bg-black text-white">
    <div class="pt-24 pb-20">
      <div class="max-w-[1200px] mx-auto px-6">
        <h1 class="text-3xl md:text-4xl font-semibold tracking-tight mb-8">Create New Product</h1>
      
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16">
          <ProductPicture />
          <ProductForm @submit="add" />
        </div>
      </div>
    </div>
  </div>
</template>

<style>

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