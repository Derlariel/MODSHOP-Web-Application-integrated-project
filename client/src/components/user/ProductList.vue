<script setup>
import ListModel from '../shared/ListModel.vue';

import { onMounted } from 'vue';
import { useProductStore } from '@/stores/useProductStore';

const productStore = useProductStore()
onMounted(() => {
productStore.loadProducts()
})


</script>
 
<template>
<div>
  <ListModel :products="productStore.allProducts" :productImages="productStore.productImages" />
</div>
</template>
 
<style scoped>

</style> 