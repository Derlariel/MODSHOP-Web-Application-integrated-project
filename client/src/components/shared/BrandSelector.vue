<script setup>
import { X, Filter } from "lucide-vue-next";
import { ref, defineProps, defineEmits } from "vue";

const props = defineProps({
  // Data props
  modelValue: {
    type: [Array, Object],
    required: true
  },
  brands: {
    type: Array,
    required: true
  },
  // Display mode
  multiple: {
    type: Boolean,
    default: false
  },
  // Styling props
  dark: {
    type: Boolean,
    default: false
  },
  error: {
    type: String,
    default: null
  }
});

const emit = defineEmits(['update:modelValue', 'brand-selected', 'brand-removed', 'clear-brands']);

// For dropdown in multiple mode
const showDropdown = ref(false);

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};

const selectBrand = (brand) => {
  if (props.multiple) {
    // Multiple selection - emit update with array
    if (!props.modelValue.includes(brand)) {
      const updatedBrands = [...props.modelValue, brand];
      emit('update:modelValue', updatedBrands);
      emit('brand-selected', brand);
    }
  } else {
    // Single selection - emit update with object
    emit('update:modelValue', brand);
    emit('brand-selected', brand);
    showDropdown.value = false; // Close dropdown after selection
  }
};

const removeBrand = (brand) => {
  if (props.multiple) {
    const updatedBrands = props.modelValue.filter(b => b !== brand);
    emit('update:modelValue', updatedBrands);
    emit('brand-removed', brand);
  }
};

const clearBrands = () => {
  if (props.multiple) {
    emit('update:modelValue', []);
    emit('clear-brands');
  }
};
</script>
 
<template>
  <!-- Multiple selection mode (for FilterSort) -->
  <div v-if="multiple" class="relative flex flex-wrap items-center max-w-full sm:max-w-2xl w-full">
    <!-- Selected Brands -->
    <div class="flex flex-wrap items-center content-center flex-1 border border-gray-300 rounded-md rounded-r-none bg-white min-h-[42px] px-2">
      <div v-for="brand in modelValue" :key="brand"
           class="bg-gray-200 text-sm rounded-full px-3 py-1 mr-2 flex items-center shadow-sm">
        {{ brand }}
        <button @click="removeBrand(brand)" class="itbms-filter-item-clear ml-2 text-gray-500 hover:text-red-500 transition">
          <X class="w-3 h-3" />
        </button>
      </div>
    </div>

    <div class="relative flex-shrink-0 flex">
      <button @click="toggleDropdown"
              class="itbms-brand-filter px-4 py-2 bg-gray-300 border border-gray-300 hover:bg-gray-400 transition rounded-none">
        <Filter class="w-5 h-5" />
      </button>

      <button @click="clearBrands"
              class="itbms-brand-filter-clear px-4 py-2 bg-gray-300 border border-gray-300 rounded-r-md hover:bg-red-400 transition">
        Clear
      </button>

      <ul v-if="showDropdown"
          class="absolute top-full right-0 bg-white border border-gray-300 rounded-md shadow-lg mt-1 z-50 max-h-200 overflow-y-auto w-[200px] sm:w-[300px]">
        <li v-for="brand in brands" :key="brand" @click="selectBrand(brand)"
            class="itbms-filter-item px-4 py-2 hover:bg-gray-100 cursor-pointer text-sm">
          {{ typeof brand === 'object' ? brand.name : brand }}
        </li>
      </ul>
    </div>
  </div>

  <!-- Single selection mode (for ProductForm) -->
  <div v-else class="relative">
    <div class="relative" @click="showDropdown = !showDropdown">
      <div
        :class="[
          'w-full px-4 py-3.5 rounded-xl border focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all cursor-pointer flex items-center justify-between',
          dark ? 'bg-neutral-800 text-white border-neutral-700' : 'bg-white text-gray-700 border-gray-300',
          error ? 'border-red-700' : dark ? 'border-neutral-700' : 'border-gray-300',
        ]"
      >
        <span v-if="!modelValue?.id" class="text-gray-400">Select a brand</span>
        <span v-else>{{ modelValue.name }}</span>
        
        <svg
          class="h-5 w-5 text-gray-400"
          xmlns="http://www.w3.org/2000/svg"
          viewBox="0 0 20 20"
          fill="currentColor"
        >
          <path
            fill-rule="evenodd"
            d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z"
            clip-rule="evenodd"
          />
        </svg>
      </div>
    </div>
    
    <ul
      v-if="showDropdown"
      class="absolute z-50 w-full mt-1 rounded-md shadow-lg max-h-60 overflow-y-auto"
      :class="dark ? 'bg-neutral-800 border border-neutral-700' : 'bg-white border border-gray-300'"
    >
      <li
        v-for="brand in brands"
        :key="brand.id"
        @click="selectBrand(brand)"
        :class="[
          'px-4 py-2 cursor-pointer text-sm hover:bg-gray-100 dark:hover:bg-neutral-700',
          dark ? 'text-white' : 'text-gray-700'
        ]"
      >
        {{ brand.name }}
      </li>
    </ul>
  </div>
</template>