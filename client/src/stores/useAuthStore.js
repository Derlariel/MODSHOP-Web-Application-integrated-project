import { defineStore } from "pinia"
import { validateEmailPassword } from "@/utils/validate"

const BASE_URL = import.meta.env.VITE_BASE_URL

async function request(path, options = {}, skipAuth = false) {
  const headers = options.headers ? { ...options.headers } : {}

  
  const requestOptions = {
    ...options,
    headers,
    credentials: 'include', 
  }

  const res = await fetch(`${BASE_URL}${path}`, requestOptions)

  let data = {}
  try {
    data = await res.json()
  } catch (e) {}

  return { res, data }
}

export const useAuthStore = defineStore("auth", {
  state: () => ({
    isSubmitting: false,
    user: (() => {
      try {
        const userClaims = sessionStorage.getItem("userClaims")
        const parsedUser = userClaims ? JSON.parse(userClaims) : null
        console.log("Loading user from sessionStorage:", parsedUser)
        return parsedUser
      } catch (e) {
        console.error("Error parsing user claims from sessionStorage:", e)
        sessionStorage.removeItem("userClaims")
        return null
      }
    })(),
  }),
  getters: {
    isAuthenticated: (s) => !!(s.user && s.user.id),
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

        if (res.status !== 200) {
          if (res.status === 403) {
            throw new Error("You need to activate your account before signing in.")
          }
          throw new Error(
            res.status === 400 || res.status === 401
              ? "Email or Password is incorrect."
              : "Authentication failed."
          )
        }

        
        this.user = {
          nickname: data.nickname || "",
          id: data.user_id || data.id,
          email: data.email,
          role: data.role,
        }

        

        sessionStorage.setItem("userClaims", JSON.stringify(this.user))

        return this.user
      } finally {
        this.isSubmitting = false
      }
    },

    async logout() {
      try {
        await request("/v2/auth/logout", {
          method: "POST",
        })
      } catch (error) {
        console.error("Logout failed:", error)
      } finally {
        this.user = null
        sessionStorage.removeItem("userClaims")
        sessionStorage.setItem("logout-success", "true")
      }
    },

    ensureNotExpired() {
      if (!this.user) {
        this.user = null
        sessionStorage.removeItem("userClaims")
      }
    },

    async refreshUserData() {
      if (!this.user || !this.user.id) {
      
        return false
      }
      
      try {
        const { res, data } = await request(`/v2/users/${this.user.id}`)
        
        if (res.ok) {
          
          this.user = {
            ...this.user,
            nickname: data.nickName || data.nickname || this.user.nickname,
            email: data.email || this.user.email,
            role: data.userType || data.role || this.user.role,
          }
          sessionStorage.setItem("userClaims", JSON.stringify(this.user))

          return true
        } else {
          console.error("Failed to refresh user data:", res.status)
          return false
        }
      } catch (error) {
        console.error("Error refreshing user data:", error)
        return false
      }
    },
  },
})
