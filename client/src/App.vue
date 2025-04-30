<script setup>
import { useTheme } from '@/stores/useTheme'
const theme = useTheme()
</script>

<template>
  <div :data-theme="theme">
    <RouterView />
  </div>
</template>

<style scoped>
</style>
