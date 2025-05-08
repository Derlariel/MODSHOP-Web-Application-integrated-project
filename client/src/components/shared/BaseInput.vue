<script setup>
import { defineProps } from "vue";
const model = defineModel();
defineProps({
  label: String,
  id: String,
  type: {
    type: String,
    default: "text",
  },
  placeholder: String,
  error: String,
  helper: String,
  prefix: String, 
});

</script>
<template>
  <div class="space-y-3">
    <label :for="id" class="block text-sm font-medium text-gray-300">
      {{ label }}
    </label>

    <div class="relative">
      <div
        v-if="prefix"
        class="absolute inset-y-0 left-0 flex items-center pl-4 pointer-events-none text-gray-400 font-medium"
      >
        {{ prefix }}
      </div>

      <input
        v-model="model"
        :type="type"
        :id="id"
        :placeholder="placeholder"
        :class="[
          'w-full py-3.5 pr-4 rounded-xl border transition-all bg-neutral-800 text-white',
          prefix ? 'pl-12' : 'px-4',
          error
            ? 'border-red-500 focus:ring-red-500 focus:border-red-500'
            : 'border-neutral-700 focus:ring-white focus:border-neutral-500'
        ]"
      />
    </div>

    <p v-if="error" class="text-sm text-red-500 mt-1">{{ error }}</p>
    <p v-else-if="helper" class="text-sm text-gray-400 mt-1">{{ helper }}</p>
  </div>
</template>


<style scoped></style>
