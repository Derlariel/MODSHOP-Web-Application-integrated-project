import { defineStore } from "pinia"
import { validateEmailPassword } from "@/utils/validate"
import { request } from "../middleware/interception.js"

const BASE_URL = import.meta.env.VITE_BASE_URL


export const useAuthStore = defineStore("auth", {
  state: () => ({
    isSubmitting: false,
    _syncInitialized: false,
    user: (() => {
      try {
        
        const userClaims = localStorage.getItem("userClaims")
        const parsedUser = userClaims ? JSON.parse(userClaims) : null
        return parsedUser
      } catch (e) {
        console.error("Error parsing user claims from sessionStorage:", e)
        localStorage.removeItem("userClaims")
        return null
      }
    })(),
  }),
  getters: {
    isAuthenticated: (s) => !!(s.user && s.user.id),
    nickname: (s) => (s.user && s.user.nickname) || "",
  },
  actions: {
    initPersistence() {
      if (this._syncInitialized) return
      this._syncInitialized = true
      window.addEventListener('storage', (e) => {
        if (e.key === 'userClaims') {
          try {
            if (e.newValue) {
              const parsed = JSON.parse(e.newValue)
              this.user = parsed
            } else {
              this.user = null
            }
          } catch (err) {
            console.error('Failed to sync userClaims from storage:', err)
          }
        }
      })
    },
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
          nickname: data.nickName || data.nickname || "",
          id: data.user_id || data.id,
          email: data.email,
          role: data.role,
          fullName: data.fullName || data.fullname || data.full_name || "",
        }

        localStorage.setItem("userClaims", JSON.stringify(this.user))

        try { await this.refreshUserData() } catch {}

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
        localStorage.removeItem("userClaims")
        sessionStorage.setItem("logout-success", "true")
      }
    },

    ensureNotExpired() {
      if (!this.user) {
        this.user = null
        localStorage.removeItem("userClaims")
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
            fullName: data.fullName || data.fullname || data.full_name || this.user.fullName,
          }
          localStorage.setItem("userClaims", JSON.stringify(this.user))

          return true
        } else {
          console.error("Failed to refresh user data:", res.status)
          // If server says not authorized/invalid, clear local persisted state for security
          this.user = null
          localStorage.removeItem("userClaims")
          return false
        }
      } catch (error) {
        console.error("Error refreshing user data:", error)
        // network or other errors shouldn't keep stale identity around
        this.user = null
        localStorage.removeItem("userClaims")
        return false
      }
    },
  },
})
