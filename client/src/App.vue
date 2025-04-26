<script setup>
import { useTheme } from '@/stores/useTheme'
import ThemeToggle from '@/components/shared/ThemeToggle.vue'
const theme = useTheme()
</script>

<template>
  <div :data-theme="theme">
    <ThemeToggle />
    <RouterView />
  </div>
</template>

<style scoped>
</style>
