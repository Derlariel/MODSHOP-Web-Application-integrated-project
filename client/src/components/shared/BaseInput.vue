<script setup>
import { defineProps, defineEmits, computed, watch } from "vue";

const props = defineProps({
  error: String,
  modelValue: {
    type: [String, Number],
    default: "",
  },
  label: String,
  type: {
    type: String,
    default: "text",
  },
  required: Boolean,
  cypress: String,
  placeholder: String,
  prefix: String,
  step: String,
  id: String,
  maxInput: Number,
  showCounter: Boolean,
  validateOnInput: Boolean,
});

const emit = defineEmits(["update:modelValue", "trim"]);

const updateValue = (e) => {
  emit("update:modelValue", e.target.value);
  
  if (props.validateOnInput && props.maxInput && e.target.value.length > props.maxInput) {
    emit("trim");
  }
};

const onBlur = () => {
  emit("trim");
};


</script>

<template>
  <div class="space-y-1">
    <div class="flex justify-between items-center">
      <label :for="id || cypress" class="block text-sm font-medium text-gray-300">
        {{ label }}<span class="text-red-500 ml-1">*</span>
      </label>

    </div>
    <div class="relative">
      <div v-if="prefix" class="absolute inset-y-0 left-0 pl-4 flex items-center pointer-events-none text-gray-400">
        {{ prefix }}
      </div>
      <input
        :id="id || cypress"
        :class="[
          `${cypress} w-full px-4 py-3.5 rounded-xl border focus:ring-2 focus:ring-white focus:border-neutral-500 transition-all bg-neutral-800 text-white`,
          (error) ? 'border-red-700' : 'border-neutral-700',
          prefix ? 'pl-8' : '',
        ]"
        :value="modelValue"
        :type="type"
        :placeholder="placeholder"
        :step="step"
        @input="updateValue"
        @blur="onBlur"
      />
    </div>
    <p v-if="error" class="itbms-message text-sm text-red-500 mt-1">{{ error }}</p>
  </div>
</template>


<style scoped></style>
