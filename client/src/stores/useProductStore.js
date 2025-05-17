import { defineStore } from "pinia";
import { getProducts, getProductById, addProduct, updateProductById, deleteProductById } from "@/utils/tool";
const BASE_URL = import.meta.env.VITE_BASE_URL;
import I14PROMAX from "@/assets/apple/iPhone-14-Pro-Max-Space-Black.webp";
import I14 from "@/assets/apple/iPhone_14_Midnight.png";
import I13PRO from "@/assets/apple/iphone-13-pro-blue-select.png";
import SE2020 from "@/assets/apple/iPhone_SE3_Starlight.webp";
import I14PLUS from "@/assets/apple/iPhone_14_Plus_Blue-square_medium.webp";

import HuaweiP40 from "@/assets/banner/huawei-p40-banner.webp";
import Mi9 from "@/assets/banner/Mi9-banner.jpg";
import S24 from "@/assets/banner/S24-banner.jpg";
import Vivo from "@/assets/banner/vivo-banner.webp";
export const useProductStore = defineStore("product", {
  state: () => ({
    products: [],
    productImages: {
      1: I14PROMAX,
      2: I14,
      3: I13PRO,
      7: SE2020,
      8: I14PLUS,
    },
    bannerImages: {
      1: HuaweiP40,
      2: Mi9,
      3: S24,
      4: Vivo,
    }
  }),
  getters: {
    allProducts: (state) => state.products,
    getProductById: (state) => {
      return (id) => state.products.find((product) => product.id === id);
    },    
    latestProduct: (state) => {
      return [...state.products]
        .sort((a, b) => new Date(b.updatedOn) - new Date(a.updatedOn))
        .slice(0, 5);
    },
    getProductBestSeller: (state) => (minRating = 4.5) => {
      return [...state.products]
        .filter(p => p && typeof p.rate === "number" && p.rate >= minRating)
        .sort((a, b) => b.rate - a.rate);
    }
  },
  actions: {
    async fetchProductDetail(id) {
      const product = await fetchWithErrorHandling(`${BASE_URL}/v1/sale-items/${id}`);
      this.selectedProduct = product;
      return product;
    },
    async loadProducts() {
      try {
        const data = await getProducts(`${BASE_URL}/v1/sale-items`);
    
        const normalized = data.map(product => ({
          ...product,
          rate: parseFloat(product.rate),
        }));
    
        this.products = normalized;
      } catch (err) {
        console.error("Failed to load all products", err);
      }
    },
    
    async createProduct(product) {
      try {
        const newProduct = await addProduct(`${BASE_URL}/v1/sale-items`, product);
        this.products.push(newProduct);
      } catch (err) {
        console.error("Failed to add product", err);
      }
    },
    async updateProduct(product) {
      try {
        const updated = await updateProductById(`${BASE_URL}/v1/sale-items`, product.id, product);
        const index = this.products.findIndex(p => p.id === product.id);
        if (index !== -1) this.products[index] = updated;
      } catch (err) {
        console.error("Failed to update product", err);
      }
    },
    async deleteProduct(id) {
      try {
        await deleteProductById(`${BASE_URL}/v1/sale-items`, id);        
        this.products = this.products.filter(p => p.id !== id);
      } catch (err) {
        console.error("Failed to delete product", err);
        throw err;
      }
    },
  },
});
