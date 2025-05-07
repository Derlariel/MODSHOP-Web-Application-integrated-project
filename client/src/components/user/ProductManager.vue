<script setup>
import { onMounted, ref } from "vue";
import { useProductStore } from "@/stores/useProductStore";
import ProductList from "./ProductList.vue";
import { useRoute } from "vue-router";
const route = useRoute()

const productStore = useProductStore();

onMounted(async () => {
    await productStore.loadProducts();
    
});
</script>
 
<template>
<div>
    <ProductList v-if="!['product-detail','product-add'].includes(route.name) && !message"/>
    <router-view></router-view>
</div>
</template>
 
<style scoped> 
</style>