<script setup>

import ProductGallery from "@/components/user/ProductGallery.vue";
import AppFooter from "@/components/shared/AppFooter.vue";
import { useRoute } from "vue-router";
const route = useRoute()


</script>
 
<template>
<div>
    <ProductGallery v-if="!['product-detail','product-add','product-list'].includes(route.name)"/>
    <router-view></router-view>
     <AppFooter />
</div>
</template>
 
<style scoped> 

</style>