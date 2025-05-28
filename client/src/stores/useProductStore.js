import { defineStore } from "pinia";
import {
  getProducts,
  getProductById,
  addProduct,
  updateProductById,
  deleteProductById,
} from "@/utils/tool";
import defaultImage from "@/assets/default.jpg";
const BASE_URL = import.meta.env.VITE_BASE_URL;



import { getProductsPage } from "../utils/tool";
export const useProductStore = defineStore("product", {
  state: () => ({
    activePage: 1,
    totalPages: 0,
    products: [],
    productImages: [
      {
        id: 0,
        image: defaultImage,
      },
    ],
  }),
  getters: {
    allProducts: (state) => state.products,
    getActivePage: (state) => state.activePage,
    allPages: (state) => state.totalPages,
    getProductById: (state) => {
      return (id) => state.products.find((product) => product.id === id);
    },
    latestProduct: (state) => {
      return [...state.products]
        .sort((a, b) => new Date(b.updatedOn) - new Date(a.updatedOn))
        .slice(0, 5);
    },
    getProductBestSeller:
      (state) =>
      (minRating = 4.5) => {
        return [...state.products]
          .filter((p) => p && typeof p.rate === "number" && p.rate >= minRating)
          .sort((a, b) => b.rate - a.rate);
      },
  },
  actions: {
    setActivePage(page) {
      this.activePage = page;
    },
    async fetchProductDetail(id) {
      try {
        const product = await getProductById(`${BASE_URL}/v1/sale-items`, id);
        this.selectedProduct = product;
        return product;
      } catch (err) {
        console.error("Failed to fetch product details", err);
        throw err;
      }
    },
    async loadProducts() {
      try {
        const data = await getProducts(`${BASE_URL}/v1/sale-items`);

        const normalized = data
          ? data.map((product) => ({
              ...product,
              rate: parseFloat(product.rate),
            }))
          : [];

        this.products = normalized;
      } catch (err) {
        console.error("Failed to load all products", err);
      }
    },

    async loadProductsPage(params) {
      try {
        const data = await getProductsPage(`${BASE_URL}/v2/sale-items`, params);

        const normalized = Array.isArray(data.content)
          ? data.content.map((product) => ({
              ...product,
            }))
          : [];

        this.products = normalized;
      } catch (err) {
        console.error("Failed to load all products", err);
      }
    },

    async loadAllPages(params) {
      try {
        const data = await getProductsPage(`${BASE_URL}/v2/sale-items`, params);
        console.log("data.totalPages "+data.totalPages)
        this.totalPages = data.totalPages;
        
      } catch (err) {
        console.error("Failed to load all page products", err);
      }
    },

    async createProduct(product) {
      try {
        const newProduct = await addProduct(
          `${BASE_URL}/v1/sale-items`,
          product
        );
        this.products.push(newProduct);
      } catch (err) {
        console.error("Failed to add product", err);
      }
    },
    async updateProduct(product) {
      try {
        const updated = await updateProductById(
          `${BASE_URL}/v1/sale-items`,
          product.id,
          product
        );
        const index = this.products.findIndex((p) => p.id === product.id);
        if (index !== -1) this.products[index] = updated;
      } catch (err) {
        console.error("Failed to update product", err);
      }
    },
    async deleteProduct(id) {
      try {
        await deleteProductById(`${BASE_URL}/v1/sale-items`, id);
        this.products = this.products.filter((p) => p.id !== id);
      } catch (err) {
        console.error("Failed to delete product", err);
        throw err;
      }
    },
  },
});
