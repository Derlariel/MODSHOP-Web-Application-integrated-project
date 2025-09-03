import { defineStore } from "pinia";
import axios from "axios";
import { validateEmailPassword } from "@/utils/validate";
import { decodeJwt } from "@/utils/jwt";

const BASE_URL = import.meta.env.VITE_BASE_URL;

export const api = axios.create({ baseURL: BASE_URL });

api.interceptors.request.use((config) => {
  const url = config.url || "";
  const skipAuth =
    url.endsWith("/v2/users/authentications") ||
    url.endsWith("/v2/users/register");

  if (!skipAuth) {
    const token = sessionStorage.getItem("accessToken");
    if (token) config.headers.Authorization = `Bearer ${token}`;
  }
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

        const token = res.data?.accessToken || res.data?.access_token || null;
        const refresh =
          res.data?.refreshToken || res.data?.refresh_token || null;

        if (res.status !== 200 || !token) {
          if (res.status === 403) {
            throw new Error("You need to activate your account before signing in.");
          }
          throw new Error(
            res.status === 400 || res.status === 401
              ? "Email or Password is incorrect."
              : "Authentication failed."
          );
        }

        // decode เฉพาะ access token (หรือจะใช้ข้อมูล nickname/id จาก body ก็ได้)
        const claims = decodeJwt(token);

        this.token = token;
        this.user = {
          ...claims,
          nickname: claims.nickname ?? res.data.nickname ?? "",
          id: claims.id ?? res.data.userId ?? res.data.id,
          email: claims.email ?? res.data.email,
          role: claims.role ?? res.data.role,
        };

        sessionStorage.setItem("accessToken", token);
        sessionStorage.setItem("userClaims", JSON.stringify(this.user));

        return this.user;
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
