<script setup>
import { onMounted, ref } from "vue";
import { useProductStore } from "@/stores/useProductStore";
import ProductList from "./ProductList.vue";
import { useRoute } from "vue-router";
const route = useRoute()

const productStore = useProductStore();
const message = ref(null)

onMounted(async () => {
    await productStore.loadProducts();

    message.value = sessionStorage.getItem('error-message');
    sessionStorage.removeItem('error-message');
    
});
</script>
 
<template>
<div>
    <div v-if="message">
        <h1 class="itbms-message">{{ message }}</h1>
    </div>
    <ProductList v-if="!['product-detail'].includes(route.name) && !message"/>
    <router-view></router-view>
</div>
</template>
 
<style scoped> 
</style>