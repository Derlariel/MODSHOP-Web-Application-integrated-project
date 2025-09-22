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
  
  // Check basic fields
  const basicChanged = (
    form.value.nickName !== original.value.nickName ||
    form.value.fullName !== original.value.fullName
  )
  
  // Check seller-specific fields if user is a seller
  if (form.value.userType === 'SELLER') {
    const sellerChanged = (
      form.value.phoneNumber !== original.value.phoneNumber ||
      form.value.bankName !== original.value.bankName ||
      form.value.bankAccount !== original.value.bankAccount
    )
    return basicChanged || sellerChanged
  }
  
  return basicChanged
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
  <div class="min-h-screen bg-gradient-to-br from-gray-900 to-black flex items-center justify-center pt-4">
    <div class="w-full h-full px-10 py-16">
      <!-- Navigation Path (outside the box) -->
      <nav class="flex items-center text-sm text-gray-400 mb-8">
        <router-link to="/sale-items" class="itbms-home hover:text-white transition-colors">Home</router-link>
        <span class="mx-2">/</span>
        <router-link to="/profile" class="itbms-profile hover:text-white transition-colors">Profile</router-link>
        <span class="mx-2">/</span>
        <span class="text-white font-medium">Edit Profile</span>
      </nav>

      <div class="w-full max-w-lg mx-auto bg-gray-800 rounded-2xl shadow-2xl border border-gray-700 p-10">
        <h1 class="text-3xl font-extrabold text-white mb-8 text-center">
          ✏️ Edit Profile
        </h1>

      <div v-if="form" class="space-y-6">
        <BaseInput
          label="Nickname"
          v-model="form.nickName"
          required
          placeholder="Enter your nickname"
          cypress="itbms-nickname"
          :error="!form.nickName ? 'This field is required' : ''"
        />
        <BaseInput
          label="Fullname"
          v-model="form.fullName"
          required
          placeholder="Enter your full name"
          cypress="itbms-fullname"
          :error="!form.fullName ? 'This field is required' : ''"
        />

        <div class="bg-gray-700 px-4 py-3 rounded-lg text-gray-200 text-sm border border-gray-600">
          <span class="font-medium text-gray-400">Email: </span> 
          <span class="itbms-email">{{ form.email }}</span>
        </div>

        <div class="bg-gray-700 px-4 py-3 rounded-lg text-gray-200 text-sm border border-gray-600">
          <span class="font-medium text-gray-400">User Type: </span> 
          <span class="itbms-type">{{ form.userType }}</span>
        </div>

        <!-- Seller-specific fields -->
        <div v-if="form.userType === 'SELLER'" class="space-y-6 pt-4 border-t border-gray-600">
          <h3 class="text-lg font-semibold text-gray-200 mb-4">Seller Information</h3>
          
          <BaseInput
            label="Mobile Number"
            v-model="form.phoneNumber"
            placeholder="Enter your mobile number"
            cypress="itbms-mobile"
          />
          <BaseInput
            label="Bank Name"
            v-model="form.bankName"
            placeholder="Enter your bank name"
            cypress="itbms-bankName"
          />
          <BaseInput
            label="Bank Account"
            v-model="form.bankAccount"
            placeholder="Enter your bank account number"
            cypress="itbms-bankAccount"
          />
        </div>

        <div class="flex items-center justify-center gap-4 pt-6">
          <button
            :disabled="!changed"
            @click="save"
            class="itbms-save-button px-6 py-3 rounded-xl bg-gradient-to-r from-green-500 to-emerald-600 text-white font-semibold shadow hover:shadow-lg hover:from-green-600 hover:to-emerald-700 transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            💾 Save
          </button>
          <button
            @click="cancel"
            class="itbms-cancel-button px-6 py-3 rounded-xl bg-gray-600 text-gray-100 font-medium shadow hover:bg-gray-500 hover:shadow-md transition-all"
          >
            ❌ Cancel
          </button>
        </div>
      </div>
    </div>
    </div>
    <!-- Modals -->
    <SuccessModal :visible="showSuccess" message="Profile updated successfully." @close="showSuccess=false"/>
    <ErrorModal :visible="showError" :message="errorMessage" @close="showError=false"/>
  </div>
</template>
