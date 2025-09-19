<script setup>
import { ref, onMounted } from "vue"
import { useAuthStore } from "@/stores/useAuthStore"

const BASE_URL = import.meta.env.VITE_BASE_URL
const auth = useAuthStore()
const profile = ref(null)

const maskNumber = (num) => {
  if (!num) return ""
  const str = String(num)
  if (str.length < 4) return "x".repeat(str.length)

  const arr = str.split("")
  return arr
    .map((ch, i) => {
      const revIndex = str.length - 1 - i
      return revIndex === 1 || revIndex === 2 || revIndex === 3 ? ch : "x"
    })
    .join("")
}

onMounted(async () => {
  try {
    const res = await fetch(`${BASE_URL}/v2/users/${auth.user.id}`, {
      headers: {
        Authorization: `Bearer ${sessionStorage.getItem("accessToken")}`,
        "Content-Type": "application/json",
      },
    })
    if (!res.ok) throw new Error("Failed to fetch profile")
    profile.value = await res.json()
  } catch (err) {
    console.error(err)
  }
})
</script>

<template>
  <div class="min-h-screen w-full bg-gradient-to-br from-black to-gray-900 text-gray-100 pt-16">
    <div class="w-full h-full px-10 py-16">
      <h1 class="text-5xl font-extrabold text-center mb-16 tracking-tight">
        My Profile
      </h1>

      <div v-if="profile" class="grid grid-cols-1 lg:grid-cols-2 gap-10">
        <!-- LEFT COLUMN -->
        <div class="bg-gray-800/80 backdrop-blur-md rounded-2xl shadow-xl p-10 border border-gray-700 hover:shadow-2xl transition">
          <h2 class="text-2xl font-bold mb-6 text-gray-200">General Info</h2>
          <div class="space-y-6">
            <div>
              <p class="text-sm text-gray-400">Nickname</p>
              <p class="text-xl font-semibold">{{ profile.nickName }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-400">Fullname</p>
              <p class="text-xl font-semibold">{{ profile.fullName }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-400">Email</p>
              <p class="text-xl font-semibold">{{ profile.email }}</p>
            </div>
          </div>
        </div>

        <!-- RIGHT COLUMN -->
        <div
          v-if="profile.userType === 'SELLER'"
          class="bg-gray-800/80 backdrop-blur-md rounded-2xl shadow-xl p-10 border border-gray-700 hover:shadow-2xl transition"
        >
          <h2 class="text-2xl font-bold mb-6 text-gray-200">Seller Information</h2>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-8">
            <div>
              <p class="text-sm text-gray-400">Mobile</p>
              <p class="text-lg font-semibold">{{ maskNumber(profile.phoneNumber) }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-400">Bank Name</p>
              <p class="text-lg font-semibold">{{ profile.bankName }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-400">Bank Account</p>
              <p class="text-lg font-semibold">{{ maskNumber(profile.bankAccount) }}</p>
            </div>
          </div>
        </div>
      </div>

      <div v-else class="text-center text-gray-500 text-lg mt-20">
        Loading profile...
      </div>

      <div class="mt-16 text-center">
        <router-link
          to="/profile/edit"
          class="inline-block px-10 py-4 rounded-xl bg-gradient-to-r from-blue-600 to-indigo-700 text-white font-semibold text-lg shadow hover:shadow-2xl hover:scale-105 transition-all"
        >
          Edit Profile
        </router-link>
      </div>
    </div>
  </div>
</template>

