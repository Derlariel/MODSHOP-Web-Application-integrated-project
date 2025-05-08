<script setup>
import { onMounted, computed, defineEmits, defineProps, reactive, watch, watchEffect, ref } from "vue";
import { useBrandStore } from "@/stores/useBrandStore";
import BaseInput from "../shared/BaseInput.vue";
const brandStore = useBrandStore();
const brands = computed(() => brandStore.getBrands());

const emit = defineEmits(["submit", "cancel"]);

const props = defineProps({
  init: {
    type: Object,
    default: () => ({}),
  },
});

const temp = reactive({
  model: "",
  brand: {
    id: "",
    name: "",
  },
  description: "",
  price: "",
  ramGb: "",
  storageGb: "",
  screenSizeInch: "",
  color: "",
  quantity: ""
});

const btnNotAvailable = ref(false)

watch(() => props.init, (newValue) => {
  Object.assign(temp, newValue)
}, { immediate: true })

watchEffect(() => {
  if (!temp.brand.name || !temp.price || !temp.quantity || !temp.model || !temp.description) {
    btnNotAvailable.value = true
  } else {
    btnNotAvailable.value = false
  }
})

const submit = () => {
  if (!temp.brand.name || !temp.price || !temp.quantity || !temp.model || !temp.description) {
    return;
  } 
};


onMounted(() => {
  brandStore.loadBrands();

});
</script>

<template>
  <div>
    <form @submit.prevent class="space-y-8">
      <div class="space-y-3">
        <label for="brand" class="block text-sm font-medium text-gray-300"
          >Brand</label
        >
        <div class="relative">
          <select
            v-model="temp.brand.name"
            id="brand"
            class="w-full px-4 py-3.5 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white appearance-none"
          >
            <option value="" disabled selected>Select a brand</option>
            <option
              v-for="(brand, index) in brands"
              :key="index"
              :value="brand.name"
            >
              {{ brand.name }}
            </option>
          </select>
          <div
            class="absolute inset-y-0 right-0 flex items-center pr-3 pointer-events-none"
          >
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
      </div>

      <BaseInput
        v-model="temp.model"
        class=""
        placeholder="IPhone 15"
        label="Model"
      />

      <BaseInput
      v-model="temp.price"
        class=""
        placeholder="0.00"
        label="Price (Baht)"
        type="number"
        prefix="฿"
      />

      <div class="space-y-3">
        <label for="description" class="block text-sm font-medium text-gray-300"
          >Description</label
        >
        <textarea
          v-model="temp.description"
          id="description"
          rows="4"
          class="w-full px-4 py-3.5 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white"
          placeholder="Enter product description"
        ></textarea>
      </div>

      <div class="pt-2">
        <h3 class="text-lg font-medium text-white mb-4">
          Technical Specifications
        </h3>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
          <BaseInput v-model="temp.ramGb" label="RAM (GB)" placeholder="6" id="ram" />
          <BaseInput
          v-model="temp.screenSizeInch"
            label="Screen Size (Inches)"
            placeholder="6.1"
            id="screenSize"
          />
          <BaseInput v-model="temp.storageGb" label="Storage (GB)" placeholder="128" id="storage" />
          <BaseInput v-model="temp.color" label="Color" placeholder="Black" id="color" />
        </div>
      </div>

      <div class="space-y-3 pt-6 border-t border-neutral-800">
        <h3 class="text-lg font-medium text-white mb-4">Inventory</h3>
        <div class="space-y-3">
          <label for="quantity" class="block text-sm font-medium text-gray-300"
            >Quantity</label
          >
          <div class="relative">
            <input
              v-model="temp.quantity"
              type="number"
              id="quantity"
              min="1"
              class="w-full px-4 py-3.5 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white"
              placeholder="10"
            />
            <div class="absolute inset-y-0 right-0 flex">
              <button
                type="button"
                class="px-4 border-l border-neutral-700 text-gray-400 hover:bg-neutral-700 transition-colors rounded-r-xl"
              >
                -
              </button>
              <button
                type="button"
                class="px-4 border-l border-neutral-700 text-gray-400 hover:bg-neutral-700 transition-colors rounded-r-xl"
              >
                +
              </button>
            </div>
          </div>
        </div>
      </div>

      <div class="pt-8 flex flex-col sm:flex-row gap-4">
        <button
          type="submit"
          @click="submit"
          :class="btnNotAvailable
          ? 'flex-1 bg-red-400/20 text-white py-4 px-6 rounded-full hover:bg-neutral-700 transition-colors duration-300 font-medium' 
          : 'flex-1 bg-white text-black py-4 px-6 rounded-full hover:bg-gray-200 transition-colors duration-300 font-medium'"
        >
          Save Product
        </button>
        <button
          type="button"
          :class="btnNotAvailable
          ? 'flex-1 bg-neutral-800 text-white py-4 px-6 rounded-full hover:bg-neutral-700 transition-colors duration-300 font-medium' 
          : 'flex-1 bg-neutral-800 text-white py-4 px-6 rounded-full hover:bg-neutral-700 transition-colors duration-300 font-medium'"
        >
          Cancel
        </button>
      </div>
      <p class="text-center text-gray-500 text-sm">
        All changes will be saved to the inventory system
      </p>
    </form>
  </div>
</template>

<style scoped></style>
