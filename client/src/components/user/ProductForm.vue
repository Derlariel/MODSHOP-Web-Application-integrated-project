<script setup>
import {
  onMounted,
  computed,
  defineEmits,
  defineProps,
  reactive,
  watch,
  watchEffect,
  ref,
} from "vue";
import { useBrandStore } from "@/stores/useBrandStore";
import { useRouter } from "vue-router";
import BaseInput from "../shared/BaseInput.vue";

const router = useRouter()
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
    name: "",
  },
  description: "",
  price: "",
  ramGb: "",
  storageGb: "",
  screenSizeInch: "",
  color: "",
  quantity: "",
});

const btnNotAvailable = ref(false);

watch(
  () => props.init,
  (newValue) => {
    Object.assign(temp, newValue);
    temp.brand.name = props.init.brandName;
  },
  { immediate: true }
);

watchEffect(() => {
  const requiredFields = ["model", "price", "quantity", "description"];

  const AllFields = [
    "model",
    "description",
    "price",
    "ramGb",
    "storageGb",
    "screenSizeInch",
    "color",
    "quantity",
  ];
  const requiredFieldsEmpty = requiredFields.some((field) => {
    return !temp[field];
  });

  if (temp.ramGb === "") temp.ramGb = null
  
  const isUnchanged =
    AllFields.every((field) => temp[field] === props.init[field]) &&
    temp.brand.name === props.init.brandName;

  btnNotAvailable.value = requiredFieldsEmpty || isUnchanged;
});

const submit = () => {
  if(btnNotAvailable.value === true) return
  emit("submit", temp);
};

const trimField = (field) => {
  temp[field] = temp[field].trim()
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
            class="itbms-brand w-full px-4 py-3.5 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white appearance-none"
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
        @trim="trimField('model')"
        :maxInput="60"
        v-model="temp.model"
        cypress="itbms-model"
        placeholder="IPhone 15"
        label="Model"
      />

      <BaseInput
        v-model="temp.price"
        cypress="itbms-price"
        placeholder="0.00"
        type="number"
        step="0.01"
        label="Price (Baht)"
        prefix="฿"
      />

      <div class="space-y-3">
        <label for="description" class="block text-sm font-medium text-gray-300"
          >Description</label
        >
        <textarea
          @blur="trimField('description')"
          v-model="temp.description"
          id="description"
          rows="4"
          class="itbms-description w-full px-4 py-3.5 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white"
          placeholder="Enter product description"
        ></textarea>
      </div>

      <div class="pt-2">
        <h3 class="text-lg font-medium text-white mb-4">
          Technical Specifications
        </h3>
        <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
          <BaseInput
            cypress="itbms-ramGb"
            v-model="temp.ramGb"
            label="RAM (GB)"
            type="number"
            placeholder="6"
            id="ram"
          />
          <BaseInput
            cypress="itbms-screenSizeInch"
            v-model="temp.screenSizeInch"
            label="Screen Size (Inches)"
            placeholder="6.1"
            step="0.01"
            type="number"
            id="screenSize"
          />
          <BaseInput
            cypress="itbms-storageGb"
            v-model="temp.storageGb"
            label="Storage (GB)"
            placeholder="128"
            type="number"
            id="storage"
          />
          <BaseInput
           cypress="itbms-color"
           @trim="trimField('color')"
            v-model="temp.color"
            label="Color"
            placeholder="Black"
            id="color"
          />
        </div>
      </div>

      <div class="space-y-3 pt-6 border-t border-neutral-800">
        <h3 class="text-lg font-medium text-white mb-4">Inventory</h3>
        <BaseInput
           cypress="itbms-quantity"
           @trim="trimField('quantity')"
            v-model="temp.quantity"
            label="Quantity"
            placeholder="10"
            id="color"
          />
      </div>

      <div class="pt-8 flex flex-col sm:flex-row gap-4">
        <button
          type="button"
          @click="submit"
          :disabled="btnNotAvailable"
          class="itbms-save-button"
          :class="
            btnNotAvailable
              ? 'flex-1 bg-red-400/20 text-white py-4 px-6 rounded-full hover:bg-neutral-700 transition-colors duration-300 font-medium'
              : 'flex-1 bg-white text-black py-4 px-6 rounded-full hover:bg-gray-200 transition-colors duration-300 font-medium'
          "
        >
          Save Product
        </button>
        <button
          @click="router.back()"
          type="button"
          class="itbms-cancel-button"
          :class="
            btnNotAvailable
              ? 'flex-1 bg-neutral-800 text-white py-4 px-6 rounded-full hover:bg-neutral-700 transition-colors duration-300 font-medium'
              : 'flex-1 bg-neutral-800 text-white py-4 px-6 rounded-full hover:bg-neutral-700 transition-colors duration-300 font-medium'
          "
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
