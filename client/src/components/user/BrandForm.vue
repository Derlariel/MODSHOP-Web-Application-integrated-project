<script setup>
import { reactive, computed, watch } from "vue";
import BaseInput from "../shared/BaseInput.vue";

const props = defineProps({
  initialData: {
    type: Object,
    default: () => ({
      name: "",
      websiteUrl: "",
      countryOfOrigin: "",
      isActive: true,
    }),
  },
  isSubmitting: {
    type: Boolean,
    default: false,
  },
  mode: {
    type: String,
    default: "create",
    validator: (value) => ["create", "edit"].includes(value),
  },
});

const emit = defineEmits(["submit", "cancel"]);

const formData = reactive({
  name: "",
  websiteUrl: "",
  countryOfOrigin: "",
  isActive: true,
});

watch(
  () => props.initialData,
  (newValue) => {
    Object.assign(formData, newValue);
  },
  { immediate: true, deep: true }
);

const errors = reactive({
  name: "",
});

const validateName = () => {
  if (!formData.name.trim()) {
    errors.name = "Brand name is required";
    return false;
  }
  errors.name = "";
  return true;
};

const isFormValid = computed(() => {
  return formData.name.trim() !== "";
});

const handleSubmit = () => {
  if (validateName()) {
    emit("submit", {
      name: formData.name.trim(),
      websiteUrl: formData.websiteUrl.trim(),
      countryOfOrigin: formData.countryOfOrigin.trim(),
      isActive: formData.isActive,
    });
  }
};

const handleCancel = () => {
  emit("cancel");
};
</script>

<template>
  <form @submit.prevent="handleSubmit" class="space-y-6">
    <BaseInput
      v-model="formData.name"
      label="Brand Name *"
      id="brand-name"
      placeholder="Enter brand name"
      cypress="itbms-name"
      class="itbms-name"
      :error="errors.name"
      @blur="validateName"
    />

    <BaseInput
      v-model="formData.websiteUrl"
      label="Website URL"
      id="website-url"
      placeholder="https://www.example.com"
      cypress="itbms-websiteUrl"
      class="itbms-websiteUrl"
    />
    <BaseInput
      v-model="formData.countryOfOrigin"
      label="Country of Origin"
      id="country"
      placeholder="Enter country of origin"
      cypress="itbms-countryOfOrigin"
      class="itbms-countryOfOrigin"
    />

    <div class="flex items-center space-x-3">
      <button
        type="button"
        @click="formData.isActive = !formData.isActive"
        class="itbms-isActive relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-neutral-800"
        :class="formData.isActive ? 'bg-blue-600' : 'bg-neutral-700'"
        role="switch"
        :aria-checked="formData.isActive"
      >
        <span class="sr-only">Active Brand</span>
        <span
          aria-hidden="true"
          class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out"
          :class="formData.isActive ? 'translate-x-5' : 'translate-x-0'"
        ></span>
      </button>
      <label class="text-sm font-medium text-gray-300">
        {{ formData.isActive ? "Active" : "Inactive" }} Brand
      </label>
    </div>

    <div class="flex justify-end space-x-4 pt-6">
      <button
        type="button"
        @click="handleCancel"
        class="itbms-cancel-button bg-neutral-800 text-white px-6 py-2.5 rounded hover:bg-neutral-700 transition-colors duration-200"
        :disabled="isSubmitting"
      >
        Cancel
      </button>
      <button
        type="submit"
        class="itbms-save-button bg-white text-black px-6 py-2.5 rounded hover:bg-gray-200 transition-colors duration-200 font-medium"
        :disabled="isSubmitting || !isFormValid"
      >
        {{
          isSubmitting
            ? "Saving..."
            : props.mode === "create"
            ? "Save"
            : "Update"
        }}
      </button>
    </div>
  </form>
</template>
