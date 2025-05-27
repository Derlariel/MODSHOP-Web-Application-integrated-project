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
import BrandSelector from "@/components/shared/BrandSelector.vue";

import {
  runValidation,
  validateModel,
  validateBrandSelected,
  validateDescription,
  validatePrice,
  validateQuantity,
  validateRamSize,
  validateStorageSize,
  validateScreenSize,
  validateColor,
} from "@/utils/validate.js";

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

      // Find the brand first
      let selectedBrand = null;
      if (newValue.brandName) {
        selectedBrand = availableBrands.find(
          (brand) => brand.name === newValue.brandName
        );
        
        if (!selectedBrand) {
          console.warn(`Brand "${newValue.brandName}" not found in available brands`);
          errorMessage.value = `Brand "${newValue.brandName}" is not available`;
        }
      }

      // Use Object.assign to update all properties at once
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
        // Fix: Ensure brand object structure is correct
        brand: selectedBrand ? { id: selectedBrand.id, name: selectedBrand.name } : { id: null, name: null },
      });

      console.log("temp after assignment:", temp);
      console.log("temp.brand:", temp.brand);
      
      isLoading.value = false;
    } catch (error) {
      console.error("Error loading brands:", error);
      errorMessage.value = "Failed to load brands";
      isLoading.value = false;
    }
  },
  { immediate: true, deep: true }
);

const notPass = ref(false);

const errors = reactive({
  model: null,
  brand: null,
  description: null,
  price: null,
  quantity: null,
  ramGb: null,
  storageGb: null,
  screenSizeInch: null,
  color: null,
});



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
    return value == null || value.toString().trim() === null;
  });

  // Clean up empty string values
  if (temp.ramGb === "") temp.ramGb = null;
  if (temp.storageGb === "") temp.storageGb = null;
  if (temp.screenSizeInch === "") temp.screenSizeInch = null;
  if (temp.quantity === "") temp.quantity = null;
  if (temp.price === "") temp.price = null;

  const isNewItem = Object.keys(props.init).length === 0;
  let isUnchanged = false;

  notPass.value = Object.values(errors).some((value) => value !== null );

  if (!isNewItem) {
    isUnchanged =
      allFields.every((field) => {
        const tempValue = temp[field] === "" ? null : temp[field];
        const initValue =
          props.init[field] === undefined ? null : props.init[field];
        if (
          [
            "price",
            "ramGb",
            "storageGb",
            "screenSizeInch",
            "quantity",
          ].includes(field)
        ) {
          return Number(tempValue) === Number(initValue);
        }
        return tempValue === initValue;
      }) && temp.brand.name === (props.init.brandName || null);
  }

  btnNotAvailable.value =
    requiredFieldsEmpty || (!isNewItem && isUnchanged) || !temp.brand.id;
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

  console.log(errors.model)

  emit("submit", {
    ...temp,
    price: temp.price != null ? Number(temp.price) : null,
    ramGb: temp.ramGb != null  ? Number(temp.ramGb) : null,
    storageGb: temp.storageGb != null  ? Number(temp.storageGb) : null,
    screenSizeInch: temp.screenSizeInch != null  ? Number(temp.screenSizeInch) : null,
    quantity: temp.quantity != null  ? Number(temp.quantity) : null,
  });
};

const validateField = (field) => {
  let result = { valid: true, message: null };
  if (field === "model") {
    result = runValidation(temp.model, [validateModel]);
  } else if (field === "description") {
    result = runValidation(temp.description, [validateDescription]);
  } else if (field === "price") {
    result = runValidation(temp.price, [validatePrice]);
  } else if (field === "brand") {
    result = runValidation(temp.brand.name, [validateBrandSelected]);
  } else if (field === "quantity") {
    result = runValidation(temp.quantity, [validateQuantity]);
  } else if (field === "screenSizeInch") {
    result = runValidation(temp.screenSizeInch, [validateScreenSize]);
  } else if (field === "color") {
    result = runValidation(temp.color, [validateColor]);
  } else if (field === "storageGb") {
    result = runValidation(temp.storageGb, [validateStorageSize]);
  } else if (field === "ramGb") {
    result = runValidation(temp.ramGb, [validateRamSize]);
  }

  errors[field] = result.message;
  return result.valid;
};



const trimField = (field) => {
  if (typeof temp[field] === "string") {
    temp[field] = temp[field].trim() || "";
  }
  validateField(field);
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
          <BrandSelector
            @blur="trimField('brand')"
            v-model="temp.brand"
            :brands="brands"
            :multiple="false"
            :dark="true"
          />
          <p v-if="errors.brand" class="itbms-message text-sm text-red-500 mt-1">
            {{ errors.brand }}
          </p>
        </div>

        <BaseInput
          :error="errors.model"
          @trim="trimField('model')"
          :maxInput="60"
          v-model="temp.model"
          cypress="itbms-model"
          placeholder="IPhone 15"
          label="Model"
        />

        <BaseInput
          :error="errors.price"
          @trim="trimField('price')"
          v-model="temp.price"
          cypress="itbms-price"
          placeholder="0.00"
          type="number"
          step="0.01"
          label="Price (Baht)"
          prefix="฿"
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
            :class="[
              'itbms-description w-full px-4 py-3.5 rounded-xl border focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white',
              errors.description ? 'border-red-700' : 'border-neutral-700',
            ]"
            placeholder="Enter product description"
            required
          ></textarea>
          <p v-if="errors.description" class="itbms-message text-sm text-red-500 mt-1">
            {{ errors.description }}
          </p>
        </div>

        <div class="pt-2">
          <h3 class="text-lg font-medium text-white mb-4">
            Technical Specifications
          </h3>
          <div class="grid grid-cols-1 sm:grid-cols-2 gap-6">
            <BaseInput
              cypress="itbms-ramGb"
              @trim="trimField('ramGb')"
              v-model="temp.ramGb"
              label="RAM (GB)"
              type="number"
              placeholder="6"
              :error="errors.ramGb"
              id="ram"
            />
            <BaseInput
              :error="errors.screenSizeInch"
              @trim="trimField('screenSizeInch')"
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
              @trim="trimField('storageGb')"
              v-model="temp.storageGb"
              label="Storage (GB)"
              placeholder="128"
              type="number"
              :error="errors.storageGb"
              id="storage"
            />
            <BaseInput
              cypress="itbms-color"
              @trim="trimField('color')"
              v-model="temp.color"
              label="Color"
              placeholder="Black"
              :error="errors.color"
              id="color"
            />
          </div>
        </div>

        <div class="space-y-3 pt-6 border-t border-neutral-800">
          <h3 class="text-lg font-medium text-white mb-4">Inventory</h3>
          <BaseInput
            :error="errors.quantity"
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
            :disabled="notPass || btnNotAvailable"
            class="itbms-save-button"
            :class="
              notPass || btnNotAvailable
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