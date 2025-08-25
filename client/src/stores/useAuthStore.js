import { defineStore } from "pinia";
import axios from "axios";

const BASE_URL = import.meta.env.VITE_BASE_URL;

export const useAuthStore = defineStore("auth", {
  state: () => ({
    isSubmitting: false,
  }),
  actions: {
    async register(formData) {
      this.isSubmitting = true;
      try {
        const res = await axios.post(`${BASE_URL}/v1/register`, formData, {
          headers: { "Content-Type": "multipart/form-data" },
          validateStatus: () => true, 
        });
        if (res.status !== 201) {
          const msg =
            (res.data && (res.data.message || res.data.error)) ||
            "Registration failed.";
          throw new Error(msg);
        }
        return res.data; // user account detail
      } finally {
        this.isSubmitting = false;
      }
    },
  },
});
