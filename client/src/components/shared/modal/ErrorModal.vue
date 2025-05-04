<script setup>
import { X } from 'lucide-vue-next';

defineProps({
  visible: Boolean,
  message: {
    type: String,
    default: "An error occurred. Please try again later."
  }
});
</script>

<template>
  <transition name="fade">
    <div
      v-if="visible"
      class="fixed inset-0 backdrop-blur-sm bg-black/30 z-50 flex items-center justify-center"
    >
      <div class="bg-white rounded-lg shadow-xl p-6 max-w-md w-full relative">
        <button
          @click="$emit('close')"
          class="absolute top-3 right-3 text-gray-500 hover:text-gray-700 transition"
        >
          <X class="w-5 h-5" />
        </button>
        <h2 class="text-lg font-semibold text-red-600 mb-2 text-center">Error</h2>
        <p class="text-gray-800 text-center">{{ message }}</p>
      </div>
    </div>
  </transition>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
