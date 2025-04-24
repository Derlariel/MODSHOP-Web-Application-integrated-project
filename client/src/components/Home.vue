<script setup></script>
<template>
  <div
    class="relative min-h-screen bg-[linear-gradient(135deg,_#383838_3%,_#080808_48%)] text-white overflow-hidden"
  >
    <div
      class="grid grid-cols-1 md:grid-cols-2 place-items-center h-screen w-full px-6 md:px-12 z-10 relative"
    >
      <!-- Left: Text content -->
      <div class="text-left space-y-6 z-10 pl-14 md:pl-26 ">
        <h1
          class="text-4xl md:text-7xl font-extrabold tracking-wide leading-normal"
        >
          Discover Most<br />
          Affordable<br />
          <span class="whitespace-nowrap">Apple Products</span>
        </h1>
        <p class="text-[#8B8E99] max-w-md">
          Find the best, reliable and affordable apple products here. We focus
          on the product quality. Here you can find all the products apple ever
          made. Even the products apple officially stopped selling. So why you
          are waiting? Just order now!
        </p>
        <div
          class="flex items-center bg-white rounded-full px-4 py-2 w-full max-w-md"
        >
          <input
            type="text"
            placeholder="Find the best product"
            class="flex-grow text-black focus:outline-none"
          />
          <button
            class="bg-[#3D5AFE] text-white px-4 py-2 rounded-full font-semibold"
          >
            Search
          </button>
        </div>
      </div>

      <!-- Right: Big image -->
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
