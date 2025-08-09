<script setup>
import { ref, defineProps, defineEmits, computed, watch } from "vue";
import { X } from "lucide-vue-next";

const props = defineProps({
  lowerPrice: {
    type: [Number, null],
    default: null
  },
  upperPrice: {
    type: [Number, null],
    default: null
  }
});

const emit = defineEmits([
  "update:lowerPrice",
  "update:upperPrice",
  "price-selected",
  "price-cleared"
]);

const showDropdown = ref(false);
const minPrice = ref(props.lowerPrice || null);
const maxPrice = ref(props.upperPrice || null);

const customMinPrice = ref("");
const customMaxPrice = ref("");

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
  emit("update:lowerPrice", minPrice.value);
  emit("update:upperPrice", maxPrice.value);
};

const handleCustomPriceInput = () => {
  let min = customMinPrice.value ? parseInt(customMinPrice.value) : null;
  let max = customMaxPrice.value ? parseInt(customMaxPrice.value) : null;
  
  // Auto-swap if min > max
  if (min !== null && max !== null && min > max) {
    [min, max] = [max, min];
    customMinPrice.value = min.toString();
    customMaxPrice.value = max.toString();
  }
  
  minPrice.value = min;
  maxPrice.value = max;
  updateModelValue();
  emit("price-selected");
};

// Watch for custom input changes and apply filter immediately
watch([customMinPrice, customMaxPrice], () => {
  handleCustomPriceInput();
}, { deep: true });

// Initialize from props
watch(() => [props.lowerPrice, props.upperPrice], ([newLower, newUpper]) => {
  minPrice.value = newLower || null;
  maxPrice.value = newUpper || null;
  
  // Update custom input fields
  customMinPrice.value = newLower ? newLower.toString() : "";
  customMaxPrice.value = newUpper ? newUpper.toString() : "";
}, { immediate: true });
</script>

<template>
  <div class="flex flex-col w-full">
    <!-- Selected price display area -->
    <div class="flex items-center w-full">
      <div 
        class="flex-1 border border-gray-300 rounded-md rounded-r-none bg-white min-h-[32px] md:min-h-[36px] max-h-[36px] md:max-h-[36px] overflow-y-auto"
      >
        <div class="grid grid-cols-2 gap-1 p-1 md:p-1.5">
          <div v-if="!selectedRange" class="col-span-2 text-gray-500 text-xs md:text-sm py-0.5">
            Price Range
          </div>
          <div v-if="selectedRange" class="col-span-2 itbms-price-item bg-blue-100 text-blue-800 text-xs rounded-full px-1.5 py-0.5 flex items-center shadow-sm border border-blue-200 transition-colors hover:bg-blue-200 min-w-0">
            <span class="truncate flex-1">{{ selectedRange }}</span>
            <button @click="clearPriceRange" class="itbms-price-item-clear ml-1 text-blue-600 hover:text-red-600 transition-colors flex-shrink-0">
              <X class="w-3 h-3" />
            </button>
          </div>
        </div>
      </div>

      <!-- Action buttons -->
      <div class="flex-shrink-0 flex">
        <button @click="toggleDropdown" class="itbms-price-filter px-2 md:px-3 py-1.5 bg-gray-500 border border-gray-500 hover:bg-gray-400 transition rounded-md w-[42px] md:w-[48px] h-[32px] md:h-[36px] rounded-l-none flex items-center justify-center">
          <span class="text-white text-sm md:text-base">₿</span>
        </button>
      </div>
    </div>

    <!-- Dropdown menu -->
    <div class="relative w-full">
      <div v-if="showDropdown" class="absolute left-0 top-full mt-1 w-full z-50">
        <div class="bg-white border border-gray-300 rounded-md shadow-lg p-4 md:w-[36vh]">
          <!-- Pre-defined ranges -->
          <div>
            <label class="block text-sm font-medium text-gray-700 mb-2 text-center">Price Options</label>
            <div class="space-y-1">
              <button
                v-for="range in priceRanges"
                :key="range.label"
                @click="selectPriceRange(range)"
                :class="[
                  'w-full text-left px-3 py-2 text-sm rounded-md transition-colors border-b-1 border-gray-200',
                  minPrice === range.min && maxPrice === range.max 
                    ? 'bg-blue-50 text-blue-700 border border-blue-200' 
                    : 'hover:bg-gray-100'
                ]"
              >
                <span class="flex items-center justify-start ">
                  {{ range.label }}
                  <span v-if="minPrice === range.min && maxPrice === range.max" class="text-blue-600">✓</span>
                </span>
              </button>
            </div>
          </div>
          <div class="mt-2">
            <div class="flex items-center gap-2">
              <input
                v-model="customMinPrice"
                type="number"
                placeholder="Min "
                class=" itbms-price-item-min flex-1 px-3 py-2 text-sm md:w-[4rem] border border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                min="0"
              />
              - 
              <input
                v-model="customMaxPrice"
                type="number"
                placeholder="Max "
                class="itbms-price-item-max flex-1 px-3 py-2  text-sm border md:w-[4rem] border-gray-300 rounded-md focus:ring-2 focus:ring-blue-500 focus:border-blue-500"
                min="0"
              />
              <span class="text-sm text-gray-600">Baht</span>
            </div>
          </div>
        </div>
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