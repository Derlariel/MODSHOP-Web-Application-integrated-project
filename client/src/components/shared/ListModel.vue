<script setup>
const props = defineProps({
  saleItems: {
    type: Array,
    required: true,
  },
  viewType: {
    type: String,
    default: "gallery",
    validator(value) {
      return ["gallery", "list"].includes(value);
    },
  },
});
</script>

<template>
  <div class="container mx-auto px-4">
    <div v-if="viewType === 'gallery'" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-6">
      <div v-for="(item, index) in saleItems" :key="index">
        <slot name="listItems" :Item="item" :viewType="viewType"></slot>
      </div>
    </div>
    <div v-if="viewType === 'list'" class="space-y-4">
      <div v-for="(item, index) in saleItems" :key="index" class="border-b-1 border-gray-300 p-4">
        <slot name="listItems" :Item="item" :viewType="viewType"></slot>
      </div>
    </div>
  </div>
</template>


<style scoped></style>