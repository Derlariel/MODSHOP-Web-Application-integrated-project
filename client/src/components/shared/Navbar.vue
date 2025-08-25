<script setup>
import { Heart, ShoppingCart, User, Menu } from "lucide-vue-next";
import { useRoute } from "vue-router";
import { ref } from "vue";

const route = useRoute();
const isMobileMenuOpen = ref(false);
const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value;
};
</script>

<template>
  <nav
    class="fixed top-0 inset-x-0 bg-black bg-opacity-80 backdrop-blur-lg z-50 border-b border-neutral-800"
  >
    <div
      class="relative flex justify-between items-center h-14 w-full px-4 sm:px-6 lg:px-8"
    >
      <!-- Logo -->
      <router-link to="/" class="flex items-center gap-2">
        <img src="@/assets/icon.png" alt="logo" class="w-8 h-8 rounded-md" />
        <div class="text-xl tracking-wide text-white font-bold">MODSHOP</div>
      </router-link>

      <!-- Desktop Nav -->
      <div class="hidden lg:flex justify-center flex-1 text-white font-light">
        <ul
          class="flex justify-center space-x-6 lg:space-x-10 xl:space-x-12 font-light"
        >
          <li
            :class="
              route.path === '/main'
                ? 'text-white font-bold'
                : 'hover:text-white'
            "
          >
            <router-link :to="{ name: 'Main' }">Home</router-link>
          </li>
          <li
            :class="
              route.path.startsWith('/sale-items')
                ? 'text-white font-light'
                : 'hover:text-white'
            "
          >
            <router-link :to="{ name: 'product-gallery' }"
              >Products</router-link
            >
          </li>
          <li class="hover:text-white">
            <router-link :to="{ name: 'brands-list' }">Brands</router-link>
          </li>
          <li class="hover:text-white">
            <router-link to="/about">About</router-link>
          </li>
        </ul>
      </div>

      <!-- Icons -->
      <div class="hidden lg:flex text-white items-center space-x-4">
        <Heart class="w-5 h-5 cursor-pointer hover:text-white" />
        <ShoppingCart class="w-5 h-5 cursor-pointer hover:text-white" />
        <div class="cursor-pointer flex items-center space-x-2">
          <User class="w-5 h-5 cursor-pointer hover:text-white" />
          <!-- Uncomment the following lines if you want to display text next to icons -->
          <!-- <h1 class="font-bold text-md">Wishlist</h1> -->
          <!-- <component  :is="User" class="w-5 h-5" /> -->
<button
  class="font-bold text-sm bg-white text-black px-3 py-1 rounded-lg shadow-md 
         hover:bg-gradient-to-r hover:from-blue-500 hover:to-purple-500 
         hover:text-white hover:shadow-xl hover:scale-105 
         transition-all duration-300 ease-out"
>
  
<router-link to="/register">Register</router-link>

</button>

          <button class="font-bold text-md">Signin</button>
        </div>
      </div>

      <!-- Mobile Menu Button -->
      <div class="lg:hidden cursor-pointer" @click="toggleMobileMenu">
        <Menu class="w-6 h-6 text-white" />
      </div>
    </div>

    <!-- Mobile Nav -->
    <div
      v-if="isMobileMenuOpen"
      class="lg:hidden px-6 py-4 space-y-4 border-t text-center font-semibold text-white bg-black bg-opacity-90"
    >
      <router-link :to="{ name: 'Main' }" class="block hover:text-blue-400"
        >Home</router-link
      >
      <router-link
        :to="{ name: 'product-gallery' }"
        class="block hover:text-blue-400"
        >Product</router-link
      >
      <router-link
        :to="{ name: 'brands-list' }"
        class="block hover:text-blue-400"
        >Brand</router-link
      >
      <router-link to="/about" class="block hover:text-blue-400"
        >About</router-link
      >
    </div>
  </nav>
</template>
