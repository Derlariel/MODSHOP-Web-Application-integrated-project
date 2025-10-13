<script setup>
import { ref, computed, watch, onMounted } from "vue";
import BaseInput from "@/components/shared/BaseInput.vue";
import SuccessModal from "@/components/shared/modal/SuccessModal.vue";
import { useAuthStore } from "@/stores/useAuthStore";
import { useRouter, useRoute } from "vue-router";

const router = useRouter();
const route = useRoute();
const auth = useAuthStore();

const status = ref("");
const email = ref("");
const password = ref("");
const showSuccessModal = ref(false);
const alertMessage = ref("");
const errors = ref({ email: "", password: "", server: "" });

watch([email, password], () => {
  errors.value.server = "";
});

const canSubmit = computed(() => {
  const emailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value);
  const passwordNotEmpty = password.value && password.value.trim().length > 0;
  return !auth.isSubmitting && emailValid && passwordNotEmpty;
});

onMounted(() => {
  if (route.query.status === "password-changed")
    status.value = "Your password has been changed. Please log in again.";
  else if (route.query.status === "reset-success")
    status.value = "Password reset successfully. Please log in.";
});

async function handleSubmit() {
  errors.value = { email: "", password: "", server: "" };
  try {
    await auth.login({ email: email.value, password: password.value });
    sessionStorage.setItem("login-success", "true");

    // redirect ตาม role
    if (auth.user?.role === "SELLER") router.replace("/sale-items/list");
    else router.replace("/sale-items");
  } catch (err) {
    errors.value.server = err?.message || "Email or Password is incorrect.";
  }
}

function handleCancel() {
  email.value = "";
  password.value = "";
  errors.value = { email: "", password: "", server: "" };
  router.push("/sale-items");
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-black px-4">
    <SuccessModal :visible="showSuccessModal" :message="alertMessage" />
    
    <div class="max-w-md w-full space-y-5">
      <h2 class="text-3xl font-bold text-white text-center">Login</h2>

      <!-- Status Message -->
      <transition name="fade">
        <p v-if="status" class="text-green-400 text-center font-medium">
          {{ status }}
        </p>
      </transition>

      <!-- Email -->
      <BaseInput
        v-model="email"
        label="Email"
        type="email"
        required
        placeholder="you@example.com"
        cypress="email-input itbms-email"
        :error="errors.email"
        :maxInput="50"
      />

      <!-- Password -->
      <BaseInput
        v-model="password"
        label="Password"
        type="password"
        required
        placeholder="********"
        cypress="password-input itbms-password"
        :error="errors.password"
        :maxInput="14"
      />

      <!-- Error -->
      <p v-if="errors.server" class="text-sm text-red-500 text-center">
        {{ errors.server }}
      </p>

      <!-- Buttons -->
      <div class="flex space-x-3 pt-4">
        <BaseInput
          isButton
          buttonText="Login"
          type="button"
          variant="primary"
          cypress="login-submit itbms-signin-button"
          :disabled="!canSubmit"
          @click="handleSubmit"
        />
        <BaseInput
          isButton
          buttonText="Cancel"
          type="button"
          variant="secondary"
          cypress="login-cancel"
          @click="handleCancel"
        />
      </div>

      <!-- Divider -->
      <div class="relative my-6">
        <div class="absolute inset-0 flex items-center">
          <div class="w-full border-t border-neutral-700"></div>
        </div>
        <div class="relative flex justify-center text-xs uppercase">
          <span class="bg-black px-3 text-neutral-400">More options</span>
        </div>
      </div>

      <!-- Forgot + Signup -->
      <div class="flex flex-col space-y-3">
        <router-link
          to="/forgot-password"
          class="w-full text-center py-2 rounded-xl bg-neutral-800 hover:bg-neutral-700 text-white transition-all duration-200"
        >
          Forgot your password?
        </router-link>

        <div class="text-center text-sm text-gray-400">
          Don’t have an account?
          <router-link
            to="/register"
            class="text-blue-400 hover:text-blue-300 font-medium transition"
          >
            Sign up here
          </router-link>
        </div>
      </div>

      <p class="text-xs text-gray-500 text-center pt-4">
        By logging in, you agree to our
        <a href="#" class="underline hover:text-white transition">terms & conditions</a>.
      </p>
    </div>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.4s;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
