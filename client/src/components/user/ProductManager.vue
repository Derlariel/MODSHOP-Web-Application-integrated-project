<script setup>
import { onMounted, ref } from "vue";
import { useProductStore } from "@/stores/useProductStore";
import ProductGallery from "./ProductGallery.vue";
import { useRoute } from "vue-router";
const route = useRoute()

const productStore = useProductStore();

onMounted(async () => {
    await productStore.loadProducts();
    
});
</script>
 
<template>
<div>
    <ProductGallery v-if="!['product-detail','product-add','product-list'].includes(route.name)"/>
    <router-view></router-view>
</div>
</template>
 
<style scoped> 

</style>