<script setup>
const props = defineProps({
  variant: {
    type: String,
    default: "dark" 
  }
})

const variantClasses = {
  dark: "bg-neutral-900 border border-neutral-700 text-white shadow-xl rounded-2xl",
  light: "bg-white border border-gray-200 text-gray-900 shadow-md rounded-xl",
  transparent: "bg-transparent border border-gray-700 text-white"
}
</script>
 
<template>
  <div
    :class="variantClasses[variant]"
  >
    <slot />
  </div>
</template>

 
<style scoped>

</style>