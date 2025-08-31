<script setup>
import { ref, computed, watch } from "vue";
import BaseInput from "@/components/shared/BaseInput.vue";
import { validateEmailPassword } from "@/utils/validate";
import { useAuthStore } from "@/stores/useAuthStore";
import router from "@/router";

const email = ref("");
const password = ref("");

// Watch for changes and trim excess characters
watch(email, (newValue) => {
  if (newValue.length > 50) {
    email.value = newValue.substring(0, 50);
  }
});

watch(password, (newValue) => {
  if (newValue.length > 14) {
    password.value = newValue.substring(0, 14);
  }
});

const errors = ref({
  email: "",
  password: "",
  server: "",
});

const validate = () => {
  const { valid, errors: validationErrors } = validateEmailPassword({
    email: email.value,
    password: password.value,
  });
  errors.value.email = validationErrors.email;
  errors.value.password = validationErrors.password;
  return valid;
};

const canSubmit = computed(() => {
  const emailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value);
  const passwordNotEmpty = password.value && password.value.trim().length > 0;

  return emailValid || passwordNotEmpty;
});

const authStore = useAuthStore();

const handleSubmit = async () => {
  // Clear previous server errors
  errors.value.server = "";
  errors.value.email = "";
  errors.value.password = "";

  try {
    await authStore.login({
      email: email.value,
      password: password.value,
    });
    console.log("Login successful");
    router.push("/sale-items");
  } catch (err) {
    errors.value.server = err.message || "Login failed. Please try again.";
  }
};

const handleCancel = () => {
  email.value = "";
  password.value = "";
  errors.value = { email: "", password: "", server: "" };
  router.push("/sale-items");
};
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-black px-4">
    <div class="max-w-md w-full space-y-4">
      <h2 class="text-3xl font-bold text-white text-center">Login</h2>

      <BaseInput v-model="email" label="Email" type="email" required placeholder="you@example.com" cypress="email-input"
        :error="errors.email" :maxInput="50" />

      <BaseInput v-model="password" label="Password" type="password" required placeholder="********"
        cypress="password-input" :error="errors.password" :maxInput="14" />


      <div class="flex space-x-3 py-2">
        <BaseInput isButton buttonText="Login" type="button" variant="primary" cypress="login-submit"
          :disabled="!canSubmit || authStore.isSubmitting" @click="handleSubmit" />

        <BaseInput isButton buttonText="Cancel" type="button" variant="secondary" cypress="login-cancel"
          @click="handleCancel" />
      </div>
      <p v-if="errors.server" class="text-sm text-red-500 text-center">
        {{ errors.server }}
      </p>


      <!-- Sign up link -->
      <div class="text-center mt-6">
        <p class="text-sm text-gray-400">
          No account?
          <router-link to="/register" class="text-white hover:text-blue-400 underline font-medium">
            Sign up
          </router-link>
        </p>
      </div>
      <p class="text-xs text-gray-400 text-center">
        By logging in, you agree to our
        <a href="#" class="underline">terms & conditions</a>.
      </p>
    </div>
  </div>
</template>
