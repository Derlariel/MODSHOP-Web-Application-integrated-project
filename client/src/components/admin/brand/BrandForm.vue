<script setup>
import { reactive, computed, watch, ref } from "vue"; 
import BaseInput from "@/components/shared/BaseInput.vue";
import { validateBrandName , runValidation , validateBrandOrigin , validateBrandURL } from "@/utils/validate.js"; 
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

const isError = ref(false)

const formData = reactive({
  name: "",
  websiteUrl: "",
  countryOfOrigin: "",
  isActive: true,
});


const originalData = ref({});

watch(
  () => props.initialData,
  (newValue) => {
    Object.assign(formData, newValue);
    if (props.mode === "edit") {
      originalData.value = JSON.parse(JSON.stringify(newValue));
    }
  },
  { immediate: true, deep: true }
);

// const validateName = () => {
//   if (!formData.name.trim()) {
//     errors.name = "Brand name is required";
//     return false;
//   }
//   errors.name = "";
//   return true;
// };

const hasFormChanges = computed(() => {
  if (props.mode !== "edit") return true;

  if (!originalData.value || Object.keys(originalData.value).length === 0) {
    return false;
  }
  const nameChanged =
    formData.name.trim() !== (originalData.value.name || "").trim();
  const websiteChanged =
    formData.websiteUrl.trim() !== (originalData.value.websiteUrl || "").trim();
  const countryChanged =
    formData.countryOfOrigin.trim() !==
    (originalData.value.countryOfOrigin || "").trim();
  const activeChanged = formData.isActive !== originalData.value.isActive;

  return nameChanged || websiteChanged || countryChanged || activeChanged;
});

const isFormValid = computed(() => {
  if (props.mode === "create") {
    return formData.name.trim().length > 0;
  }

  return formData.name.trim().length > 0 && hasFormChanges.value;
});

const errors = reactive({
  name: null,
  websiteUrl: null,
  countryOfOrigin: null
});

const handleSubmit = () => {
  if (isFormValid.value && !isError.value) { 
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

const trimField = (field) => {
  console.log(field);
  if (typeof formData[field] === "string") {
    formData[field] = formData[field].trim() || "";
  }

  console.log(field);

  validateField(field);
};


const validateField = (field) => {
  let result = { valid: true, message: null };
  if (field === "name") {
    result = runValidation(formData.name, [validateBrandName]);
  } else if (field === "websiteUrl") {
    result = runValidation(formData.websiteUrl, [validateBrandURL]);
  } else if (field === "countryOfOrigin") {
    result = runValidation(formData.countryOfOrigin, [validateBrandOrigin]);
  } 
  errors[field] = result.message;
  return result.valid;
};

watch(formData,() => {

  isError.value = Object.values(errors).some(error => error !== null)

}, {deep: true, immediate: true})

</script>

<template>
  <form @submit.prevent="handleSubmit" class="space-y-6">
    <BaseInput
      cypress="itbms-name"
      v-model="formData.name"
      label="Brand Name *"
      id="brand-name"
      placeholder="Enter brand name"
      inputClass="itbms-name"
      :error="errors.name"
      @trim="trimField('name')"
    />

    <BaseInput
      cypress="itbms-websiteUrl"
      v-model="formData.websiteUrl"
      label="Website URL"
      id="website-url"
      placeholder="https://www.example.com"
      inputClass="itbms-websiteUrl"
      @trim="trimField('websiteUrl')"
      :error="errors.websiteUrl"
    />
    <BaseInput
      cypress="itbms-countryOfOrigin"
      v-model="formData.countryOfOrigin"
      label="Country of Origin"
      id="country"
      placeholder="Enter country of origin"
      inputClass="itbms-countryOfOrigin"
      @trim="trimField('countryOfOrigin')"
      :error="errors.countryOfOrigin"
    />

    <div class="flex items-center space-x-3">
      <div class="relative inline-flex h-6 w-11 flex-shrink-0">
        <input
          type="checkbox"
          v-model="formData.isActive"
          class="itbms-isActive absolute opacity-0 w-full h-full left-0 top-0 z-10 cursor-pointer"
          tabindex="-1"
        />
        <!-- Visual toggle button -->
        <button
          type="button"
          @click="formData.isActive = !formData.isActive"
          class="relative inline-flex h-6 w-11 flex-shrink-0 cursor-pointer rounded-full border-2 border-transparent transition-colors duration-200 ease-in-out focus:outline-none focus:ring-2 focus:ring-white focus:ring-offset-2 focus:ring-offset-neutral-800"
          :class="formData.isActive ? 'bg-blue-600' : 'bg-neutral-700'"
          role="switch"
          :aria-checked="formData.isActive"
          style="z-index: 1"
        >
          <span class="sr-only">Active Brand</span>
          <span
            aria-hidden="true"
            class="pointer-events-none inline-block h-5 w-5 transform rounded-full bg-white shadow ring-0 transition duration-200 ease-in-out"
            :class="formData.isActive ? 'translate-x-5' : 'translate-x-0'"
          ></span>
        </button>
      </div>
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
        class="itbms-save-button px-6 py-2.5 rounded transition-colors duration-200 font-medium"
        :disabled="!isFormValid || isError" 
        :class="[
          isFormValid && !isError 
            ? 'bg-white text-black hover:bg-gray-100' 
            : 'bg-white/60 text-black hover:text-white  hover:bg-red-500/80 cursor-not-allowed'
        ]"
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
