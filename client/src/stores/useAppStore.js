import { defineStore } from 'pinia';
import { ref } from 'vue';

export const useAppStore = defineStore('app', () => {
  const adminMode = ref(false);

  const toggleAdminMode = () => {
    adminMode.value = !adminMode.value;
  };

  return { adminMode, toggleAdminMode };
});
