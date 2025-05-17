<script setup>
import { reactive, computed, watch } from 'vue';
import BaseInput from '../shared/BaseInput.vue';

const props = defineProps({
  initialData: {
    type: Object,
    default: () => ({
      name: '',
      websiteUrl: '',
      countryOfOrigin: '',
      isActive: true
    })
  },
  isSubmitting: {
    type: Boolean,
    default: false
  },
  mode: {
    type: String,
    default: 'create',
    validator: (value) => ['create', 'edit'].includes(value)
  }
});

const emit = defineEmits(['submit', 'cancel']);

const formData = reactive({
  name: '',
  websiteUrl: '',
  countryOfOrigin: '',
  isActive: true
});

watch(
  () => props.initialData,
  (newValue) => {
    Object.assign(formData, newValue);
  },
  { immediate: true, deep: true }
);

const errors = reactive({
  name: ''
});

const validateName = () => {
  if (!formData.name.trim()) {
    errors.name = 'Brand name is required';
    return false;
  }
  errors.name = '';
  return true;
};

const isFormValid = computed(() => {
  return formData.name.trim() !== '';
});

const handleSubmit = () => {
  if (validateName()) {
    emit('submit', {
      name: formData.name.trim(),
      websiteUrl: formData.websiteUrl.trim(),
      countryOfOrigin: formData.countryOfOrigin.trim(),
      isActive: formData.isActive
    });
  }
};

const handleCancel = () => {
  emit('cancel');
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

    <div class="flex items-center space-x-2">
      <input
        v-model="formData.isActive"
        type="checkbox"
        id="active-status"
        class="itbms-isActive w-4 h-4 text-blue-600 rounded focus:ring-blue-500"
      />
      <label for="active-status" class="text-sm font-medium text-gray-300">
        Active Brand
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
        {{ isSubmitting ? 'Saving...' : props.mode === 'create' ? 'Save' : 'Update' }}
      </button>
    </div>
  </form>
</template>