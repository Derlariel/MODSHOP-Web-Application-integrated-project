import { defineStore, acceptHMRUpdate } from 'pinia';
const BASE_URL = import.meta.env.VITE_BASE_URL;
import { ref } from 'vue'
import {
  getProducts,
  getProductById,
} from '@/utils/tool';


export const useBrandStore = defineStore (
  'brand',
  () => {
    const brands = ref ([]);

    const getBrands = () => {
      return brands.value;
    };
      
    const loadBrands = async () => {
        brands.value = await getProducts(`${BASE_URL}/v1/brands`)
    }

    return {
        getBrands,
        loadBrands
    };
  },
  {
    persist: true,
  }
);

if (import.meta.hot) {
  import.meta.hot.accept(acceptHMRUpdate(useBrandStore, import.meta.hot));
}
