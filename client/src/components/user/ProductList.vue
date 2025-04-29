<script setup>
import { useProductStore } from '@/stores/useProductStore.js'
import Navbar from '../shared/Navbar.vue';
const productStore = useProductStore()
</script>

<template>
  <div class="">
    <Navbar/>
 
  </div>
</template>

<style scoped>
</style>
