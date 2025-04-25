<script setup>
import ProductList from '@/components/user/ProductList.vue'
import Home from '@/components/Home.vue'
import ThemeToggle from '@/components/shared/ThemeToggle.vue';
import { useTheme } from '@/stores/useTheme'

const theme = useTheme()
</script>
 
<template>
<div :data-theme="theme">
<ThemeToggle/>
<ProductList/>
 <!-- <Home/> -->
</div>
</template>
 
<style scoped>

</style>