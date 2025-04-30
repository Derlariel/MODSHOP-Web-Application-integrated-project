import { defineStore } from "pinia";
import { getProducts, getProductById, addProduct, updateProductById, deleteProductById } from "@/utils/tool";
import I14PROMAX from '@/assets/apple/iPhone-14-Pro-Max-Space-Black.webp'
import I14 from '@/assets/apple/iPhone_14_Midnight.png'
import I13PRO from '@/assets/apple/iphone-13-pro-blue-select.png'
import SE2020 from '@/assets/apple/iPhone_SE3_Starlight.webp'
import I14PLUS from '@/assets/apple/iPhone_14_Plus_Blue-square_medium.webp'
import S23ULTRA from '@/assets/samsung/Samsung-Galaxy-S23-Ultra.webp'
const BASE_URL = import.meta.env.VITE_BASE_URL;

export const useProductStore = defineStore("product", {
  state: () => ({
    products: [],
    productImages: {
      1: I14PROMAX,
      2: I14,
      3: I13PRO,
      7: SE2020,
      8: I14PLUS,
      16: S23ULTRA,
    }
  }),
  getters: {
    allProducts: (state) => state.products,
    getProductById: (state) => (id) => state.products.find(p => p.id === id),
  },
  actions: {
    async loadProducts() {
      try {
        this.products = await getProducts(`${BASE_URL}/v1/sale-items`);
      } catch (err) {
        console.error("Failed to load all products", err);
      }
    },
    async fetchProductDetail(id) {
      try {
        return await getProductById(`${BASE_URL}/v1/sale-items`, id);
      } catch (err) {
        console.error(`Failed to load product ID:${id}`, err);
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
      }
    },
  },
});
