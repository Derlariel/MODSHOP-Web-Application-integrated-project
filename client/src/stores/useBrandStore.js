import { defineStore, acceptHMRUpdate } from 'pinia';
const BASE_URL = import.meta.env.VITE_BASE_URL;
import { ref } from 'vue'
import {
  getProducts,
  getProductById,
  addProduct,
  updateProductById,
  deleteProductById
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

    const createBrand = async (brandData) => {
      try {
        isLoading.value = true;
        error.value = null;
        
        const response = await addProduct(`${BASE_URL}/v1/brands`, brandData);
      
        await loadBrands();
        
        return response;
      } catch (err) {
        console.error('Error creating brand:', err);
        error.value = 'Failed to create brand';
        throw err;
      } finally {
        isLoading.value = false;
      }
    };
    

    const updateBrand = async (id, brandData) => {
      try {
        isLoading.value = true;
        error.value = null;
        const response = await updateProductById(`${BASE_URL}/v1/brands`, id, brandData);
        await loadBrands();
        return response;
      } catch (err) {
        console.error('Error updating brand:', err);
        error.value = 'Failed to update brand';
        throw err;
      } finally {
        isLoading.value = false;
      }
    };
    
    const deleteBrand = async (id) => {
  isLoading.value = true;
  error.value = null;
  
  try {
    await deleteProductById(`${BASE_URL}/v1/brands`, id);
    
    await loadBrands();
    
    return true;
    
  } catch (err) {
    console.error('Error deleting brand:', err);
    
    if (err.response && err.response.data) {
      const errorMessage = err.response.data.message;
      if (errorMessage && errorMessage.includes("associated sale items")) {
        error.value = "Brand cannot be deleted because it is associated with sale items.";
        return { error: "Brand has associated with sale items." };
      }
    }
    error.value = 'Delete brand failed';
    return { error: "An error occur while delete brand" };
    
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
      createBrand,
      updateBrand,
      deleteBrand
    };
  },
  {
    persist: true,
  }
);

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useBrandStore, import.meta.hot));
}