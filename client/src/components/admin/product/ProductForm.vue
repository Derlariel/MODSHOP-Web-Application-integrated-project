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
import BaseInput from "@/components/shared/BaseInput.vue";

const router = useRouter();
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
  id: null,
  model: "",
  brand: { id: null, name: null },
  description: "",
  price: "",
  ramGb: "",
  storageGb: "",
  screenSizeInch: "",
  color: "",
  quantity: "",
});

const btnNotAvailable = ref(true);
const isLoading = ref(true);
const errorMessage = ref(null);

watch(
  () => props.init,
  async (newValue) => {
    try {
      await brandStore.loadBrands();
      const availableBrands = brandStore.getBrands();

      console.log("props.init:", newValue);
      console.log("availableBrands:", availableBrands);

      Object.assign(temp, {
        id: newValue.id || null,
        model: newValue.model || "",
        description: newValue.description || "",
        price: newValue.price?.toString() || "",
        ramGb: newValue.ramGb?.toString() || "",
        storageGb: newValue.storageGb?.toString() || "",
        screenSizeInch: newValue.screenSizeInch?.toString() || "",
        color: newValue.color || "",
        quantity: newValue.quantity?.toString() || "",
      });

      const selectedBrand = availableBrands.find(
        (brand) => brand.name === newValue.brandName
      );

      temp.brand = selectedBrand || {
        id: null,
        name: newValue.brandName || null,
      };

      console.log("selectedBrand:", selectedBrand);
      console.log("temp.brand:", temp.brand);

      isLoading.value = false;
    } catch (error) {
      console.error("Error loading brands:", error);
      errorMessage.value = "Failed to load brands";
      isLoading.value = false;
    }
  },
  { immediate: true }
);

watchEffect(() => {
  const requiredFields = ["model", "price", "quantity", "description"];
  const allFields = [
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
    const value = temp[field];
    return value == null || value.toString().trim() === "";
  });

  if (temp.ramGb === "") temp.ramGb = null;
  if (temp.storageGb === "") temp.storageGb = null;
  if (temp.screenSizeInch === "") temp.screenSizeInch = null;
  if (temp.quantity === "") temp.quantity = null;
  if (temp.price === "") temp.price = null;


  const isNewItem = Object.keys(props.init).length === 0;
  let isUnchanged = false;

  if (!isNewItem) {
    isUnchanged =
      allFields.every((field) => {
        const tempValue = temp[field] === "" ? null : temp[field];
        const initValue =
          props.init[field] === undefined ? null : props.init[field];
        if (
          ["price", "ramGb", "storageGb", "screenSizeInch", "quantity"].includes(
            field
          )
        ) {
          return Number(tempValue) === Number(initValue);
        }
        return tempValue === initValue;
      }) && temp.brand.name === (props.init.brandName || null);
  }

  btnNotAvailable.value = requiredFieldsEmpty || (!isNewItem && isUnchanged) || !temp.brand.id;

});

const submit = () => {
  if (btnNotAvailable.value) {
    console.warn("Submit blocked: Button is disabled");
    return;
  }
  if (!temp.brand.id) {
    console.warn("Submit blocked: No brand selected");
    alert("Please select a brand.");
    return;
  }

  emit("submit", {
    ...temp,
    price: temp.price ? Number(temp.price) : null,
    ramGb: temp.ramGb ? Number(temp.ramGb) : null,
    storageGb: temp.storageGb ? Number(temp.storageGb) : null,
    screenSizeInch: temp.screenSizeInch ? Number(temp.screenSizeInch) : null,
    quantity: temp.quantity ? Number(temp.quantity) : null,
  });
};

const trimField = (field) => {
  if (typeof temp[field] === "string") {
    temp[field] = temp[field].trim() || "";
  }
};

onMounted(() => {
  brandStore.loadBrands();
});
</script>

<template>
  <div>
    <div v-if="errorMessage" class="text-red-500 text-center mb-4">
      {{ errorMessage }}
    </div>
    <div v-else-if="!isLoading">
      <form @submit.prevent class="space-y-8">
        <div class="space-y-3">
          <label for="brand" class="block text-sm font-medium text-gray-300"
            >Brand</label
          >
          <div class="relative">
            <select
              v-model="temp.brand"
              id="brand"
              class="itbms-brand w-full px-4 py-3.5 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white appearance-none"
              required
            >
              <option :value="{ id: null, name: null }" disabled>
                Select a brand
              </option>
              <option v-for="brand in brands" :key="brand.id" :value="brand">
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
          required
        />

        <BaseInput
          v-model="temp.price"
          cypress="itbms-price"
          placeholder="0.00"
          type="number"
          step="0.01"
          label="Price (Baht)"
          prefix="฿"
          required
        />

        <div class="space-y-3">
          <label
            for="description"
            class="block text-sm font-medium text-gray-300"
            >Description</label
          >
          <textarea
            @blur="trimField('description')"
            v-model="temp.description"
            id="description"
            rows="4"
            class="itbms-description w-full px-4 py-3.5 rounded-xl border border-neutral-700 focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white"
            placeholder="Enter product description"
            required
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
            type="number"
            required
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
            class="itbms-cancel-button flex-1 bg-neutral-800 text-white py-4 px-6 rounded-full hover:bg-neutral-700 transition-colors duration-300 font-medium"
          >
            Cancel
          </button>
        </div>
        <p class="text-center text-gray-500 text-sm">
          All changes will be saved to the inventory system
        </p>
      </form>
    </div>
    <div v-else class="text-center text-gray-400">Loading product data...</div>
  </div>
</template>

<style scoped></style>