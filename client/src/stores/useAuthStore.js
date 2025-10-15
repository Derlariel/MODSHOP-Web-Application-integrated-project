import { defineStore } from "pinia";
import { validateEmailPassword } from "@/utils/validate";
import { request } from "@/middleware/interception";

export const useAuthStore = defineStore("auth", {
  state: () => ({
    isSubmitting: false,
    _syncInitialized: false,
    user: (() => {
      try {
        const userClaims = localStorage.getItem("userClaims");
        return userClaims ? JSON.parse(userClaims) : null;
      } catch (e) {
        console.error("Error parsing user claims:", e);
        localStorage.removeItem("userClaims");
        return null;
      }
    })(),
  }),
  getters: {
    isAuthenticated: (s) => !!(s.user && s.user.id),
    nickname: (s) => (s.user && s.user.nickname) || "",
  },
  actions: {
    initPersistence() {
      if (this._syncInitialized) return;
      this._syncInitialized = true;
      window.addEventListener("storage", (e) => {
        if (e.key === "userClaims") {
          try {
            this.user = e.newValue ? JSON.parse(e.newValue) : null;
          } catch (err) {
            console.error("Failed to sync userClaims:", err);
          }
        }
      });
    },

    async register(formData) {
      this.isSubmitting = true;
      try {
        const { res, data } = await request("/v2/auth/register", {
          method: "POST",
          body: formData,
        }, true);

        if (res.status !== 201) {
          throw new Error(data?.message || "Registration failed.");
        }
        return data;
      } finally {
        this.isSubmitting = false;
      }
    },

    async login({ email, password }) {
      this.isSubmitting = true;
      try {
        const { valid } = validateEmailPassword({ email, password });
        if (!valid) throw new Error("Email or Password is incorrect.");

        const { res, data } = await request("/v2/auth/login", {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ email, password }),
        }, true);

        if (res.status !== 200) {
          if (res.status === 403) throw new Error("Account not activated.");
          throw new Error("Authentication failed.");
        }

        this.user = {
          nickname: data.nickName || data.nickname || "",
          id: data.user_id || data.id,
          email: data.email,
          role: data.role,
          fullName: data.fullName || data.fullname || data.full_name || "",
        };
        localStorage.setItem("userClaims", JSON.stringify(this.user));

        await this.refreshUserData();
        return this.user;
      } finally {
        this.isSubmitting = false;
      }
    },

    async logout() {
      try {
        await request("/v2/auth/logout", { method: "POST" }, true);
      } catch (error) {
        console.error("Logout failed:", error);
      } finally {
        this.user = null;
        localStorage.removeItem("userClaims");
        sessionStorage.setItem("logout-success", "true");
      }
    },

    async refreshUserData() {
      if (!this.user || !this.user.id) return false;

      try {
        const { res, data } = await request(`/v2/users/${this.user.id}`);
        if (res.ok) {
          this.user = {
            ...this.user,
            nickname: data.nickName || data.nickname || this.user.nickname,
            email: data.email || this.user.email,
            role: data.userType || data.role || this.user.role,
            fullName: data.fullName || data.fullname || data.full_name || this.user.fullName,
          };
          localStorage.setItem("userClaims", JSON.stringify(this.user));
          return true;
        } else {
          this.user = null;
          localStorage.removeItem("userClaims");
          return false;
        }
      } catch (err) {
        console.error("Error refreshing user data:", err);
        this.user = null;
        localStorage.removeItem("userClaims");
        return false;
      }
    },

    async forgotPasswordReset(email) {
      this.isSubmitting = true;
      try {
        const { res, data } = await request(`/v2/auth/forgot-password?email=${encodeURIComponent(email)}`, {
          method: "POST",
        }, true);
        if (!res.ok) throw new Error(data?.message || "Failed to request password reset.");
        return { success: true, message: data?.message || "Password reset email sent." };
      } catch (error) {
        console.error("Password reset request failed:", error);
        return { success: false, message: error.message || "Failed to request password reset." };
      } finally {
        this.isSubmitting = false;
      }
    },

    async updatePasswordInProfile(oldPassword, newPassword) {
      this.isSubmitting = true;
      try {
        const { res, data } = await request("/v2/auth/change-password", {
          method: "PUT",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({ oldPassword, newPassword }),
        });
        if (!res.ok) throw new Error(data?.message || "Failed to change password.");
        return { success: true, message: data?.message || "Password updated successfully." };
      } catch (error) {
        console.error("Change password failed:", error);
        return { success: false, message: error.message || "Failed to change password." };
      } finally {
        this.isSubmitting = false;
      }
    },
  },
});
