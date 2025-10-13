<script setup>
import { ref, computed } from "vue"
import { useRouter } from "vue-router"
import BaseInput from "@/components/shared/BaseInput.vue"
import SuccessModal from "@/components/shared/modal/SuccessModal.vue"
import { useAuthStore } from "@/stores/useAuthStore"

const router = useRouter()
const auth = useAuthStore()

// เช็คโหมด
const mode = computed(() => (auth.isAuthenticated ? "change" : "forgot"))

const email = ref("")
const oldPassword = ref("")
const newPassword = ref("")
const confirmPassword = ref("")
const message = ref("")
const showSuccess = ref(false)
const errors = ref({ server: "" })

const canSubmit = computed(() => {
  if (mode.value === "forgot")
    return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.value)
  if (mode.value === "change")
    return (
      oldPassword.value.trim().length >= 6 &&
      newPassword.value.trim().length >= 6 &&
      newPassword.value === confirmPassword.value
    )
  return false
})

async function handleSubmit() {
  errors.value.server = ""
  if (mode.value === "forgot") {
    const result = await auth.forgotPasswordReset(email.value)
    message.value = result.message
    if (result.success) showSuccess.value = true
    else errors.value.server = result.message
  } else {
    const result = await auth.updatePasswordInProfile(
      oldPassword.value,
      newPassword.value
    )
    message.value = result.message
    if (result.success){
        showSuccess.value = true
        setTimeout(async () => {
            await auth.logout();
            router.replace({name: 'Login', query: { status: "password-changed" }});
        },2000)
    }
    else errors.value.server = result.message
  }
}

function handleCancel() {
  if (mode.value === "forgot") router.push("/login")
  else router.push("/profile")
}
</script>

<template>
  <div class="min-h-screen flex items-center justify-center bg-black px-4">
    <SuccessModal :visible="showSuccess" :message="message" />
    <div class="max-w-md w-full space-y-4">
      <h2 class="text-3xl font-bold text-white text-center">
        {{ mode === "forgot" ? "Forgot Password" : "Change Password" }}
      </h2>

      <BaseInput
        v-if="mode === 'forgot'"
        v-model="email"
        label="Email"
        type="email"
        required
        placeholder="you@example.com"
      />

      <template v-else>
        <BaseInput
          v-model="oldPassword"
          label="Old Password"
          type="password"
          required
          placeholder="********"
        />
        <BaseInput
          v-model="newPassword"
          label="New Password"
          type="password"
          required
          placeholder="********"
        />
        <BaseInput
          v-model="confirmPassword"
          label="Confirm Password"
          type="password"
          required
          placeholder="********"
        />
        
        <div>
            <router-link
          to="/forgot-password"
          class="py-2 text-sm flex justify-end  text-gray-500 hover:text-blue-500 transition-all duration-200"
        >
          Forgot your password?
        </router-link>
        </div>
      </template>

      <p v-if="errors.server" class="text-sm text-red-500 text-center mt-2">
        {{ errors.server }}
      </p>

      <div class="flex space-x-3 pt-4">
        <BaseInput
          isButton
          :buttonText="mode === 'forgot' ? 'Send Reset Link' : 'Change Password'"
          variant="primary"
          type="button"
          :disabled="!canSubmit || auth.isSubmitting"
          @click="handleSubmit"
        />
        <BaseInput
          isButton
          buttonText="Cancel"
          type="button"
          variant="secondary"
          @click="handleCancel"
        />
      </div>

      <div v-if="mode === 'forgot'" class="text-center mt-6">
        <p class="text-sm text-gray-400">
          Remembered your password?
          <router-link
            to="/login"
            class="text-white hover:text-blue-400 underline font-medium"
          >
            Go back to Login
          </router-link>
        </p>
      </div>
    </div>
  </div>
</template>
