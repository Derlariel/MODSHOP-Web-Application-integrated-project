<script setup>
import { useAuthStore } from "@/stores/useAuthStore";
const auth = useAuthStore();
</script>

<template>
  <div
    class="relative min-h-screen bg-[linear-gradient(135deg,_#383838_3%,_#080808_43%)] text-white"
  >
    <!-- Hero Section -->
    <div
      class="grid grid-cols-1 md:grid-cols-2 place-items-center min-h-screen w-full px-6 md:px-12 py-24 md:py-0"
    >
      <!-- Left Text -->
      <div class="text-left space-y-6 z-10 w-full max-w-2xl px-4 md:px-8">
        <h1
          class="text-4xl md:text-6xl font-extrabold tracking-wide leading-tight"
        >
          Discover the Most<br />
          Affordable Phones
        </h1>
        <p class="text-[#8B8E99] text-base sm:text-lg leading-relaxed">
          Find the best, most reliable, and affordable smartphones right here.
          We don’t just focus on one brand — we’ve got every damn phone you can
          think of. From the latest flagships to rare classics you can’t even
          find in stores anymore. We care about quality, not just flashy names.
          So what the hell are you waiting for? Order now and thank us later!
        </p>
        <div class="flex flex-col sm:flex-row gap-3">
          <router-link to="/main">
            <button
              class="itmbs-shopnow bg-[#3D5AFE] text-white px-6 py-2 rounded-full font-semibold w-full sm:w-auto"
            >
              Shop Now!
            </button>
          </router-link>
          <router-link
            v-if="auth.isAuthenticated && auth.user && auth.user.role === 'SELLER'"
            :to="{ name: 'product-list' }"
          >
            <button
              class="itmbs-seller bg-[#3D5AFE] text-white px-6 py-2 rounded-full font-semibold w-full sm:w-auto"
            >
              Seller
            </button>
          </router-link>
        </div>
      </div>

      <!-- Right Image -->
      <div class="w-full h-full flex justify-center items-center p-4">
        <img
          src="@/assets/apple/iphone16-for-homepage.webp"
          alt="iPhone"
          class="w-full max-w-[400] sm:max-w-[600] lg:max-w-[800px] object-contain"
        />
      </div>
    </div>
  </div>
</template>

<style scoped></style>