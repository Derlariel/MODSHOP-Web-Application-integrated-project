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
const availableStorageSizes = ref([
  { value: "64", label: "64GB" },
  { value: "128", label: "128GB" },
  { value: "256", label: "256GB" },
  { value: "512", label: "512GB" },
  { value: "null", label: "Not specified" }
]);

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

const selectStorageSize = (storageOption) => {
  let newSelection;
  const storageValue = storageOption.value;
  
  if (selectedSizes.value.includes(storageValue)) {
    // Remove the selected size
    newSelection = selectedSizes.value.filter(s => s !== storageValue);
    emit("storage-removed", storageValue);
  } else {
    // Add the new size
    newSelection = [...selectedSizes.value, storageValue];
    emit("storage-selected", storageValue);
  }
  
  emit("update:modelValue", newSelection);
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
  if (size === "null") {
    return "Not specified";
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
            v-for="storageOption in availableStorageSizes"
            :key="storageOption.value"
            @click="selectStorageSize(storageOption)"
            class="itbms-storage-size-option px-4 py-2 hover:bg-gray-100 cursor-pointer text-sm border-b border-gray-100 last:border-b-0 transition-colors"
            :class="{ 'bg-blue-50 text-blue-700': selectedSizes.includes(storageOption.value) }"
          >
            <span class="flex items-center justify-between">
              {{ storageOption.label }}
              <span v-if="selectedSizes.includes(storageOption.value)" class="text-blue-600">✓</span>
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