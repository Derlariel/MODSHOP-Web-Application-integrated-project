<script setup>
import { X, Filter } from "lucide-vue-next";
import { ref, defineProps, defineEmits } from "vue";

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
    class="relative flex flex-wrap items-center max-w-full sm:max-w-2xl w-full"
  >
    <div
      class="flex flex-wrap items-center content-center flex-1 border border-gray-300 rounded-md rounded-r-none bg-white min-h-[42px] px-2"
    >
      <div
        v-for="brand in modelValue"
        :key="brand"
        class="bg-gray-200 text-sm rounded-full px-3 py-1 mr-2 flex items-center shadow-sm"
      >
        {{ brand }}
        <button
          @click="removeBrand(brand)"
          class="itbms-filter-item-clear ml-2 text-gray-500 hover:text-red-500 transition"
        >
          <X class="w-3 h-3" />
        </button>
      </div>
    </div>

    <div class="relative flex-shrink-0 flex">
      <button
        @click="toggleDropdown"
        class="itbms-brand-filter px-4 py-2 bg-gray-300 border border-gray-300 hover:bg-gray-400 transition rounded-none"
      >
        <Filter class="w-5 h-5" />
        <span class="sr-only">
          <span v-for="brand in modelValue" :key="brand">{{ brand }}</span>
        </span>
      </button>

      <button
        @click="clearBrands"
        class="itbms-brand-filter-clear px-4 py-2 bg-gray-300 border border-gray-300 rounded-r-md hover:bg-red-400 transition"
      >
        Clear
      </button>

      <ul
        v-if="showDropdown"
        class="absolute top-full right-0 bg-white border border-gray-300 rounded-md shadow-lg mt-1 z-50 max-h-200 overflow-y-auto w-[200px] sm:w-[300px]"
      >
        <li
          v-for="brand in brands"
          :key="brand"
          @click="selectBrand(brand)"
          class="itbms-filter-item px-4 py-2 hover:bg-gray-100 cursor-pointer text-sm"
        >
          {{ typeof brand === "object" ? brand.name : brand }}
        </li>
      </ul>
    </div>
  </div>

  <select
    v-if="!multiple"
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
    <option disabled selected value="">Select a brand</option>
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