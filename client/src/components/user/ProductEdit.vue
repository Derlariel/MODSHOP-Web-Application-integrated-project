<script setup>
import { useBrandStore } from "@/stores/useBrandStore";
import { onMounted, ref, computed } from "vue";
import ProductPicture from "./ProductPicture.vue";
import ProductForm from "./ProductForm.vue";
import { useProductStore } from "@/stores/useProductStore";

import { useRoute, useRouter } from "vue-router";
import HistoryPath from "../shared/HistoryPath.vue";
const { params } = useRoute();
const router = useRouter();

const product = ref(null);
const productStore = useProductStore();
const brandStore = useBrandStore();

const edit = async (data) => {
  await productStore.updateProduct(data);
  sessionStorage.setItem("edit-success", "true");
  if (referrer === 'list') {
    router.push('/sale-items/list');
  } else {
    router.push(`/sale-items/${params.productId}`);
  }
};

onMounted(async () => {
  brandStore.loadBrands();
  product.value = await productStore.fetchProductDetail(params.productId);
});

const route = useRoute();
const referrer = route.query.from || "detail";
const returnTo = route.query.returnTo || "gallery";

const previousPath = computed(() => {
  if (referrer === 'list') {
    return {
      name: 'Sale Items List',
      path: '/sale-items/list'
    };
  } else {
    return {
      name: `${product.value?.model || "Product"}`,
      path: `/sale-items/${route.params.productId}?from=${returnTo}`
    };
  }
});
</script>

<template>
  <div class="min-h-screen bg-black text-white">
    <div class="pt-24 pb-20">
      <div class="max-w-[1200px] mx-auto px-6">
        <h1 class="text-3xl md:text-4xl font-semibold tracking-tight mb-4">
          Edit Product
        </h1>
        <HistoryPath
          name-path="Edit Sale Item"
          :previous="1"
          :previousPathName="previousPath.name"
          :previousPath="previousPath.path"
        />
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16">
          <ProductPicture />
          <ProductForm @submit="edit" v-if="product" :init="product" />
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
