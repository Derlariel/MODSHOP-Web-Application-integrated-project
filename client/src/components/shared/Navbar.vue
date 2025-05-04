<script setup>
import { Search, Heart, ShoppingCart, User, UserCog, Menu } from "lucide-vue-next";
import ThemeToggle from "./ThemeToggle.vue";
import { useRoute } from "vue-router";
import { useAppStore } from "@/stores/useAppStore";
import { ref } from "vue";

const route = useRoute();
const appStore = useAppStore();
const isMobileMenuOpen = ref(false);
const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value;
};
</script>

<template>
  <div class="w-full sticky top-0 z-50">
    <!-- Nav -->
    <nav class="bg-white shadow text-gray-800">
      <div class="relative flex justify-between items-center h-16 w-full px-4 sm:px-6 lg:px-8">
        <!-- Logo (ซ้ายสุด) -->
        <router-link to="/">
          <div class="text-2xl font-bold tracking-wide text-blue-600">Phonezy</div>
        </router-link>

        <!-- Desktop Menu -->
        <div class="hidden lg:flex justify-center flex-1">
          <div class="max-w-screen-xl w-full mx-auto">
            <ul class="flex justify-center space-x-6 lg:space-x-12 xl:space-x-24 font-semibold">
              <li :class="route.path === '/main' ? 'text-blue-600 font-bold' : 'hover:text-blue-600'">
                <router-link to="/main">Home</router-link>
              </li>
              <li :class="route.path.startsWith('/sale-items') ? 'text-blue-600 font-bold' : 'hover:text-blue-600'">
                <router-link to="/sale-items">Product</router-link>
              </li>
              <li class="hover:text-blue-600">Brand</li>
              <li class="hover:text-blue-600">About</li>
            </ul>
          </div>
        </div>

        <!-- Right Controls (ขวาสุด) -->
        <div class="hidden lg:flex items-center space-x-4">
          <div class="relative">
            <input type="text" placeholder="What are you looking for?"
              class="pl-10 pr-4 py-2 rounded-full border border-gray-300 focus:ring-2 focus:ring-blue-400 w-64" />
            <Search class="absolute top-1/2 left-3 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
          </div>

          <Heart class="w-5 h-5 cursor-pointer hover:text-blue-600" />
          <ShoppingCart class="w-5 h-5 cursor-pointer hover:text-blue-600" />

          <div @click="appStore.toggleAdminMode" class="cursor-pointer">
            <component :is="appStore.adminMode ? UserCog : User" class="w-5 h-5" />
          </div>

          <ThemeToggle />
        </div>

        <!-- Mobile Menu Button -->
        <div class="lg:hidden cursor-pointer" @click="toggleMobileMenu">
          <Menu class="w-6 h-6" />
        </div>
      </div>

      <!-- Mobile Menu -->
      <div v-if="isMobileMenuOpen" class="lg:hidden px-6 py-4 space-y-4 font-medium border-t text-center font-semibold">
        <router-link to="/main" class="block hover:text-blue-600">Home</router-link>
        <router-link to="/sale-items" class="block hover:text-blue-600">Product</router-link>
        <router-link to="/brand" class="block hover:text-blue-600">Brand</router-link>
        <router-link to="/about" class="block hover:text-blue-600">About</router-link>
      </div>
    </nav>
  </div>
</template>
