<script setup>
import { ref, reactive, computed } from 'vue';
import { useBrandStore } from '@/stores/useBrandStore';
import { useRouter } from 'vue-router';
import HistoryPath from '../shared/HistoryPath.vue';

const router = useRouter();
const brandStore = useBrandStore();
const isLoading = ref(false);
const errorMessage = ref('');

const brand = reactive({
  name: '',
  websiteUrl: '',
  isActive: true,
  countryOfOrigin: ''
});

const isSaveDisabled = computed(() => {
  return !brand.name.trim();
});

const submit = async () => {
  if (isSaveDisabled.value) return;
  
  isLoading.value = true;
  errorMessage.value = '';
  
  try {
    const result = await brandStore.addBrand(brand);
    
    if (result.success) {
      sessionStorage.setItem('brand-add-success', 'true');
      router.push('/brands');
    } else {
      errorMessage.value = 'The status could not be added.';
    }
  } catch (error) {
    console.error('Error adding brand:', error);
    errorMessage.value = 'The status could not be added.';
  } finally {
    isLoading.value = false;
  }
};

const handleCancel = () => {
  router.push('/brands');
};
</script>
 
<template>
  <div class="min-h-screen bg-black text-white pt-24 pb-16 px-4">
    <div class="max-w-3xl mx-auto">
      <HistoryPath
        name-path="Add Brand"
        :previous="1"
        previousPathName="Manage Brand"
        previousPath="/brands"
      />
      
      <h1 class="text-3xl font-bold mb-8">Add New Brand</h1>
      
      <div v-if="errorMessage" class="bg-red-500 bg-opacity-20 border border-red-500 text-white px-4 py-3 rounded mb-6">
        {{ errorMessage }}
      </div>
      
      <form @submit.prevent="submit" class="space-y-6">
        <div class="space-y-2">
          <label for="name" class="block text-sm font-medium">Brand Name <span class="text-red-500">*</span></label>
          <input
            v-model="brand.name"
            id="name"
            type="text"
            class="itbms-brand-name w-full px-4 py-3 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white"
            required
            :disabled="isLoading"
          />
        </div>
        
        <div class="space-y-2">
          <label for="websiteUrl" class="block text-sm font-medium">Website URL</label>
          <input
            v-model.trim="brand.websiteUrl"
            id="websiteUrl"
            type="text"
            class="itbms-brand-url w-full px-4 py-3 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white"
            :disabled="isLoading"
          />
        </div>
        
        <div class="space-y-2">
          <label class="block text-sm font-medium">Active Status</label>
          <div class="flex items-center space-x-4">
            <div class="flex items-center">
              <input
                v-model.trim="brand.isActive"
                id="active-yes"
                name="isActive"
                type="radio"
                :value="true"
                class="itbms-brand-active-yes mr-2"
                :disabled="isLoading"
              />
              <label for="active-yes">Yes</label>
            </div>
            
            <div class="flex items-center">
              <input
                v-model="brand.isActive"
                id="active-no"
                name="isActive"
                type="radio"
                :value="false"
                class="itbms-brand-active-no mr-2"
                :disabled="isLoading"
              />
              <label for="active-no">No</label>
            </div>
          </div>
        </div>
        
        <div class="space-y-2">
          <label for="countryOfOrigin" class="block text-sm font-medium">Country of Origin</label>
          <input
            v-model.trim="brand.countryOfOrigin"
            id="countryOfOrigin"
            type="text"
            class="itbms-brand-country w-full px-4 py-3 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white"
            :disabled="isLoading"
          />
        </div>
        
        <div class="pt-6 flex justify-end space-x-4">
          <button
            type="button"
            @click="handleCancel"
            class="itbms-brand-cancel px-6 py-2.5 border border-neutral-600 rounded-lg hover:bg-neutral-800 transition-colors duration-200"
            :disabled="isLoading"
          >
            Cancel
          </button>
          
          <button
            type="submit"
            class="itbms-brand-save px-6 py-2.5 bg-white text-black rounded-lg hover:bg-gray-200 transition-colors duration-200 font-medium"
            :disabled="isSaveDisabled || isLoading"
            :class="{ 'opacity-50 cursor-not-allowed': isSaveDisabled || isLoading }"
          >
            {{ isLoading ? 'Saving...' : 'Save Brand' }}
          </button>
        </div>
      </form>
    </div>
  </div>
</template>