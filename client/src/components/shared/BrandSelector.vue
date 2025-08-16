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

const selectedValue = computed(() => {
  if (props.multiple) return null;
  return props.modelValue?.id || "";
});

const toggleDropdown = () => {
  showDropdown.value = !showDropdown.value;
};

const selectBrand = (brand) => {
  if (props.multiple) {
    // Toggle functionality - if brand is already selected, remove it
    if (props.modelValue.includes(brand)) {
      const updatedBrands = props.modelValue.filter(b => b !== brand);
      emit("update:modelValue", updatedBrands);
      emit("brand-removed", brand);
    } else {
      const updatedBrands = [brand, ...props.modelValue];
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
    class="flex flex-col w-full"
  >
    <!-- Selected brands display area -->
    <div class="flex items-center w-full">
      <div
        class="flex-1 rounded-md rounded-r-none bg-white min-h-[32px] md:min-h-[36px] max-h-[36px] md:max-h-[36px] overflow-y-auto border border-gray-300"
      >
        <div 
          class="grid gap-1 p-1 md:p-1.5"
          :class="modelValue.length === 1 ? 'grid-cols-1' : 'grid-cols-2'"
        >
          <div
            v-if="modelValue.length === 0"
            class="col-span-2 text-gray-500 text-xs md:text-sm py-0.5"
          >
            Brands
          </div>
          <div
            v-for="brand in modelValue"
            :key="brand"
            class="bg-blue-100 text-blue-800 text-xs rounded-full px-1.5 py-0.5 flex items-center shadow-sm border border-blue-200 transition-colors hover:bg-blue-200 min-w-0"
          >
            <span class="truncate flex-1">{{ brand}}</span>
            <button
              @click="removeBrand(brand)"
              class="itbms-filter-item-clear ml-1 text-blue-600 hover:text-red-600 transition-colors flex-shrink-0"
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
          class="itbms-brand-filter px-2 md:px-3 py-1.5 bg-gray-500 border border-gray-500 hover:bg-gray-400 transition rounded-md rounded-l-none h-[32px] md:h-[36px] w-[42px] md:w-[48px] flex items-center justify-center"
        >
          <Filter stroke="white" class="w-3 h-3 md:w-4 md:h-4" />
          <span class="sr-only">
            <span v-for="brand in modelValue" :key="brand">{{ brand }}</span>
          </span>
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
            <span class="flex items-center  justify-between">
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

/* Hide scrollbars */
.overflow-y-auto::-webkit-scrollbar {
  display: none;
}

.overflow-y-auto {
  -ms-overflow-style: none;
  scrollbar-width: none;
}

.overflow-x-auto::-webkit-scrollbar {
  display: none;
}

.overflow-x-auto {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>