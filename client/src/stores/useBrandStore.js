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

    const addBrand = async (brandData) => {
      try {
        // Trim string values before sending to backend
        const trimmedData = {
          ...brandData,
          name: brandData.name?.trim(),
          websiteUrl: brandData.websiteUrl?.trim(),
          countryOfOrigin: brandData.countryOfOrigin?.trim()
        };
        
        const response = await fetch(`${BASE_URL}/v1/brands`, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify(trimmedData)
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({}));
          throw new Error(errorData.message || 'Failed to add brand');
        }

        const data = await response.json();
        await loadBrands(); 
        return { success: true, data };
      } catch (err) {
        console.error('Error adding brand:', err);
        return { success: false, error: err.message || 'Failed to add brand' };
      }
    };

    return {
      brands,
      isLoading,
      error,
      getBrands,
      loadBrands,
      addBrand
    };
  },
  {
    persist: true,
  }
);

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useBrandStore, import.meta.hot));
}
