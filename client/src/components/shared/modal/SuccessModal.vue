<script setup>
import { ref, watch, onMounted } from 'vue';
import successIcon from '@/assets/database-success-svgrepo-com.svg'

const props = defineProps({
  visible: { type: Boolean, default: false },
  message: { type: String, default: 'The sale item successfully has been added.' },
  duration: { type: Number, default: 5000 }
});

const emit = defineEmits(['close']);
const isVisible = ref(props.visible);
const animateIn = ref(false);

const closeModal = () => {
  animateIn.value = false;
  setTimeout(() => {
    isVisible.value = false;
    emit('close');
  }, 300);
};

const showModal = () => {
  isVisible.value = true;
  setTimeout(() => (animateIn.value = true), 10);
  if (props.duration > 0) {
    setTimeout(() => closeModal(), props.duration);
  }
};

watch(() => props.visible, (newVal) => newVal ? showModal() : closeModal());
onMounted(() => props.visible && showModal());
</script>

<template>
    <div v-if="isVisible" class="fixed inset-0 z-50 flex justify-end items-end p-4 pointer-events-none">
      <div 
        class="absolute inset-0 bg-black/30 transition-opacity duration-300 pointer-events-auto"
        :class="animateIn ? 'opacity-100' : 'opacity-0'"
        @click="closeModal"
      ></div>
      <div
        class="relative w-full max-w-sm bg-zinc-900/90 border border-zinc-700 rounded-2xl shadow-2xl backdrop-blur-md pointer-events-auto transition-all duration-300 transform"
        :class="animateIn ? 'opacity-100 translate-y-0 scale-100' : 'opacity-0 translate-y-6 scale-95'"
        @click.stop
      >
        <div class="p-6">
          <div class="flex justify-center mb-4">
            <div class="w-14 h-14rounded-full flex items-center justify-center">
              <img class="w-10 h-10" :src="successIcon" alt="">
            </div>
          </div>
          <div class="text-center">
            <h3 class="text-white text-2xl font-semibold mb-1">Success</h3>
            <p class="text-zinc-400 text-sm">{{ message }}</p>
          </div>
        </div>
      </div>
    </div>
</template>
