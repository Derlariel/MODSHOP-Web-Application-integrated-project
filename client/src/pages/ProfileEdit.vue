<script setup>
import { onMounted, ref, computed } from "vue";
import { useAuthStore } from "@/stores/useAuthStore";
import { useRouter } from "vue-router";
import BaseInput from "@/components/shared/BaseInput.vue";
import SuccessModal from "@/components/shared/modal/SuccessModal.vue";
import ErrorModal from "@/components/shared/modal/ErrorModal.vue";

const auth = useAuthStore();
const router = useRouter();

const form = ref(null);
const original = ref(null);
const showSuccess = ref(false);
const showError = ref(false);
const errorMessage = ref("");

onMounted(async () => {
  try {
    if (!auth.user || !auth.user.id) {
      console.error("User not authenticated or missing ID");
      const refreshed = await auth.refreshUserData();
      if (!refreshed) {
        showError.value = true;
        errorMessage.value = "User authentication required.";
        return;
      }
    }

    console.log("Fetching profile for user ID:", auth.user.id);

    const res = await fetch(
      `${import.meta.env.VITE_BASE_URL}/v2/users/${auth.user.id}`,
      {
        credentials: "include",
        headers: {
          "Content-Type": "application/json",
        },
      }
    );

    if (!res.ok) {
      if (res.status === 401) {
        console.error("Unauthorized - might need to login again");
      }
      throw new Error(`Failed to fetch profile: ${res.status}`);
    }

    const data = await res.json();
    form.value = { ...data };
    original.value = { ...data };

    console.log("Profile data:", data);
  } catch (err) {
    console.error("Error loading profile:", err);
    showError.value = true;
    errorMessage.value = "Failed to load profile data.";
  }
});

const changed = computed(() => {
  if (!form.value || !original.value) return false;

  const basicChanged =
    form.value.nickName !== original.value.nickName ||
    form.value.fullName !== original.value.fullName;

  if (form.value.userType === "SELLER") {
    const sellerChanged =
      form.value.phoneNumber !== original.value.phoneNumber ||
      form.value.bankName !== original.value.bankName ||
      form.value.bankAccount !== original.value.bankAccount;
    return basicChanged || sellerChanged;
  }

  return basicChanged;
});

async function save() {
  try {
    if (form.value) {
      if (typeof form.value.nickName === "string") {
        form.value.nickName = form.value.nickName.trim().replace(/\s+/g, " ");
      }
      if (typeof form.value.fullName === "string") {
        form.value.fullName = form.value.fullName.trim().replace(/\s+/g, " ");
      }
    }

    const updatePayload = {
      nickName: form.value.nickName,
      fullName: form.value.fullName,
    };

    if (form.value.userType === "SELLER") {
      updatePayload.phoneNumber =
        form.value.phoneNumber || form.value.mobileNumber;
      updatePayload.bankName = form.value.bankName;
      updatePayload.bankAccount =
        form.value.bankAccount || form.value.bankAccountNumber;
    }

    const res = await fetch(
      `${import.meta.env.VITE_BASE_URL}/v2/users/${auth.user.id}`,
      {
        method: "PUT",
        credentials: "include", // Include HttpOnly cookies
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(updatePayload),
      }
    );

    if (res.ok) {
      showSuccess.value = true;
      setTimeout(() => router.push("/profile"), 1500);
    } else {
      const errorData = await res.text();
      console.error("Update failed:", res.status, errorData);
      throw new Error(`Update failed: ${res.status}`);
    }
  } catch (err) {
    console.error("Error updating profile:", err);
    showError.value = true;
    errorMessage.value = "Failed to update profile.";
  }
}

function cancel() {
  router.push("/profile");
}
</script>

<template>
  <div
    class="min-h-screen bg-gradient-to-br from-gray-900 to-black flex items-center justify-center pt-4"
  >
    <div class="w-full h-full px-10 py-16">
      <!-- Navigation Path (outside the box) -->
      <nav class="flex items-center text-sm text-gray-400 mb-8">
        <router-link
          to="/sale-items"
          class="itbms-home hover:text-white transition-colors"
          >Home</router-link
        >
        <span class="mx-2">/</span>
        <router-link
          to="/profile"
          class="itbms-profile hover:text-white transition-colors"
          >Profile</router-link
        >
        <span class="mx-2">/</span>
        <span class="text-white font-medium">Edit Profile</span>
      </nav>

      <div
        class="w-full max-w-lg mx-auto bg-gray-800 rounded-2xl shadow-2xl border border-gray-700 p-10"
      >
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

          <div
            class="bg-gray-700 px-4 py-3 rounded-lg text-gray-200 text-sm border border-gray-600"
          >
            <span class="font-medium text-gray-400">Email: </span>
            <span class="itbms-email">{{ form.email }}</span>
          </div>

          <div
            class="bg-gray-700 px-4 py-3 rounded-lg text-gray-200 text-sm border border-gray-600"
          >
            <span class="font-medium text-gray-400">Type: </span>
            <span class="itbms-type">{{ form.userType }}</span>
          </div>

          <div
            class="bg-gray-700 px-4 py-3 rounded-lg text-gray-200 text-sm border border-gray-600 flex justify-between items-center"
          >
            <span class="font-medium text-gray-400">Security: </span>
            <router-link
              to="/change-password"
              class="text-red-400 font-medium hover:text-red-300 hover:underline underline-offset-4 transition-all duration-200 cursor-pointer flex items-center gap-1"
            >
              <svg
                xmlns="http://www.w3.org/2000/svg"
                class="w-4 h-4 text-red-400 group-hover:text-red-300 transition-colors duration-200"
                fill="none"
                viewBox="0 0 24 24"
                stroke="currentColor"
                stroke-width="2"
              >
                <path
                  stroke-linecap="round"
                  stroke-linejoin="round"
                  d="M12 11c.828 0 1.5-.672 1.5-1.5S12.828 8 12 8s-1.5.672-1.5 1.5S11.172 11 12 11zm0 3v4m-9 0h18a2 2 0 002-2v-9a2 2 0 00-2-2h-2V5a5 5 0 00-10 0v2H5a2 2 0 00-2 2v9a2 2 0 002 2z"
                />
              </svg>
              Change password
            </router-link>
          </div>

          <!-- Seller-specific fields -->
          <div
            v-if="form.userType === 'SELLER'"
            class="space-y-6 pt-4 border-t border-gray-600"
          >
            <h3 class="text-lg font-semibold text-gray-200 mb-4">
              Seller Information
            </h3>

            <div
              class="bg-gray-700 px-4 py-3 rounded-lg text-gray-200 text-sm border border-gray-600"
            >
              <span class="font-medium text-gray-400">Mobile Number: </span>
              <span class="itbms-email">{{ form.phoneNumber }}</span>
            </div>

            <div
              class="bg-gray-700 px-4 py-3 rounded-lg text-gray-200 text-sm border border-gray-600"
            >
              <span class="font-medium text-gray-400">Bank Name: </span>
              <span class="itbms-email">{{ form.bankName }}</span>
            </div>

            <div
              class="bg-gray-700 px-4 py-3 rounded-lg text-gray-200 text-sm border border-gray-600"
            >
              <span class="font-medium text-gray-400">Bank Account: </span>
              <span class="itbms-email">{{ form.bankAccount }}</span>
            </div>
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
    <SuccessModal
      :visible="showSuccess"
      message="Profile updated successfully."
      @close="showSuccess = false"
    />
    <ErrorModal
      :visible="showError"
      :message="errorMessage"
      @close="showError = false"
    />
  </div>
</template>
