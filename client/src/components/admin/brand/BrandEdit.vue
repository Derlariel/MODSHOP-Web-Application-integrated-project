<script setup>
import { ref, onMounted } from "vue";
import { useBrandStore } from "@/stores/useBrandStore";
import { useRouter, useRoute } from "vue-router";
import SkeletonLoader from "@/components/shared/SkeletonLoader.vue";
import BrandForm from "./BrandForm.vue";

const router = useRouter();
const route = useRoute();
const brandStore = useBrandStore();

const brandId = Number(route.params.brandId);
const brand = ref(null);
const isLoading = ref(true);
const errorMessage = ref("");
const isSubmitting = ref(false);

onMounted(async () => {
  try {
    await brandStore.loadBrands();
    const brands = brandStore.getBrands();
    brand.value = brands.find((b) => b.id === brandId);

    if (!brand.value) {
      errorMessage.value = "Brand not found";
    }
  } catch (error) {
    console.error("Error loading brand:", error);
    errorMessage.value = "Failed to load brand details";
  } finally {
    isLoading.value = false;
  }
});

const handleSubmit = async (brandData) => {
  try {
    isSubmitting.value = true;
    await brandStore.updateBrand(brandId, brandData);

    sessionStorage.setItem("brand-edit-success", "true");

    router.push({ name: "brands-list" });
  } catch (error) {
    console.error("Error updating brand:", error);
    errorMessage.value = "The brand could not be updated.";
  } finally {
    isSubmitting.value = false;
  }
};

const handleCancel = () => {
  router.push({ name: "brands-list" });
};
const navigateToSaleItems = () => {
  router.push({ name: "product-list" });
};
const navigateToBrandList = () => {
  router.push({ name: "brands-list" });
};
</script>

<template>
  <div class="min-h-screen bg-black text-white pt-24 pb-16 px-4">
    <div class="max-w-3xl mx-auto">
      <h1 class="text-3xl font-semibold mb-4">Edit Brand</h1>

      <div class="flex cursor-pointer font-light mb-5 space-x-2.5">
        <p class="itbms-item-list" @click="navigateToSaleItems">
          Sale Item List
        </p>
        <p>/</p>
        <p class="itbms-manage-brand" @click="navigateToBrandList">
          Brand List
        </p>
        <p>/</p>
        <p
          class="bg-gradient-to-r from-neutral-100 to-blue-200 text-black px-4 rounded-3xl"
        >
          Update Brand
        </p>
      </div>

      <div v-if="isLoading" class="text-center py-12">
        <SkeletonLoader />
      </div>
      <div
        v-else-if="!brand"
        class="bg-red-500 bg-opacity-20 border border-red-500 rounded-md p-4 text-red-300"
      >
        {{ errorMessage || "Brand not found" }}
      </div>
      <BrandForm
        v-else
        :initial-data="brand"
        mode="edit"
        :is-submitting="isSubmitting"
        :error-message="errorMessage"
        submit-button-text="Update"
        :is-edit-mode="true"
        @submit="handleSubmit"
        @cancel="handleCancel"
      />  
      
    </div>
  </div>
</template>
