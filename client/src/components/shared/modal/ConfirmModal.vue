<script setup>
import { ref, watch } from 'vue';

const props = defineProps({
  visible: {
    type: Boolean,
    default: false
  },
  title: {
    type: String,
    default: 'Delete Item'
  },
  message: {
    type: String,
    default: 'Are you sure you want to delete this item? This action cannot be undone.'
  },
  confirmText: {
    type: String,
    default: 'Delete'
  },
  cancelText: {
    type: String,
    default: 'Cancel'
  },
  itemName: {
    type: String,
    default: ''
  }
});

const emit = defineEmits(['confirm', 'cancel']);

const isVisible = ref(props.visible);
const animateIn = ref(false);

watch(() => props.visible, (newValue) => {
  isVisible.value = newValue;
  if (newValue) {
    setTimeout(() => {
      animateIn.value = true;
    }, 10);
  }
});

const handleConfirm = () => {
  closeDialog();
  emit('confirm');
};

const handleCancel = () => {
  closeDialog();
  emit('cancel');
};

const closeDialog = () => {
  animateIn.value = false;
  setTimeout(() => {
    isVisible.value = false;
  }, 300);
};
</script>

<template>
  <Teleport to="body">
    <div 
      v-if="isVisible" 
      class="fixed inset-0 z-50 flex items-center justify-center px-4"
      @click="handleCancel"
    >
      <div 
        class="absolute inset-0 bg-black/30 backdrop-blur-md"
        :class="[animateIn ? 'opacity-100' : 'opacity-0']"
        style="transition: opacity 0.3s ease-in-out"
      ></div>
      
      <div 
        class="relative bg-white rounded-2xl overflow-hidden max-w-md w-full transform transition-all duration-300 ease-out shadow-xl"
        :class="[animateIn ? 'opacity-100 translate-y-0 scale-100' : 'opacity-0 translate-y-4 scale-95']"
        @click.stop
      >
        <div class="p-6 sm:p-8">
          <div class="text-center mb-6">
            <h3 class="text-xl font-semibold text-gray-900 mb-2">{{ title }}</h3>
            <p class="text-gray-600">
              {{ message }}
              <span v-if="itemName" class="font-medium text-gray-900">{{ itemName }}</span>
            </p>
          </div>
          
          <div class="flex flex-col space-y-3">
            <button 
              @click="handleConfirm"
              class="w-full py-3 px-4 rounded-full bg-red-500 text-white font-medium hover:bg-red-600 transition-colors focus:outline-none focus:ring-2 focus:ring-red-500 focus:ring-offset-2"
            >
              {{ confirmText }}
            </button>
            
            <button 
              @click="handleCancel"
              class="w-full py-3 px-4 rounded-full bg-gray-200 text-gray-800 font-medium hover:bg-gray-300 transition-colors focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
            >
              {{ cancelText }}
            </button>
          </div>
        </div>
      </div>
    </div>
  </Teleport>
</template>