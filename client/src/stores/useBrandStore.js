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

    const filterBrands = () => {
      return brands.value
      .map(b => b.name)
      .filter(name => !!name)
      .sort((a,b) => a.localeCompare(b))
    }
    

    const createBrand = async (brandData) => {
      try {
        isLoading.value = true;
        error.value = null;
        
        // Create the new brand via API
        const response = await addProduct(`${BASE_URL}/v1/brands`, brandData);
        
        // Refresh brands list
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

    const getBrandById = async (id) => {
      try {
        isLoading.value = true;
        error.value = null;
        const brand = await getProductById(`/v1/brands`, id);
        return brand;
      } catch (err) {
        console.error('Error loading brand details:', err);
        error.value = 'Failed to load brand details';
        throw err;
      } finally {
        isLoading.value = false;
      }
    };
    
    const updateBrand = async (id, brandData) => {
      try {
        isLoading.value = true;
        error.value = null;
        const response = await updateProductById(`/v1/brands`, id, brandData);
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
      try {
        isLoading.value = true;
        error.value = null;
        await deleteProductById(`/v1/brands`, id);
        await loadBrands();
      } catch (err) {
        console.error('Error deleting brand:', err);
        error.value = 'Failed to delete brand';
        throw err;
      }
    };

    return {
      brands,
      isLoading,
      error,
      getBrands,
      loadBrands,
      filterBrands,
      createBrand,
      updateBrand,
      deleteBrand,
      getBrandById
    };
  },
  {
    persist: true,
  }
);

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useBrandStore, import.meta.hot));
}