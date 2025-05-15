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
} from 'vue';
import { useBrandStore } from '@/stores/useBrandStore';
import { useRouter } from 'vue-router';
import BaseInput from '../shared/BaseInput.vue';

const router = useRouter();
const brandStore = useBrandStore();
const emit = defineEmits(['submit', 'cancel']);
const props = defineProps({
  init: {
    type: Object,
    default: () => ({}),
  },
});

const temp = reactive({
  id: null,
  name: '',
  websiteUrl: '',
  isActive: true,
  countryOfOrigin: '',
});

const btnNotAvailable = ref(true);
const isLoading = ref(true);
const errorMessage = ref(null);

watch(
  () => props.init,
  async (newValue) => {
    try {
      // Initialize form with provided data or defaults
      Object.assign(temp, {
        id: newValue.id || null,
        name: newValue.name || '',
        websiteUrl: newValue.websiteUrl || '',
        isActive: newValue.isActive !== undefined ? newValue.isActive : true,
        countryOfOrigin: newValue.countryOfOrigin || '',
      });

      isLoading.value = false;
    } catch (error) {
      console.error('Error loading brand data:', error);
      errorMessage.value = 'Failed to load brand data';
      isLoading.value = false;
    }
  },
  { immediate: true }
);

watchEffect(() => {
  const requiredFields = ['name'];
  const allFields = ['name', 'websiteUrl', 'countryOfOrigin', 'isActive'];

  // Check if required fields are empty
  const requiredFieldsEmpty = requiredFields.some((field) => {
    return !temp[field] || temp[field].toString().trim() === '';
  });

  // Check if form is unchanged from initial values
  const isUnchanged = allFields.every((field) => {
    const tempValue = temp[field] === '' ? null : temp[field];
    const initValue = props.init[field] === undefined ? null : props.init[field];
    
    if (field === 'isActive') {
      return tempValue === initValue;
    }
    return tempValue === initValue;
  });

  // Button should be disabled if required fields are empty or form is unchanged
  btnNotAvailable.value = requiredFieldsEmpty || isUnchanged;
});

const submit = () => {
  if (btnNotAvailable.value) {
    console.warn('Submit blocked: Button is disabled');
    return;
  }
  
  emit('submit', {
    ...temp,
  });
};

const trimField = (field) => {
  if (typeof temp[field] === 'string') {
    temp[field] = temp[field].trim() || '';
  }
};

onMounted(() => {
  isLoading.value = false;
});
</script>
 
<template>
  <div>
    <div v-if="errorMessage" class="text-red-500 text-center mb-4">
      {{ errorMessage }}
    </div>
    <div v-else-if="!isLoading">
      <form @submit.prevent class="space-y-8">
        <!-- Brand Name -->
        <BaseInput
          @trim="trimField('name')"
          v-model="temp.name"
          cypress="itbms-brand-name"
          placeholder="Brand name"
          label="Brand Name"
          id="name"
          :maxInput="50"
          required
        />

        <!-- Website URL -->
        <BaseInput
          @trim="trimField('websiteUrl')"
          v-model="temp.websiteUrl"
          cypress="itbms-brand-url"
          placeholder="https://example.com"
          label="Website URL"
          id="websiteUrl"
          :maxInput="100"
        />

        <!-- Country of Origin -->
        <BaseInput
          @trim="trimField('countryOfOrigin')"
          v-model="temp.countryOfOrigin"
          cypress="itbms-brand-country"
          placeholder="Country of origin"
          label="Country of Origin"
          id="countryOfOrigin"
          :maxInput="50"
        />

        <!-- Active Status -->
        <div class="space-y-3">
          <label for="isActive" class="block text-sm font-medium text-gray-300">Status</label>
          <div class="flex items-center space-x-2">
            <input
              type="checkbox"
              id="isActive"
              v-model="temp.isActive"
              class="itbms-brand-active w-4 h-4 rounded border-neutral-700 text-blue-600 focus:ring-blue-500"
            />
            <span class="text-gray-300">Active</span>
          </div>
        </div>

        <!-- Form Buttons -->
        <div class="pt-8 flex flex-col sm:flex-row gap-4">
          <button
            type="button"
            @click="submit"
            :disabled="btnNotAvailable"
            class="itbms-brand-save-button"
            :class="
              btnNotAvailable
                ? 'flex-1 bg-red-400/20 text-white py-4 px-6 rounded-full hover:bg-neutral-700 transition-colors duration-300 font-medium'
                : 'flex-1 bg-white text-black py-4 px-6 rounded-full hover:bg-gray-200 transition-colors duration-300 font-medium'
            "
          >
            Save Brand
          </button>
          <button
            @click="router.back()"
            type="button"
            class="itbms-brand-cancel-button flex-1 bg-neutral-800 text-white py-4 px-6 rounded-full hover:bg-neutral-700 transition-colors duration-300 font-medium"
          >
            Cancel
          </button>
        </div>
        
        <p class="text-center text-gray-500 text-sm">
          All changes will be saved to the brand registry
        </p>
      </form>
    </div>
    <div v-else class="text-center text-gray-400">
      <div class="animate-spin rounded-full h-8 w-8 border-t-2 border-b-2 border-white mx-auto mb-4"></div>
      Loading brand data...
    </div>
  </div>
</template>
 
<style scoped></style>