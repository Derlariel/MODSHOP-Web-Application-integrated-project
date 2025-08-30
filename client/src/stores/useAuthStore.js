import { defineStore } from "pinia";
import axios from "axios";
import { validateEmailPassword } from "@/utils/validate";

const BASE_URL = import.meta.env.VITE_BASE_URL;

export const useAuthStore = defineStore("auth", {
  state: () => ({
    isSubmitting: false,
    isAuthenticated: false,
    user: null,
  }),
  actions: {
    async register(formData) {
      this.isSubmitting = true;
      try {
        const res = await axios.post(`${BASE_URL}/v2/users/register`, formData, {
          headers: { "Content-Type": "multipart/form-data" },
          validateStatus: () => true, 
        });
        if (res.status !== 201) {
          const msg =
            (res.data && (res.data.message || res.data.error)) ||
            "Registration failed.";
          throw new Error(msg);
        }
        return res.data; 
      } finally {
        this.isSubmitting = false;
      }
    },

 async login({ email, password }) {
      this.isSubmitting = true;
      try {
        const { valid } = validateEmailPassword({ email, password });
        if (!valid) throw new Error("Email or Password is incorrect.");
        console.log("Sending login request...");
        const res = await axios.post(
          `${BASE_URL}/v2/users/authentications`,
          { email, password },
          { validateStatus: () => true }
        );

        console.log("Login response:", res);

        if (res.status === 200) {
          this.isAuthenticated = true;
          this.user = res.data;
          return res.data;
        }

        throw new Error(res.status === 400 || res.status === 401 ? "Email or Password is incorrect." : "Please try again later.");
      } finally {
        this.isSubmitting = false;
      }
    },
  }
});
