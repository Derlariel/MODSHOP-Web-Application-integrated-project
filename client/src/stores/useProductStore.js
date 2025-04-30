import { defineStore } from "pinia";
import { getProducts, getProductById, addProduct, updateProductById, deleteProductById } from "@/utils/tool";

const BASE_URL = import.meta.env.VITE_BASE_URL;

export const useProductStore = defineStore("product", {
  state: () => ({
    products: [],
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
