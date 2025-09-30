<script setup>
import {ref} from "vue";
import ProductPicture from "@/components/shared/ProductPicture.vue";
import ProductForm from "./ProductForm.vue";
import { useProductStore } from "@/stores/useProductStore";
import { useRouter } from "vue-router";
import HistoryPath from "@/components/shared/HistoryPath.vue";

const productStore = useProductStore();
const router = useRouter();

const picSelect = ref(null);

const saleItemPic = (data) => {
  picSelect.value = data;
};

async function urlToFile(url, filename, mimeType) {
  const res = await fetch(url);
  const buf = await res.arrayBuffer();
  return new File([buf], filename, { type: mimeType });
}


const add = async (data) => {
  try {
    let files = [];

    // Check if picSelect.value is an array of File objects (non-edit mode)
    if (Array.isArray(picSelect.value) && picSelect.value.every(item => item instanceof File)) {
      files = picSelect.value;
    } 
    // Check if picSelect.value is an array of objects (edit mode)
    else if (Array.isArray(picSelect.value) && picSelect.value.some(item => item.imageFile)) {
      files = picSelect.value
        .filter(item => item.status === 'NEW' && item.imageFile instanceof File)
        .map(item => item.imageFile);
    }

    // Assign the File array to data.images
    data.images = files;

    await productStore.createProduct(data);

    sessionStorage.setItem("add-success", "true");
  router.push({ name: "product-list" });
    sessionStorage.setItem("activePage", 1);
    productStore.setActivePage(1)
  } catch (error) {
    console.error("Error creating product:", error);
  }
};


</script>

<template>
  <div class="min-h-screen bg-black text-white">
    <div class="pt-24 pb-20">
      <div class="max-w-[1200px] mx-auto px-6">
        <h1 class="text-xl md:text-4xl font-semibold tracking-tight mb-4">
          Create New Product
        </h1>
        <HistoryPath :previous="1" name-path="New Sale Item" />
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16">
          <ProductPicture @save-pic="saleItemPic" />
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
