<script setup>
import { useTheme } from '@/stores/useTheme'
import HomePage from './pages/HomePage.vue';
import MainPage from './components/user/MainPage.vue';
const theme = useTheme()
</script>

<template>
  <div :data-theme="theme">
    <router-view/>
  </div>
</template>

<style scoped>
</style>
