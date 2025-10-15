<script setup>
import { Heart, ShoppingCart, User, Menu, FileText, ShoppingBag } from "lucide-vue-next";
import { useRoute, useRouter } from "vue-router";
import { computed, ref, onMounted, watch } from "vue";
import ConfirmModal from "@/components/shared/modal/ConfirmModal.vue";
import { useAuthStore } from "@/stores/useAuthStore";
import { useCartStore } from "@/stores/useCartStore";
import { useSellerOrdersStore } from "@/stores/useSellerOrdersStore";
import { useOrderStore } from "@/stores/useOrderStore";

const route = useRoute();
const router = useRouter();
const isMobileMenuOpen = ref(false);
const toggleMobileMenu = () => {
  isMobileMenuOpen.value = !isMobileMenuOpen.value;
};

const auth = useAuthStore();
const cart = useCartStore();
const sellerOrders = useSellerOrdersStore();
const orderStore = useOrderStore();

const greeting = computed(() => {
  return auth.isAuthenticated ? `${auth.nickname}` : "Login";
});

const showLogoutModal = ref(false);
const handleLogout = () => {
  showLogoutModal.value = true;
};
const confirmLogout = async () => {
  showLogoutModal.value = false;
  await auth.logout();
  sessionStorage.setItem("logout-success", "true");
  await router.replace("/sale-items");
  window.location.reload();
};
const cancelLogout = () => {
  showLogoutModal.value = false;
};

onMounted(() => {
  if (auth.isAuthenticated && auth.user) {
    if (auth.user.role === 'SELLER') {
      sellerOrders.refreshBadge(auth.user.id);
    }
    if (auth.user.role === 'BUYER' || auth.user.role === 'SELLER') {
      orderStore.refreshBadge(auth.user.id);
    }
  }
});

watch(
  () => auth.user,
  (u) => {
    if (u?.role === 'SELLER') {
      sellerOrders.refreshBadge(u.id);
    }
    if (u?.role === 'BUYER' || u?.role === 'SELLER') {
      orderStore.refreshBadge(u.id);
    }
  },
  { immediate: false }
);
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
        <!-- Seller Sales Orders shortcut -->
        <router-link v-if="auth.isAuthenticated && auth.user?.role==='SELLER'" to="/sale-orders" class="relative">
          <ShoppingBag class="w-5 h-5 cursor-pointer hover:text-white" />
          <span
            v-if="sellerOrders.newOrdersCount > 0"
            class="absolute -top-2 -right-2 bg-purple-500 text-white text-xs min-w-5 h-5 px-1 flex items-center justify-center rounded-full"
          >
            {{ sellerOrders.newOrdersCount }}
          </span>
        </router-link>

        <!-- Your Orders link for both Seller and Buyer -->
        <router-link v-if="auth.isAuthenticated && (auth.user?.role==='BUYER' || auth.user?.role==='SELLER')" to="/your-orders" class="relative">
          <FileText class="w-5 h-5 cursor-pointer hover:text-white" />
          <span
            v-if="orderStore.newOrdersCount > 0"
            class="absolute -top-2 -right-2 bg-blue-500 text-white text-xs min-w-5 h-5 px-1 flex items-center justify-center rounded-full"
          >
            {{ orderStore.newOrdersCount }}
          </span>
        </router-link>

        <router-link to="/cart" class="relative">
          <ShoppingCart class="w-5 h-5 cursor-pointer hover:text-white" />
          <span
            v-if="auth.isAuthenticated && cart.totalItems > 0"
            class="absolute -top-2 -right-2 bg-red-500 text-white text-xs w-5 h-5 flex items-center justify-center rounded-full"
          >
            {{ cart.totalItems }}
          </span>
        </router-link>
        
        <router-link
          v-if="!auth.isAuthenticated"
          to="/register"
          class="font-bold text-sm bg-white text-black px-3 py-1 rounded-lg shadow-md hover:bg-gradient-to-r hover:from-blue-500 hover:to-purple-500 hover:text-white hover:shadow-xl hover:scale-105 transition-all duration-300 ease-out"
        >
          Register
        </router-link>

        <div class="flex items-center gap-4">
          <router-link to="/profile" v-if="auth.isAuthenticated">
            <div class="flex space-x-1 items-center ">
              <User class="itbms-profile-button w-5 h-5" />
              <span v-if="auth.isAuthenticated" class="itbms-nickname text-sm">{{ greeting }}</span>
            </div>
          </router-link>
          <router-link to="/login" v-else
            class="font-bold text-sm bg-white text-black px-3 py-1 rounded-lg shadow-md
               hover:bg-gradient-to-r hover:from-purple-500 hover:to-pink-500
               hover:text-white hover:shadow-xl hover:scale-105 transition-all duration-300 ease-out"
          > Login</router-link>
          <button 
            v-if="auth.isAuthenticated"
            @click="handleLogout"
            class="itbms-logout text-sm opacity-70 hover:opacity-100"
          >
            Logout
          </button>
          <ConfirmModal
            :visible="showLogoutModal"
            title="Logout Confirmation"
            message="Are you sure you want to logout?"
            confirmText="Logout"
            cancelText="Cancel"
            @confirm="confirmLogout"
            @cancel="cancelLogout"
          />
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
      <router-link :to="{ name: 'Main' }" class="block hover:text-blue-400">Home</router-link>
      <router-link :to="{ name: 'product-gallery' }" class="block hover:text-blue-400">Products</router-link>
      <router-link :to="{ name: 'brands-list' }" class="block hover:text-blue-400">Brands</router-link>
      <router-link to="/about" class="block hover:text-blue-400">About</router-link>

      <!-- Seller Sales Orders shortcut -->
      <router-link v-if="auth.isAuthenticated && auth.user?.role==='SELLER'" to="/sale-orders" class="block hover:text-purple-400 flex items-center gap-1 justify-center">
        <ShoppingBag class="w-5 h-5" />
        <span>Sales Orders</span>
        <span v-if="sellerOrders.newOrdersCount > 0" class="ml-2 bg-purple-500 text-white text-xs min-w-5 h-5 px-2 py-0.5 rounded-full">{{ sellerOrders.newOrdersCount }}</span>
      </router-link>

      <!-- Your Orders link for both Seller and Buyer -->
      <router-link v-if="auth.isAuthenticated && (auth.user?.role==='BUYER' || auth.user?.role==='SELLER')" to="/your-orders" class="block hover:text-purple-400 flex items-center gap-1 justify-center">
        <FileText class="w-5 h-5" />
        <span>Your Orders</span>
        <span v-if="orderStore.newOrdersCount > 0" class="ml-2 bg-blue-500 text-white text-xs min-w-5 h-5 px-2 py-0.5 rounded-full">{{ orderStore.newOrdersCount }}</span>
      </router-link>

      <!-- Cart link -->
      <router-link to="/cart" class="block hover:text-red-400 relative">
        Cart
        <span v-if="auth.isAuthenticated && cart.totalItems > 0" class="ml-2 bg-red-500 text-white text-xs w-5 h-5 inline-flex items-center justify-center rounded-full">{{ cart.totalItems }}</span>
      </router-link>

      <!-- Register -->
      <router-link
        v-if="!auth.isAuthenticated"
        to="/register"
        class="inline-block font-bold text-sm bg-white text-black px-3 py-1 rounded-lg shadow-md hover:bg-gradient-to-r hover:from-blue-500 hover:to-purple-500 hover:text-white hover:shadow-xl hover:scale-105 transition-all duration-300 ease-out"
        style="width: auto; min-width: 80px;"
      >
        Register
      </router-link>

      <!-- Profile / Login / Logout -->
      <div class="flex items-center justify-center gap-4 mt-4">
        <router-link to="/profile" v-if="auth.isAuthenticated" class="itbms-profile">
          <span v-if="auth.isAuthenticated" class="itbms-nickname">{{ greeting }}</span>
        </router-link>
        <router-link to="/login" v-else
          class="itbms-login block font-bold text-sm bg-white text-black px-3 py-1 rounded-lg shadow-md 
           hover:bg-gradient-to-r hover:from-purple-500 hover:to-pink-500 
           hover:text-white hover:shadow-xl hover:scale-105 transition-all duration-300 ease-out "
        >
          Login
        </router-link>
        <button
          v-if="auth.isAuthenticated"
          @click="handleLogout"
          class="itbms-logout text-sm opacity-70 hover:opacity-100"
        >
          Logout
        </button>
        <ConfirmModal
          :visible="showLogoutModal"
          title="Logout Confirmation"
          message="Are you sure you want to logout?"
          confirmText="Logout"
          cancelText="Cancel"
          @confirm="confirmLogout"
          @cancel="cancelLogout"
        />
      </div>
    </div>
  </nav>
</template>
