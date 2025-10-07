<script setup>
import { ref, computed, watch, onMounted } from "vue";
import BaseInput from "@/components/shared/BaseInput.vue";
import SuccessModal from "@/components/shared/modal/SuccessModal.vue";
import { validateEmailPassword } from "@/utils/validate";
import { useAuthStore } from "@/stores/useAuthStore";
import { useRouter } from "vue-router";

const router = useRouter();
const auth = useAuthStore();

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
  
  return !auth.isSubmitting && (emailValid || passwordNotEmpty);
});

onMounted(() => {
  // No logout success handling here; it now shows on /sale-items
});

async function handleSubmit() {
  errors.value = { email: "", password: "", server: "" };

  try {
    await auth.login({ 
      email: email.value, 
      password: password.value 
    });
    
    // Set login success flag for showing success modal
    sessionStorage.setItem("login-success", "true");
    
    // Redirect based on user role
    if (auth.user && auth.user.role === "SELLER") {
      router.replace("/sale-items/list");
    } else {
      router.replace("/sale-items");
    }
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
    
    <div class="max-w-md w-full space-y-4">
      <h2 class="text-3xl font-bold text-white text-center">Login</h2>

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
      <p v-if="errors.server" class="text-sm text-red-500 text-center">
        {{ errors.server }}
      </p>

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
