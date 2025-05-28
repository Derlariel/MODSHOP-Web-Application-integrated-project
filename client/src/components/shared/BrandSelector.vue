<script setup>
import { X, Filter } from "lucide-vue-next";
import { ref, defineProps, defineEmits, computed } from "vue";

const props = defineProps({
  modelValue: {
    type: [Array, Object],
    required: true,
  },
  brands: {
    type: Array,
    required: true,
  },
  multiple: {
    type: Boolean,
    default: false,
  },
  dark: {
    type: Boolean,
    default: false,
  },
  error: {
    type: String,
    default: null,
  },
});

const emit = defineEmits([
  "update:modelValue",
  "brand-selected",
  "brand-removed",
  "clear-brands",
]);

const showDropdown = ref(false);

// Computed property to get the current selected value for single selection
const selectedValue = computed(() => {
  if (props.multiple) return null;
  return props.modelValue?.id || "";
});

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};

const selectBrand = (brand) => {
  if (props.multiple) {
    if (!props.modelValue.includes(brand)) {
      const updatedBrands = [...props.modelValue, brand];
      emit("update:modelValue", updatedBrands);
      emit("brand-selected", brand);
    }
  } else {
    emit("update:modelValue", brand);
    emit("brand-selected", brand);
    showDropdown.value = false;
  }
};

const removeBrand = (brand) => {
  if (props.multiple) {
    const updatedBrands = props.modelValue.filter((b) => b !== brand);
    emit("update:modelValue", updatedBrands);
    emit("brand-removed", brand);
  }
};

const clearBrands = () => {
  if (props.multiple) {
    emit("update:modelValue", []);
    emit("clear-brands");
  }
};

const handleSelect = (event) => {
  const selectedId = parseInt(event.target.value);
  if (!selectedId) {
    // Handle empty selection
    emit("update:modelValue", { id: null, name: null });
    return;
  }
  
  const selectedBrand = props.brands.find((b) => b.id === selectedId);
  if (selectedBrand) {
    emit("update:modelValue", selectedBrand);
    emit("brand-selected", selectedBrand);
  }
};
</script>

<template>
  <!-- Multiple selection mode -->
  <div
    v-if="multiple"
    class="flex flex-col max-w-full sm:max-w-2xl w-full"
  >
    <!-- Selected brands display area -->
    <div class="flex items-center w-full">
      <div
        class="flex flex-wrap items-center content-center flex-1 border border-gray-300 rounded-md rounded-r-none bg-white min-h-[42px] px-2 py-1 gap-1"
      >
        <div
          v-if="modelValue.length === 0"
          class="text-gray-500 text-sm py-1"
        >
          Select brands...
        </div>
        <div
          v-for="brand in modelValue"
          :key="brand"
          class="bg-blue-100 text-blue-800 text-sm rounded-full px-3 py-1 flex items-center shadow-sm border border-blue-200 transition-colors hover:bg-blue-200"
        >
          {{ brand }}
          <button
            @click="removeBrand(brand)"
            class="itbms-filter-item-clear ml-2 text-blue-600 hover:text-red-600 transition-colors"
          >
            <X class="w-3 h-3" />
          </button>
        </div>
      </div>

      <!-- Action buttons -->
      <div class="flex-shrink-0 flex">
        <button
          @click="toggleDropdown"
          class="itbms-brand-filter px-4 py-2 bg-gray-500 border border-gray-500 hover:bg-gray-400 transition rounded-none h-[42px]"
        >
          <Filter class="w-5 h-5" />
          <span class="sr-only">
            <span v-for="brand in modelValue" :key="brand">{{ brand }}</span>
          </span>
        </button>

        <button
          @click="clearBrands"
          class="itbms-brand-filter-clear px-4 py-2 bg-gray-500 border border-gray-500 rounded-r-md hover:bg-red-400 transition h-[42px]"
        >
          Clear
        </button>
      </div>
    </div>

    <!-- Dropdown menu -->
    <div class="relative w-full">
      <div v-if="showDropdown" class="absolute left-0 top-full mt-1 w-full z-50">
        <ul
          class="bg-white border border-gray-300 rounded-md shadow-lg max-h-48 overflow-y-auto w-full"
        >
          <li
            v-for="brand in brands"
            :key="brand"
            @click="selectBrand(brand)"
            class="itbms-filter-item px-4 py-2 hover:bg-gray-100 cursor-pointer text-sm border-b border-gray-100 last:border-b-0 transition-colors"
            :class="{ 'bg-blue-50 text-blue-700': modelValue.includes(brand) }"
          >
            <span class="flex items-center justify-between">
              {{ typeof brand === "object" ? brand.name : brand }}
              <span v-if="modelValue.includes(brand)" class="text-blue-600">✓</span>
            </span>
          </li>
        </ul>
      </div>
    </div>
  </div>

  <!-- Single selection mode -->
  <select
    @blur="$emit('blur')"
    v-if="!multiple"
    :value="selectedValue"
    class="itbms-brand w-full px-4 py-3.5 rounded-xl border focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all cursor-pointer"
    :class="[
      dark
        ? 'bg-neutral-800 text-white border-neutral-700'
        : 'bg-white text-gray-700 border-gray-300',
      error
        ? 'border-red-700'
        : dark
        ? 'border-neutral-700'
        : 'border-gray-300',
    ]"
    @change="handleSelect"
  >
    <option value="">Select a brand</option>
    <option v-for="brand in brands" :key="brand.id" :value="brand.id">
      {{ brand.name }}
    </option>
  </select>

</template>

<style scoped>
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}
</style>