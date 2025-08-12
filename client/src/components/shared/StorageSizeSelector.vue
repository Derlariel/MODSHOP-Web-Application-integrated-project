<script setup>
import { ref, defineProps, defineEmits, computed } from "vue";
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
const availableStorageSizes = ref([32, 64, 128, 256, 512, 1024, "Not specified"]);

const selectedSizes = computed({
  get() {
    // Convert API values (with null) to display values (with "Not specified")
    return props.modelValue.map(toDisplayValue);
  },
  set(value) {
    emit("update:modelValue", value);
  }
});

// Helper function to convert display value to API value
const toApiValue = (displayValue) => {
  return displayValue === "Not specified" ? null : displayValue;
};

// Helper function to convert API value to display value
const toDisplayValue = (apiValue) => {
  return apiValue === null ? "Not specified" : apiValue;
};

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};

const selectStorageSize = (size) => {
  // If "Not specified" is selected, clear all other selections and only keep "Not specified"
  if (size === "Not specified") {
    if (selectedSizes.value.includes("Not specified")) {
      // If "Not specified" is already selected, remove it (toggle off)
      emit("update:modelValue", []);
      emit("storage-removed", null);
    } else {
      // Clear all and select only "Not specified"
      emit("update:modelValue", [null]);
      emit("storage-selected", null);
    }
    return;
  }

  // If any specific storage size is selected, remove "Not specified" first
  let newSelection;
  if (selectedSizes.value.includes(size)) {
    // Remove the selected size
    newSelection = selectedSizes.value.filter(s => s !== size);
  } else {
    // Add the new size and remove "Not specified" if it exists
    newSelection = [size, ...selectedSizes.value.filter(s => s !== "Not specified")];
  }
  
  const apiValues = newSelection.map(toApiValue);
  emit("update:modelValue", apiValues);
  
  if (selectedSizes.value.includes(size)) {
    emit("storage-removed", toApiValue(size));
  } else {
    emit("storage-selected", toApiValue(size));
  }
};

const removeStorageSize = (size) => {
  const newSelection = selectedSizes.value.filter(s => s !== size);
  const apiValues = newSelection.map(toApiValue);
  emit("update:modelValue", apiValues);
  emit("storage-removed", toApiValue(size));
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
</script>
 
<template>
  <div class="flex flex-col w-full">
    <!-- Selected storage display area -->
    <div class="flex items-center w-full">
      <div 
        class="flex-1 rounded-md rounded-r-none bg-white min-h-[32px] md:min-h-[36px] max-h-[36px] md:max-h-[36px] overflow-y-auto border border-gray-300"
      >
        <div 
          class="grid gap-1 p-1 md:p-1.5"
          :class="selectedSizes.length === 1 ? 'grid-cols-1' : 'grid-cols-2'"
        >
          <div v-if="selectedSizes.length === 0" class="col-span-2 text-gray-500 text-xs md:text-sm py-0.5">
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