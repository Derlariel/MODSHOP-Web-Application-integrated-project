<script setup>
import { ref, computed, watch } from "vue";
import BaseInput from "@/components/shared/BaseInput.vue";
import { validateEmailPassword } from "@/utils/validate";
import { useAuthStore } from "@/stores/useAuthStore";
import { useRouter } from "vue-router";

const router = useRouter();
const auth = useAuthStore();

const email = ref("");
const password = ref("");
const errors = ref({ email: "", password: "", server: "" });

watch([email, password], () => {
  errors.value.server = "";
  errors.value.email = "";
  errors.value.password = "";
});

function normalized() {
  return {
    email: String(email.value || "")
      .trim()
      .toLowerCase(),
    password: String(password.value || ""),
  };
}

function validate() {
  const { email: e, password: p } = normalized();
  const { valid, errors: fe } = validateEmailPassword({
    email: e,
    password: p,
  });
  errors.value.email = fe.email || "";
  errors.value.password = fe.password || "";
  return valid;
}
const canSubmit = computed(() => {
  const emailTrim = String(email.value || "").trim().toLowerCase();
  const emailValid = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(emailTrim) && emailTrim.length <= 100;
  const pwdValid = typeof password.value === "string" && password.value.length > 0;
  return !auth.isSubmitting && emailValid && pwdValid;
});


async function handleSubmit() {
  errors.value = { email: "", password: "", server: "" };
  const { email: e, password: p } = normalized();
  const { valid, errors: fe } = validateEmailPassword({ email: e, password: p });
  if (!valid) {
    errors.value.email = fe.email || "";
    errors.value.password = fe.password || "";
    return;
  }

  try {
    await auth.login({ email: e, password: p });
    router.replace("/sale-items");
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
    <div class="max-w-md w-full space-y-4">
      <h2 class="text-3xl font-bold text-white text-center">Login</h2>

      <BaseInput
        v-model="email"
        label="Email"
        type="email"
        required
        placeholder="you@example.com"
        cypress="email-input"
        :error="errors.email"
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

      <!-- Sign up link -->
      <div class="text-center mt-6">
        <p class="text-sm text-gray-400">
          No account?
          <router-link
            to="/register"
            class="text-white hover:text-blue-400 underline font-medium"
          >
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
