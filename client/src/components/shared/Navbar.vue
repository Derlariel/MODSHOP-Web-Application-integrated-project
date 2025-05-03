<script setup>
import { Search, Heart, ShoppingCart, User, UserCog } from "lucide-vue-next";
import ThemeToggle from "./ThemeToggle.vue";
import { useRoute } from "vue-router";
import { useAppStore } from "@/stores/useAppStore";

const route = useRoute();
const appStore = useAppStore();
</script>

<template>
  <nav class="sticky w-full z-50 h-16 flex items-center justify-between px-8 py-2 bg-white shadow text-gray-800">
    <!-- Logo -->
    <router-link to="/">
      <div class="text-2xl font-bold tracking-wide text-blue-600">Phonezy</div>
    </router-link>

    <!-- Menu -->
    <ul class="absolute left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 flex space-x-6 font-medium">
      <li :class="route.path === '/main' ? 'text-blue-600 font-bold' : 'hover:text-blue-600'">
        <router-link to="/main">Homie</router-link>
      </li>
      <li :class="route.path.startsWith('/v1/sale-items') ? 'text-blue-600 font-bold' : 'hover:text-blue-600'">
        <router-link to="/v1/sale-items">Product ▼</router-link>
      </li>
      <li class="hover:text-blue-600">Brand</li>
      <li class="hover:text-blue-600">About</li>
    </ul>

    <!-- Right Controls -->
    <div class="flex items-center space-x-4">
      <div class="relative">
        <input
          type="text"
          placeholder="What are you looking for?"
          class="pl-10 pr-4 py-2 rounded-full border border-gray-300 focus:ring-2 focus:ring-blue-400 w-64"
        />
        <Search class="absolute top-1/2 left-3 transform -translate-y-1/2 text-gray-400 w-5 h-5" />
      </div>

      <Heart class="w-5 h-5 cursor-pointer hover:text-blue-600" />
      <ShoppingCart class="w-5 h-5 cursor-pointer hover:text-blue-600" />

      <!-- Toggle Admin Mode -->
      <div @click="appStore.toggleAdminMode" class="cursor-pointer">
        <component :is="appStore.adminMode ? UserCog : User" class="w-5 h-5" />
      </div>

      <ThemeToggle />
    </div>
  </nav>
</template>
