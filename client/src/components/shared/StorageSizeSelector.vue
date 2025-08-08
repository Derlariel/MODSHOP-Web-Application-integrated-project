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

// Available storage sizes from database (include special values)
const defaultStorageSizes = [32, 64, 128, 256, 512, 1024, "Not specified"]; // 1TB = 1024GB

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
  if (size === "Not specified") {
    return "Not specified";
  }
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
      const dbSizes = data.map(item => item.storage_gb || item.storageGb).sort((a, b) => a - b);
      // Add "Not specified" option
      availableStorageSizes.value = [...dbSizes, "Not specified"];
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
  <div class="flex flex-col w-full">
    <!-- Selected storage display area -->
    <div class="flex items-center w-full">
      <div 
        class="flex-1 rounded-md rounded-r-none bg-white min-h-[32px] md:min-h-[36px] max-h-[36px] md:max-h-[36px] overflow-y-auto border border-gray-300"
      >
        <div class="grid grid-cols-1 gap-1 p-1 md:p-1.5">
          <div v-if="selectedSizes.length === 0" class="col-span-1 text-gray-500 text-xs md:text-sm py-0.5">
            Storage Size
          </div>
          <div
            v-for="size in selectedSizes"
            :key="size"
            class="itbms-storage-size-item bg-blue-100 text-blue-800 text-xs rounded-full px-1.5 py-0.5 flex items-center shadow-sm border border-blue-200 transition-colors hover:bg-blue-200 min-w-0"
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
          class="itbms-storage-size-filter px-2 md:px-3 py-1.5 bg-gray-500 border border-gray-500 hover:bg-gray-400 transition rounded-md rounded-l-none h-[32px] md:h-[36px] w-[42px] md:w-[48px] flex items-center justify-center"
        >
          <HardDrive class="w-3 h-3 md:w-4 md:h-4 text-white" />
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
/* Hide scrollbars */
.overflow-y-auto::-webkit-scrollbar {
  display: none;
}

.overflow-y-auto {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>