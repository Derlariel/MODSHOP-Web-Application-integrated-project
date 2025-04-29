<script setup>
import ListModel from '../shared/ListModel.vue';
import Navbar from '../shared/Navbar.vue';
import { onMounted } from 'vue';
import { useProductStore } from '@/stores/useProductStore';
import I14PROMAX from '@/assets/apple/iPhone-14-Pro-Max-Space-Black.webp'
import I14 from '@/assets/apple/iPhone_14_Midnight.png'
import I13PRO from '@/assets/apple/iphone-13-pro-blue-select.png'
import SE2020 from '@/assets/apple/iPhone_SE3_Starlight.webp'
import I14PLUS from '@/assets/apple/iPhone_14_Plus_Blue-square_medium.webp'
import S23ULTRA from '@/assets/samsung/Samsung-Galaxy-S23-Ultra.webp'
const productStore = useProductStore()
onMounted(() => {
productStore.loadProducts()
})

const productImages = {
  1: I14PROMAX,
  2: I14,
  3: I13PRO,
  7: SE2020,
  8: I14PLUS,
  16: S23ULTRA,
}

</script>
 
<template>
<div>
  <Navbar/>
  <ListModel :products="productStore.allProducts" :productImages="productImages" />
</div>
</template>
 
<style scoped>

</style> 