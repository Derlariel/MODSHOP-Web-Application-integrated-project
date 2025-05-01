<script setup>
import { onMounted, ref } from "vue";
import { useProductStore } from "@/stores/useProductStore";
import ProductList from "./ProductList.vue";
import { useRoute } from "vue-router";
const route = useRoute()
import Navbar from "../shared/Navbar.vue";

const productStore = useProductStore();

onMounted(async () => {
    await productStore.loadProducts();
});
</script>
 
<template>
<div>
    <ProductList v-if="!['product-detail'].includes(route.name)"/>
    <router-view></router-view>
</div>
</template>
 
<style scoped> 
</style>