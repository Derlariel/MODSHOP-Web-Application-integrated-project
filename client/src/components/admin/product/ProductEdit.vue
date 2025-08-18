<script setup>
import { useBrandStore } from "@/stores/useBrandStore";
import { onMounted, ref, computed } from "vue";
import ProductPicture from "@/components/shared/ProductPicture.vue";
import ProductForm from "./ProductForm.vue";
import { useProductStore } from "@/stores/useProductStore";
import { useRoute, useRouter } from "vue-router";
import HistoryPath from "@/components/shared/HistoryPath.vue";

const { params } = useRoute();
const router = useRouter();

const product = ref(null);
const productStore = useProductStore();
const brandStore = useBrandStore();
const picSelect = ref([]); // Store emitted images from ProductPicture

console.log("Product ID from route params:", params.productId);

const edit = async (data) => {
  try {
    // Create FormData object
    const formData = new FormData();

    Object.keys(data).forEach(key => {
  if (key === "brand" && typeof data[key] === "object" && data[key] !== null) {
    // แตก brand object ออกมา
    if (data[key].id !== undefined) {
      formData.append("saleItem.brand.id", data[key].id);
    }
    if (data[key].name !== undefined) {
      formData.append("saleItem.brand.name", data[key].name);
    }
  } else {
    formData.append(`saleItem.${key}`, data[key]);
  }
});

    // Append imageInfos from picSelect
    picSelect.value.forEach((image, index) => {
      formData.append(`imageInfos[${index}].order`, image.order);
      formData.append(`imageInfos[${index}].fileName`, image.fileName);
      formData.append(`imageInfos[${index}].status`, image.status);
      if (image.imageFile) {
        // Append File object for new images
        formData.append(`imageInfos[${index}].imageFile`, image.imageFile);
      }
    });

    // Debug FormData contents
    console.log("FormData contents:");
    for (let [key, value] of formData.entries()) {
      console.log(`${key}:`, value);
    }

    // Send FormData to updateProduct
    await productStore.updateProduct(params.productId, formData);

    sessionStorage.setItem("edit-success", "true");
    router.push(`/sale-items/${params.productId}`);
  } catch (error) {
    console.error("Error updating product:", error);
    sessionStorage.setItem("error-message", "true");
  }
};

const title = computed(() => {
  if (!product.value) return "";

  const {
    brandName = "",
    model = "",
    ramGb = "",
    storageGb = "",
    color = "",
  } = product.value;

  return `${brandName} ${model} ${ramGb}/${storageGb}GB ${color}`.trim();
});

onMounted(async () => {
  try {
    await productStore.loadProducts();
    product.value = await productStore.fetchProductDetail(params.productId);
  } catch (e) {
    router.push("/sale-items");
    sessionStorage.setItem("error-message", "true");
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
        <HistoryPath :name-path="title" :previous="2" :next="1" />
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16">
          <ProductPicture
            v-if="product && product.saleItemImages"
            :images="product.saleItemImages"
            :editMode="true"
            @savePic="picSelect = $event"
          />
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