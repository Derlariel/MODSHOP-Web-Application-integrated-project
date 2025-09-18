<script setup>
import { onMounted, ref, computed } from "vue"
import { useAuthStore } from "@/stores/useAuthStore"
import { useRouter } from "vue-router"
import BaseInput from "@/components/shared/BaseInput.vue"
import SuccessModal from "@/components/shared/modal/SuccessModal.vue"
import ErrorModal from "@/components/shared/modal/ErrorModal.vue"

const auth = useAuthStore()
const router = useRouter()

const form = ref(null)
const original = ref(null)
const showSuccess = ref(false)
const showError = ref(false)
const errorMessage = ref("")

onMounted(async () => {
  try {
    const res = await fetch(
      `${import.meta.env.VITE_BASE_URL}/v2/users/${auth.user.id}`,
      {
        headers: {
          Authorization: `Bearer ${sessionStorage.getItem("accessToken")}`,
          "Content-Type": "application/json",
        },
      }
    )
    const data = await res.json()
    form.value = { ...data }
    original.value = { ...data }
  } catch (err) {
    showError.value = true
    errorMessage.value = "Failed to load profile data."
  }
})

const changed = computed(() => {
  if (!form.value || !original.value) return false
  return (
    form.value.nickName !== original.value.nickName ||
    form.value.fullName !== original.value.fullName
  )
})

async function save() {
  try {
    const res = await fetch(
      `${import.meta.env.VITE_BASE_URL}/v2/users/${auth.user.id}`,
      {
        method: "PUT",
        headers: {
          Authorization: `Bearer ${sessionStorage.getItem("accessToken")}`,
          "Content-Type": "application/json",
        },
        body: JSON.stringify(form.value),
      }
    )

    if (res.ok) {
      showSuccess.value = true
      setTimeout(() => router.push("/profile"), 1500)
    } else {
      throw new Error("Update failed")
    }
  } catch (err) {
    showError.value = true
    errorMessage.value = "Failed to update profile."
  }
}

function cancel() {
  router.push("/profile")
}
</script>

<template>
  <div class="min-h-screen bg-gradient-to-br from-gray-900 to-black flex items-center justify-center p-6">
    <div class="w-full max-w-lg bg-gray-800 rounded-2xl shadow-2xl border border-gray-700 p-10">
      <h1 class="text-3xl font-extrabold text-white mb-8 text-center">
        ✏️ Edit Profile
      </h1>

      <div v-if="form" class="space-y-6">
        <BaseInput
          label="Nickname"
          v-model="form.nickName"
          required
          placeholder="Enter your nickname"
          :error="!form.nickName ? 'This field is required' : ''"
        />
        <BaseInput
          label="Fullname"
          v-model="form.fullName"
          required
          placeholder="Enter your full name"
          :error="!form.fullName ? 'This field is required' : ''"
        />

        <div class="bg-gray-700 px-4 py-3 rounded-lg text-gray-200 text-sm border border-gray-600">
          <span class="font-medium text-gray-400">Email:</span> {{ form.email }}
        </div>

        <div class="flex items-center justify-center gap-4 pt-6">
          <button
            :disabled="!changed"
            @click="save"
            class="px-6 py-3 rounded-xl bg-gradient-to-r from-green-500 to-emerald-600 text-white font-semibold shadow hover:shadow-lg hover:from-green-600 hover:to-emerald-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            💾 Save
          </button>
          <button
            @click="cancel"
            class="px-6 py-3 rounded-xl bg-gray-600 text-gray-100 font-medium shadow hover:bg-gray-500 hover:shadow-md transition-all"
          >
            ❌ Cancel
          </button>
        </div>
      </div>
    </div>
    <!-- Modals -->
    <SuccessModal :visible="showSuccess" message="Profile updated successfully." @close="showSuccess=false"/>
    <ErrorModal :visible="showError" :message="errorMessage" @close="showError=false"/>
  </div>
</template>
