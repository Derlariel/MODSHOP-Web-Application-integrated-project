<script setup>
import { ref } from 'vue';
import { useBrandStore } from '@/stores/useBrandStore';
import { useRouter } from 'vue-router';
import BrandForm from './BrandForm.vue';
import AppFooter from "../shared/AppFooter.vue";

const router = useRouter();
const brandStore = useBrandStore();
const isLoading = ref(false);
const errorMessage = ref('');

const saveBrand = async (brandData) => {
  try {
    isLoading.value = true;
    await brandStore.createBrand(brandData);
    sessionStorage.setItem('brand-add-success', 'true');
    router.push('/brands');
  } catch (error) {
    console.error('Error creating brand:', error);
    errorMessage.value = 'The brand could not be added.';
  } finally {
    isLoading.value = false;
  }
};

const handleCancel = () => {
  router.push('/brands');
};
const navigateToSaleItems = () => {
  router.push({ name: 'product-list' });
};

const navigateToBrandList = () => {
  router.push({ name: 'brands-list' });
};

</script>

<template>
  <div class="min-h-screen bg-black text-white pt-24 pb-16 px-4">
    <div class="max-w-3xl mx-auto">
      <h1 class="text-3xl font-semibold mb-4">Add New Brand</h1>
      
      <div class="flex cursor-pointer font-light mb-5 space-x-2.5">
        <p class="itbms-item-list" @click="navigateToSaleItems">Sale Item List</p>
        <p>/</p>
        <p class="itbms-manage-brand" @click="navigateToBrandList">Brand List</p>
        <p>/</p>
        <p class="bg-gradient-to-r from-neutral-100 to-blue-200 text-black px-4 rounded-3xl">
          New Brand
        </p>
      </div>
      

      <div class="bg-neutral-900 rounded-lg p-6 shadow-lg">
        <div v-if="errorMessage" class="mb-6 p-4 bg-red-500 bg-opacity-20 border border-red-500 rounded-md text-red-300">
          {{ errorMessage }}
        </div>
        <BrandForm 
          mode="create"
          :isSubmitting="isLoading"
          @submit="saveBrand"
          @cancel="handleCancel"
        />
      </div>
    </div>
    <AppFooter />
  </div>
</template>