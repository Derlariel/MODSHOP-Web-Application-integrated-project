import { defineStore } from "pinia";
import {
  getProducts,
  getProductById,
  addProduct,
  updateProductById,
  deleteProductById,
} from "@/utils/tool";
import defaultImage from "@/assets/default.jpg";
import { getProductsPage } from "../utils/tool";
import { LucideProjector } from "lucide-vue-next";

const BASE_URL = import.meta.env.VITE_BASE_URL;

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
    selectedProduct: null,
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
        return data;
      } catch (err) {
        console.error("Failed to load all products", err);
        this.products = [];
        throw err;
      }
    },

    async loadAllPages(params) {
      try {
        const data = await getProductsPage(`${BASE_URL}/v2/sale-items`, params);
        console.log("data.totalPages " + data.totalPages);
        this.totalPages = data.totalPages;
        return data;
      } catch (err) {
        console.error("Failed to load all page products", err);
      }
    },

    async createProduct(product) {
      try {
        const formData = new FormData();

        const plainProduct = JSON.parse(JSON.stringify(product));

        formData.append("model", plainProduct.model);
        formData.append("description", plainProduct.description);
        formData.append("price", plainProduct.price);
        formData.append("ramGb", plainProduct.ramGb);
        formData.append("screenSizeInch", plainProduct.screenSizeInch);
        formData.append("quantity", plainProduct.quantity);
        formData.append("storageGb", plainProduct.storageGb);
        formData.append("color", plainProduct.color);
        formData.append("brand.id", plainProduct.brand.id);
        formData.append("brand.name", plainProduct.brand.name);

        if (product.images && Array.isArray(product.images)) {
          product.images.forEach((file, index) => {
            if (file instanceof File) {
              formData.append("images", file);
            } else {
              console.warn(
                `Image at index ${index} is not a File, skipping:`,
                file
              );
            }
          });
        }

        const newProduct = await addProduct(
          `${BASE_URL}/v2/sale-items`,
          formData
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
    async removeSaleItemPictures(
      saleItemId,
      payload /* { deleteIds:[], order:[] } */
    ) {
      try {
        const res = await fetch(
          `${BASE_URL}/v2/sale-items/${saleItemId}/pictures`,
          {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
          }
        );
        if (!res.ok) {
          const text = await res.text().catch(() => null);
          throw new Error(
            `Failed to update images: ${res.status} ${text ?? ""}`
          );
        }
        const data = await res.json(); // expect Array<{id, fileName, position}>

        // Update local cache minimal-change: replace pictures only
        const idx = this.products.findIndex((p) => p.id === saleItemId);
        if (idx !== -1) {
          // shallow clone product and replace pictures ref
          this.products[idx] = { ...this.products[idx], pictures: data };
        }
        if (this.selectedProduct?.id === saleItemId) {
          this.selectedProduct = { ...this.selectedProduct, pictures: data };
        }
        return data;
      } catch (err) {
        console.error("removeSaleItemPictures error", err);
        throw err;
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
