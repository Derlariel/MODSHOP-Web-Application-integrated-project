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
    <nav class="fixed top-0 inset-x-0 bg-black bg-opacity-80 backdrop-blur-lg z-50 border-b border-neutral-800">
      <div class="relative flex justify-between items-center h-14 w-full px-4 sm:px-6 lg:px-8">
        <router-link to="/">
          <div class="text-2xl font-medium tracking-wide text-white">KK1</div>
        </router-link>

        <div class="hidden text-white font-light lg:flex justify-center flex-1">
          <div class="max-w-screen-xl w-full mx-auto">
            <ul class="flex justify-center space-x-6 lg:space-x-10 xl:space-x-10">
              <li :class="route.path === '/main' ? 'text-white font-normal' : 'hover:text-white'">
                <router-link to="/main">Home</router-link>
              </li>
              <li :class="route.path.startsWith('/sale-items') ? 'text-white font-normal' : 'hover:text-white'">
                <router-link to="/sale-items">Product</router-link>
              </li>
              <li class="hover:text-white">Brand</li>
              <li class="hover:text-white">About</li>
            </ul>
          </div>
        </div>


        <div class="hidden lg:flex text-white items-center space-x-4">
          <Heart class="w-5 h-5 cursor-pointer hover:text-white" />
          <ShoppingCart class="w-5 h-5 cursor-pointer hover:text-white" />
          <div @click="appStore.toggleAdminMode" class="cursor-pointer">
            <component :is="appStore.adminMode ? UserCog : User" class="w-5 h-5" />
          </div>
        </div>

        <!-- Mobile Menu Button -->
        <div class="lg:hidden cursor-pointer" @click="toggleMobileMenu">
          <Menu class="w-6 h-6" />
        </div>
      </div>

      <!-- Mobile Menu -->
      <div v-if="isMobileMenuOpen" class="lg:hidden px-6 py-4 space-y-4 border-t text-center font-semibold">
        <router-link to="/main" class="block hover:text-blue-600">Home</router-link>
        <router-link to="/sale-items" class="block hover:text-blue-600">Product</router-link>
        <router-link to="/brand" class="block hover:text-blue-600">Brand</router-link>
        <router-link to="/about" class="block hover:text-blue-600">About</router-link>
      </div>
    </nav>
</template>

