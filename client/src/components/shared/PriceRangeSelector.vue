<script setup>
import { ref, defineProps, defineEmits, computed, watch } from "vue";
import { X } from "lucide-vue-next";

const props = defineProps({
  modelValue: {
    type: Object,
    default: () => ({ min: null, max: null })
  }
});

const emit = defineEmits([
  "update:modelValue",
  "price-selected",
  "price-cleared"
]);

const showDropdown = ref(false);
const minPrice = ref(props.modelValue?.min || null);
const maxPrice = ref(props.modelValue?.max || null);

// Pre-defined price ranges
const priceRanges = [
  { label: "0 - 5,000 Baht", min: 0, max: 5000 },
  { label: "5,001-10,000 Baht", min: 5001, max: 10000 },
  { label: "10,001-20,000 Baht", min: 10001, max: 20000 },
  { label: "20,001-30,000 Baht", min: 20001, max: 30000 },
  { label: "30,001-40,000 Baht", min: 30001, max: 40000 },
  { label: "40,001-50,000 Baht", min: 40001, max: 50000 },
  { label: "50,001 + Baht", min: 50001, max: null }
];

const selectedRange = computed(() => {
  if (minPrice.value !== null || maxPrice.value !== null) {
    if (minPrice.value !== null && maxPrice.value !== null) {
      return `${minPrice.value.toLocaleString()} - ${maxPrice.value.toLocaleString()}`;
    } else if (minPrice.value !== null) {
      return `${minPrice.value.toLocaleString()} +`;
    } else if (maxPrice.value !== null) {
      return `Up to ${maxPrice.value.toLocaleString()}`;
    }
  }
  return null;
});

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};

const selectPriceRange = (range) => {
  // Toggle functionality - if the same range is selected, clear it
  if (minPrice.value === range.min && maxPrice.value === range.max) {
    clearPriceRange();
  } else {
    // Set new price range
    minPrice.value = range.min;
    maxPrice.value = range.max;
    updateModelValue();
    emit("price-selected");
  }
  showDropdown.value = false;
};

const clearPriceRange = () => {
  minPrice.value = null;
  maxPrice.value = null;
  updateModelValue();
  emit("price-cleared");
};

const updateModelValue = () => {
  emit("update:modelValue", {
    min: minPrice.value,
    max: maxPrice.value
  });
};

// Initialize from props
watch(() => props.modelValue, (newValue) => {
  minPrice.value = newValue?.min || null;
  maxPrice.value = newValue?.max || null;
}, { immediate: true });
</script>
 
<template>
  <div class="flex flex-col max-w-full sm:max-w-2xl w-full">
    <!-- Selected price display area -->
    <div class="flex items-center w-full">
      <div class="flex flex-wrap items-center content-center flex-1 border border-gray-300 rounded-md bg-white min-h-[42px] px-2 py-1 gap-1">
        <div v-if="!selectedRange" class="text-gray-500 text-sm py-1">
          Price Range
        </div>
        <div v-if="selectedRange" class="itbms-price-item bg-blue-100 text-blue-800 text-sm rounded-full px-3 py-1 flex items-center shadow-sm border border-blue-200 transition-colors hover:bg-blue-200">
          {{ selectedRange }}
          <button @click="clearPriceRange" class="itbms-price-item-clear ml-2 text-blue-600 hover:text-red-600 transition-colors">
            <X class="w-3 h-3" />
          </button>
        </div>
      </div>

      <!-- Action buttons -->
      <div class="flex-shrink-0 flex">
        <button @click="toggleDropdown" class="itbms-price-filter px-4 py-2 bg-gray-500 border border-gray-500 hover:bg-gray-400 transition rounded-md h-[42px] ml-2">
          <span class="text-white text-sm">₿</span>
        </button>
      </div>
    </div>

    <!-- Dropdown menu -->
    <div class="relative w-full">
      <div v-if="showDropdown" class="absolute left-0 top-full mt-1 w-full z-50">
        <div class="bg-white border border-gray-300 rounded-md shadow-lg p-4 w-full">
          <!-- Pre-defined ranges -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2">Price Options</label>
            <div class="space-y-1">
              <button
                v-for="range in priceRanges"
                :key="range.label"
                @click="selectPriceRange(range)"
                :class="[
                  'w-full text-left px-3 py-2 text-sm rounded-md transition-colors',
                  minPrice === range.min && maxPrice === range.max 
                    ? 'bg-blue-50 text-blue-700 border border-blue-200' 
                    : 'hover:bg-gray-100'
                ]"
              >
                <span class="flex items-center justify-between">
                  {{ range.label }}
                  <span v-if="minPrice === range.min && maxPrice === range.max" class="text-blue-600">✓</span>
                </span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>
 
<style scoped>
/* Custom styles for price selector */
</style>