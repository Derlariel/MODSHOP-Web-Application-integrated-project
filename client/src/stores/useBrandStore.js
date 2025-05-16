import { defineStore, acceptHMRUpdate } from 'pinia';
const BASE_URL = import.meta.env.VITE_BASE_URL;
import { ref } from 'vue'
import {
  getProducts,
  getProductById,
} from '@/utils/tool';


export const useBrandStore = defineStore(
  'brand',
  () => {
    const brands = ref([]);
    const isLoading = ref(false);
    const error = ref(null);

    const getBrands = () => {
      return brands.value;
    };
      
    const loadBrands = async () => {
      try {
        isLoading.value = true;
        error.value = null;
        brands.value = await getProducts(`${BASE_URL}/v1/brands`);
        return brands.value;
      } catch (err) {
        console.error('Error loading brands:', err);
        error.value = 'Failed to load brands';
        return [];
      } finally {
        isLoading.value = false;
      }
    };

  

    return {
      brands,
      isLoading,
      error,
      getBrands,
      loadBrands,
    };
  },
  {
    persist: true,
  }
);

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useBrandStore, import.meta.hot));
}