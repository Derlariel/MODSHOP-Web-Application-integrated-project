import { defineStore, acceptHMRUpdate } from "pinia";
const BASE_URL = import.meta.env.VITE_BASE_URL;
import { ref, computed } from "vue";
import { getProducts, getProductById } from "@/utils/tool";

export const useBrandStore = defineStore(
  "brand",
  () => {
    const brands = ref([]);
    const isLoading = ref(false);
    const error = ref(null);
    const selectedBrand = ref(null);

    const allBrands = computed(() => brands.value);
    const getBrandById = computed(() => {
      return (id) => brands.value.find((brand) => brand.id === id);
    });
    
    const getActiveBrands = computed(() => {
      return brands.value.filter(brand => brand.isActive);
    });

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
        console.error("Error loading brands:", err);
        error.value = "Failed to load brands";
        return [];
      } finally {
        isLoading.value = false;
      }
    };

    const fetchBrandDetail = async (id) => {
      try {
        isLoading.value = true;
        error.value = null;
        const brand = await getProductById(`${BASE_URL}/v1/brands`, id);
        selectedBrand.value = brand;
        return brand;
      } catch (err) {
        console.error(`Failed to load brand ID:${id}`, err);
        error.value = `Failed to load brand with ID: ${id}`;
        return null;
      } finally {
        isLoading.value = false;
      }
    };

    const addBrand = async (brandData) => {
      try {
        isLoading.value = true;
        error.value = null;
        
        const response = await fetch(`${BASE_URL}/v1/brands`, {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(brandData),
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({}));
          throw new Error(errorData.message || "Failed to add brand");
        }

        const data = await response.json();
        await loadBrands(); 
        return { success: true, data };
      } catch (err) {
        console.error("Error adding brand:", err);
        error.value = err.message || "Failed to add brand";
        return { success: false, error: err.message || "Failed to add brand" };
      } finally {
        isLoading.value = false;
      }
    };

    const updateBrand = async (brandData) => {
      try {
        isLoading.value = true;
        error.value = null;
        
        const response = await fetch(`${BASE_URL}/v1/brands/${brandData.id}`, {
          method: "PUT",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(brandData),
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({}));
          throw new Error(errorData.message || "Failed to update brand");
        }

        const updated = await response.json();
        
        const index = brands.value.findIndex(b => b.id === brandData.id);
        if (index !== -1) brands.value[index] = updated;
        
        return { success: true, data: updated };
      } catch (err) {
        console.error("Error updating brand:", err);
        error.value = err.message || "Failed to update brand";
        return { success: false, error: err.message || "Failed to update brand" };
      } finally {
        isLoading.value = false;
      }
    };

    const deleteBrand = async (id) => {
      try {
        isLoading.value = true;
        error.value = null;
        
        const response = await fetch(`${BASE_URL}/v1/brands/${id}`, {
          method: "DELETE",
        });

        if (!response.ok) {
          const errorData = await response.json().catch(() => ({}));
          throw new Error(errorData.message || "Failed to delete brand");
        }

        brands.value = brands.value.filter(b => b.id !== id);
        
        return { success: true };
      } catch (err) {
        console.error("Error deleting brand:", err);
        error.value = err.message || "Failed to delete brand";
        return { success: false, error: err.message || "Failed to delete brand" };
      } finally {
        isLoading.value = false;
      }
    };

    return {
      // State
      brands,
      isLoading,
      error,
      selectedBrand,
      
      // Getters
      allBrands,
      getBrandById,
      getActiveBrands,
      getBrands,
      
      // Actions
      loadBrands,
      fetchBrandDetail,
      addBrand,
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