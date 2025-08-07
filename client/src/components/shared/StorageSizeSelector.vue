<script setup>
import { ref, defineProps, defineEmits, computed, onMounted } from "vue";
import { X, HardDrive } from "lucide-vue-next";

const props = defineProps({
  modelValue: {
    type: Array,
    default: () => []
  }
});

const emit = defineEmits([
  "update:modelValue",
  "storage-selected",
  "storage-removed",
  "clear-storage"
]);

const showDropdown = ref(false);
const availableStorageSizes = ref([]);

// Available storage sizes from database
const defaultStorageSizes = [32, 64, 128, 256, 512, 1024]; // 1TB = 1024GB

const selectedSizes = computed({
  get() {
    return props.modelValue;
  },
  set(value) {
    emit("update:modelValue", value);
  }
});

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};

const selectStorageSize = (size) => {
  // Toggle functionality - if storage size is already selected, remove it
  if (selectedSizes.value.includes(size)) {
    const newSelection = selectedSizes.value.filter(s => s !== size);
    emit("update:modelValue", newSelection);
    emit("storage-removed", size);
  } else {
    // Add new storage size
    const newSelection = [...selectedSizes.value, size];
    emit("update:modelValue", newSelection);
    emit("storage-selected", size);
  }
};

const removeStorageSize = (size) => {
  const newSelection = selectedSizes.value.filter(s => s !== size);
  emit("update:modelValue", newSelection);
  emit("storage-removed", size);
};

const clearAllStorageSizes = () => {
  emit("update:modelValue", []);
  emit("clear-storage");
};

const formatStorageSize = (size) => {
  if (size >= 1024) {
    return `${size / 1024}TB`;
  }
  return `${size}GB`;
};

// Fetch available storage sizes from database
const fetchStorageSizes = async () => {
  try {
    const BASE_URL = import.meta.env.VITE_BASE_URL;
    // First try to get distinct storage sizes from the view
    const res = await fetch(`${BASE_URL}/v1/distinct-storage-size`);
    if (res.ok) {
      const data = await res.json();
      // Assuming the view returns an array of objects with storage_gb property
      availableStorageSizes.value = data.map(item => item.storage_gb || item.storageGb).sort((a, b) => a - b);
    } else {
      // Fallback to default sizes if API fails
      availableStorageSizes.value = defaultStorageSizes;
    }
  } catch (err) {
    console.error("Error fetching storage sizes:", err);
    availableStorageSizes.value = defaultStorageSizes;
  }
};

onMounted(() => {
  fetchStorageSizes();
});
</script>
 
<template>
  <div class="flex flex-col max-w-full sm:max-w-2xl w-full">
    <!-- Selected storage display area -->
    <div class="flex items-center w-full">
      <div 
        class="flex-1 rounded-md rounded-r-none bg-white min-h-[42px] max-h-[45px] overflow-y-auto"
      >
        <div class="grid grid-cols-2 gap-1 p-2">
          <div v-if="selectedSizes.length === 0" class="col-span-2 text-gray-500 text-sm py-1">
            Storage Size
          </div>
          <div
            v-for="size in selectedSizes"
            :key="size"
            class="itbms-storage-size-item bg-blue-100 text-blue-800 text-xs rounded-full px-2 py-1 flex items-center shadow-sm border border-blue-200 transition-colors hover:bg-blue-200 min-w-0"
          >
            <span class="truncate flex-1">{{ formatStorageSize(size) }}</span>
            <button
              @click="removeStorageSize(size)"
              class="itbms-storage-size-item-clear ml-1 text-blue-600 hover:text-red-600 transition-colors flex-shrink-0"
            >
              <X class="w-3 h-3" />
            </button>
          </div>
        </div>
      </div>

      <!-- Action buttons -->
      <div class="flex-shrink-0 flex">
        <button
          @click="toggleDropdown"
          class="itbms-storage-size-filter px-4 py-2 bg-gray-500 border border-gray-500 hover:bg-gray-400 transition rounded-md rounded-l-none h-[42px]"
        >
          <HardDrive class="w-5 h-5 text-white" />
          <span class="sr-only">
            <span v-for="size in selectedSizes" :key="size">{{ formatStorageSize(size) }}</span>
          </span>
        </button>
      </div>
    </div>

    <!-- Dropdown menu -->
    <div class="relative w-full">
      <div v-if="showDropdown" class="absolute left-0 top-full mt-1 w-full z-50">
        <ul class="bg-white border border-gray-300 rounded-md shadow-lg max-h-48 overflow-y-auto w-full">
          <li
            v-for="size in availableStorageSizes"
            :key="size"
            @click="selectStorageSize(size)"
            class="itbms-storage-size-option px-4 py-2 hover:bg-gray-100 cursor-pointer text-sm border-b border-gray-100 last:border-b-0 transition-colors"
            :class="{ 'bg-blue-50 text-blue-700': selectedSizes.includes(size) }"
          >
            <span class="flex items-center justify-between">
              {{ formatStorageSize(size) }}
              <span v-if="selectedSizes.includes(size)" class="text-blue-600">✓</span>
            </span>
          </li>
        </ul>
      </div>
    </div>
  </div>
</template>
 
<style scoped>
/* Custom styles for storage selector */
</style>