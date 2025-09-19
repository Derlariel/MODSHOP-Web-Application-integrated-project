import { defineStore } from "pinia"
import { validateEmailPassword } from "@/utils/validate"
import { decodeJwt } from "@/utils/jwt"

const BASE_URL = import.meta.env.VITE_BASE_URL

async function request(path, options = {}, skipAuth = false) {
  const headers = options.headers ? { ...options.headers } : {}

  if (!skipAuth) {
    const token = sessionStorage.getItem("accessToken")
    if (token) headers.Authorization = `Bearer ${token}`
  }

  const res = await fetch(`${BASE_URL}${path}`, {
    ...options,
    headers,
  })

  let data = {}
  try {
    data = await res.json()
  } catch (e) {}

  return { res, data }
}

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
    nickname: (s) => (s.user && s.user.nickname) || "",
  },
  actions: {
    async register(formData) {
      this.isSubmitting = true
      try {
        const { res, data } = await request("/v2/auth/register", {
          method: "POST",
          body: formData,
        }, true)

        if (res.status !== 201) {
          const msg = data?.message || data?.error || "Registration failed."
          throw new Error(msg)
        }

        return data
      } finally {
        this.isSubmitting = false
      }
    },

    async login({ email, password }) {
      this.isSubmitting = true
      try {
        const { valid } = validateEmailPassword({ email, password })
        if (!valid) throw new Error("Email or Password is incorrect.")

        const { res, data } = await request("/v2/auth/login", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, password }),
        }, true)

        console.log("login status:", res.status, "data:", data)

        const token = data?.accessToken || data?.access_token || null

        if (res.status !== 200 || !token) {
          if (res.status === 403) {
            throw new Error("You need to activate your account before signing in.")
          }
          throw new Error(
            res.status === 400 || res.status === 401
              ? "Email or Password is incorrect."
              : "Authentication failed."
          )
        }

        const claims = decodeJwt(token)

        this.token = token
        this.user = {
          ...claims,
          nickname: claims.nickname ?? data.nickname ?? "",
          id: claims.id ?? data.userId ?? data.id,
          email: claims.email ?? data.email,
          role: claims.role ?? data.role,
        }

        sessionStorage.setItem("accessToken", token)
        sessionStorage.setItem("userClaims", JSON.stringify(this.user))

        return this.user
      } finally {
        this.isSubmitting = false
      }
    },

    logout() {
      this.token = null
      this.user = null
      sessionStorage.removeItem("accessToken")
      sessionStorage.removeItem("userClaims")
    },

    ensureNotExpired() {
      if (this.user?.exp && this.user.exp * 1000 < Date.now()) {
        this.logout()
      }
    },
  },
})
