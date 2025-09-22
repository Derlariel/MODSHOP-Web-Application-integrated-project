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
  // Button props
  isButton: Boolean,
  buttonText: String,
  disabled: Boolean,
  variant: {
    type: String,
    default: "primary", // primary, secondary
  },
  sanitize: {type:String , default:""}
});

const emit = defineEmits(["update:modelValue", "trim", "click"]);

const cleanValue = (raw) => {
  let v = raw ?? ""
  if(props.sanitize === "email"){
    v = String(v).replace(/\s+/g, "")
  }
  return v
}

const updateValue = (e) => {
  const raw = e.target.value;
  const val = cleanValue(raw)
  if(val !== raw) e.target.value = val
  emit("update:modelValue", e.target.value);
  
  if (props.validateOnInput && props.maxInput && e.target.value.length > props.maxInput) {
    emit("trim");
  }
};

const onBlur = (e) => {
  const val = cleanValue(e.target.value)
  if(val !== e.target.value) e.target.value = val
  emit("update:modelValue", val);
  emit("trim");
};

const onButtonClick = (e) => {
  emit("click", e);
};

const buttonClasses = computed(() => {
  const baseClasses = "flex-1 px-4 py-3 rounded-xl text-md transition-all duration-300 ease-out disabled:bg-gray-200/40 disabled:hover:bg-red-500 disabled:hover:text-white disabled:cursor-not-allowed";

  if (props.variant === "primary") {
    return `${baseClasses} bg-white text-black shadow-md hover:opacity-70 cursor-pointer `;
  } else {
    return `${baseClasses} bg-neutral-800 text-white hover:bg-neutral-700 cursor-pointer`;
  }
});


</script>

<template>
  <div class="space-y-1">
    <div v-if="label && !isButton" class="flex justify-between items-center">
      <label :for="id || cypress" class="block text-sm font-medium text-gray-300">
        {{ label }}<span v-if="required" class="text-red-500 ml-1">*</span>
      </label> 
    </div>
    
    <!-- Button mode -->
    <button
      v-if="isButton"
      :type="type"
      :class="[cypress, buttonClasses]"
      :disabled="disabled"
      @click="onButtonClick"
    >
      {{ buttonText }}
    </button>
    
    <!-- Input mode -->
    <div v-else class="relative">
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
    :maxlength="maxInput"
        @input="updateValue"
        @blur="onBlur"
      />
    </div>
    <p v-if="error" class="itbms-message text-sm text-red-500 mt-1">{{ error }}</p>
  </div>
</template>


<style scoped></style>
