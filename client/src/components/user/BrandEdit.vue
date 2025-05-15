<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import { useBrandStore } from '@/stores/useBrandStore';
import HistoryPath from '@/components/shared/HistoryPath.vue';
import BrandForm from '@/components/user/BrandForm.vue';
import SuccessModal from '@/components/shared/modal/SuccessModal.vue';

const route = useRoute();
const router = useRouter();
const brandStore = useBrandStore();

const brandId = ref(null);
const isLoading = ref(true);
const errorMessage = ref(null);
const brandData = ref({});
const showSuccessModal = ref(false);
const successMessage = ref("");
const previousPath = ref({
  name: 'Brands',
  path: '/brands'
});

onMounted(async () => {
  try {
    brandId.value = parseInt(route.params.brandId);
    
    if (isNaN(brandId.value)) {
      errorMessage.value = "Invalid brand ID";
      setTimeout(() => router.push('/brands'), 1500);
      return;
    }
    
    const data = await brandStore.fetchBrandDetail(brandId.value);
    
    if (!data) {
      errorMessage.value = "The brand does not exist.";
      setTimeout(() => router.push('/brands'), 1500);
      return;
    }
    
    brandData.value = data;
    isLoading.value = false;
  } catch (error) {
    console.error("Error loading brand:", error);
    errorMessage.value = "The brand does not exist.";
    setTimeout(() => router.push('/brands'), 1500);
  }
});

const updateBrand = async (formData) => {
  try {
    const brandToUpdate = {
      ...formData,
      id: brandId.value,
    };
    const result = await brandStore.updateBrand(brandToUpdate);
    if (result.success) {
      brandData.value = {
        ...brandData.value,
        ...brandToUpdate
      };
      successMessage.value = "The brand has been updated";
      showSuccessModal.value = true;
      setTimeout(() => {
        router.push('/brands');
      }, 1500);
    } else {
      errorMessage.value = result.error || "Failed to update brand";
    }
  } catch (error) {
    console.error("Error updating brand:", error);
    if (error.response?.status === 404) {
      errorMessage.value = "The brand does not exist.";
      setTimeout(() => router.push('/brands'), 1500);
    } else {
      errorMessage.value = "An error occurred while updating the brand.";
    }
  } finally {
    if (errorMessage.value) {
      isLoading.value = false;
    }
  }
};
</script>
 
<template>
  <div class="min-h-screen bg-black text-white">
    <SuccessModal :visible="showSuccessModal" :message="successMessage" />
    
    <div v-if="errorMessage" class="pt-24 pb-20 text-center">
      <div class="bg-red-900/30 text-red-400 p-6 rounded-lg max-w-lg mx-auto">
        {{ errorMessage }}
      </div>
    </div>
    
    <div v-else class="pt-24 pb-20">
      <div class="max-w-[1200px] mx-auto px-6">
        <HistoryPath 
          name-path="Edit Brand"
          :previous="1"
          :previousPathName="previousPath.name"
          :previousPath="previousPath.path"
        />
        
        <div class="max-w-2xl mx-auto mt-10">
          <div v-if="isLoading" class="text-center py-16">
            <div class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-white mx-auto"></div>
            <p class="mt-6 text-gray-400 text-lg">Loading brand data...</p>
          </div>
          
          <BrandForm 
            v-else 
            :init="brandData" 
            @submit="updateBrand"
          />
        </div>
      </div>
    </div>
  </div>
</template>
 
<style scoped></style>