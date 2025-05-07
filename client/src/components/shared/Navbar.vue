<script setup>
import {
  Heart,
  ShoppingCart,
  User,
  UserCog,
  Menu,
} from "lucide-vue-next";
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
  <nav class="fixed top-0 inset-x-0 bg-black bg-opacity-80 backdrop-blur-lg z-50 border-b border-neutral-800">
    <div class="relative flex justify-between items-center h-14 w-full px-4 sm:px-6 lg:px-8">
      <!-- Logo -->
      <router-link to="/">
        <div class="text-xl font-light tracking-wide text-white">KK1</div>
      </router-link>

      <!-- Desktop Nav -->
      <div class="hidden lg:flex justify-center flex-1 text-white font-light">
        <ul class="flex justify-center space-x-6 lg:space-x-10 xl:space-x-12 font-light">
          <li :class="route.path === '/main' ? 'text-white font-bold' : 'hover:text-white'">
            <router-link to="/main">Home</router-link>
          </li>
          <li :class="route.path.startsWith('/sale-items') ? 'text-white font-light' : 'hover:text-white'">
            <router-link to="/sale-items">Product</router-link>
          </li>
          <li class="hover:text-white">
            <router-link to="/brand">Brand</router-link>
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
        <div @click="appStore.toggleAdminMode" class="cursor-pointer">
          <component :is="appStore.adminMode ? UserCog : User" class="w-5 h-5" />
        </div>
      </div>

      <!-- Mobile Menu Button -->
      <div class="lg:hidden cursor-pointer" @click="toggleMobileMenu">
        <Menu class="w-6 h-6 text-white" />
      </div>
    </div>

    <!-- Mobile Nav -->
    <div v-if="isMobileMenuOpen" class="lg:hidden px-6 py-4 space-y-4 border-t text-center font-semibold text-white bg-black bg-opacity-90">
      <router-link to="/main" class="block hover:text-blue-400">Home</router-link>
      <router-link to="/sale-items" class="block hover:text-blue-400">Product</router-link>
      <router-link to="/brand" class="block hover:text-blue-400">Brand</router-link>
      <router-link to="/about" class="block hover:text-blue-400">About</router-link>
    </div>
  </nav>
</template>
