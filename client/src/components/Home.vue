<script setup>
import MainPreview from './MainPreview.vue';
import PhoneRecommendations from './PhoneRecommendations.vue';
</script>

<template>
 <div
 class="relative min-h-screen bg-[linear-gradient(135deg,_#383838_3%,_#080808_43%)] text-white"

  >
    <!-- Hero Section -->
    <div
      class="grid grid-cols-1 md:grid-cols-2 place-items-center h-screen w-full px-6 md:px-12 z-10 relative"
    >
      <!-- Left Text -->
      <div class="text-left space-y-6 z-10 pl-14 md:pl-26">
        <h1
          class="text-4xl md:text-7xl font-extrabold tracking-wide leading-normal"
        >
          Discover the Most<br />
          Affordable Phones<br />
        </h1>
        <p class="text-[#8B8E99] max-w-xl">
          Find the best, most reliable, and affordable smartphones right here.
          We don’t just focus on one brand — we’ve got every damn phone you can
          think of. From the latest flagships to rare classics you can’t even
          find in stores anymore. We care about quality, not just flashy names.
          So what the hell are you waiting for? Order now and thank us later!
        </p>
        <router-link
          to="/main"
          class="flex items-center px-4 py-2 w-full max-w-md"
        >
          <button
            class="itmbs-shopnow bg-[#3D5AFE] text-white px-4 py-2 rounded-full font-semibold"
          >
            Shop Now!
          </button>
        </router-link>
      </div>

      <!-- Right Image -->
      <div class="w-full h-full flex justify-center items-center">
        <img
          src="@/assets/apple/iphone16-for-homepage.webp"
          alt="iPhone"
          class="w-full h-full object-contain max-w-[1000px]"
        />
      </div>
    </div>
  </div>
  
</template>

<style scoped></style>
