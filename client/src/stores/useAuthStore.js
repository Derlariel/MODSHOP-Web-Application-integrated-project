import { defineStore } from "pinia";
import axios from "axios";
import { validateEmailPassword } from "@/utils/validate";
import { decodeJwt } from "@/utils/jwt";

const BASE_URL = import.meta.env.VITE_BASE_URL;

export const api = axios.create({ baseURL: BASE_URL });

api.interceptors.request.use((config) => {
  const token = sessionStorage.getItem("accessToken");
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export const useAuthStore = defineStore("auth", {
  state: () => ({
    isSubmitting: false,
    token: sessionStorage.getItem("accessToken") || null,
    user: sessionStorage.getItem("userClaims")
      ? JSON.parse(sessionStorage.getItem("userClaims"))
      : null,
  }),
  getters: {
    isAuthenticated: (s) => !!s.token && !!s.user,
    nickname: (s) => s.user?.nickname ?? "",
  },
  actions: {
    async register(formData) {
      this.isSubmitting = true;
      try {
        const res = await api.post(`/v2/users/register`, formData, {
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

        const res = await api.post(
          `/v2/users/authentications`,
          { email, password },
          { validateStatus: () => true }
        );

        console.log("login status:", res.status, "data:", res.data);

        if (res.status !== 200 || !res.data?.accessToken) {
          console.log("login status:", res.status, "data:", res.data);
          throw new Error(
            res.status === 400 || res.status === 401
              ? "Email or Password is incorrect."
              : "Please try again later."
          );
        }

        const token = res.data.accessToken;
        const claims = decodeJwt(token);

        this.token = token;
        this.user = claims;
        sessionStorage.setItem("accessToken", token);
        sessionStorage.setItem("userClaims", JSON.stringify(claims));

        return claims;
      } finally {
        this.isSubmitting = false;
      }
    },

    logout() {
      this.token = null;
      this.user = null;
      sessionStorage.removeItem("accessToken");
      sessionStorage.removeItem("userClaims");
    },

    ensureNotExpired() {
      if (this.user?.exp && this.user.exp * 1000 < Date.now()) {
        this.logout();
      }
    },
  },
});
