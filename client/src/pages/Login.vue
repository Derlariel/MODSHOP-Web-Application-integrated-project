<script setup>
import { ref, computed } from "vue";
import BaseInput from "@/components/shared/BaseInput.vue";
import { validateEmailPassword } from "@/utils/validate";
import { useAuthStore } from "@/stores/useAuthStore";
import router from "@/router";

const email = ref("");
const password = ref("");
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
  return (
    /\S+@\S+\.\S+/.test(email.value) &&
    email.value.length <= 50 &&
    password.value &&
    password.value.length <= 14
  );
});

const authStore = useAuthStore();

const handleSubmit = async () => {
  if (!validate()) return;

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
  router.back();
};
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-black px-4">
    <div class="max-w-md w-full space-y-8">
      <h2 class="text-3xl font-bold text-white text-center">Login</h2>

      <BaseInput
        v-model="email"
        label="Email"
        type="email"
        required
        placeholder="you@example.com"
        cypress="email-input"
        :error="errors.email"
        sanitize="email"
        :maxInput="50"
      />

      <BaseInput
        v-model="password"
        label="Password"
        type="password"
        required
        placeholder="********"
        cypress="password-input"
        :error="errors.password"
        :maxInput="14"
      />
      <p v-if="errors.server" class="text-sm text-red-500 text-center">
        {{ errors.server }}
      </p>

      <div class="flex space-x-3 pt-4">
        <BaseInput
          isButton
          buttonText="Sign In"
          type="button"
          variant="primary"
          cypress="login-submit"
          :disabled="!canSubmit || authStore.isSubmitting"
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

      <p class="text-xs text-gray-400 text-center">
        By logging in, you agree to our
        <a href="#" class="underline">terms & conditions</a>.
      </p>
    </div>
  </div>
</template>
