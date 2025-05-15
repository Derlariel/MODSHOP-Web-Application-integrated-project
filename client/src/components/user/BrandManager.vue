<script setup>
import { onMounted, ref } from "vue";
import BrandList from "./BrandList.vue";
import { useRoute } from "vue-router";
const route = useRoute()


onMounted(async () => {
    await useBrandStore.loadBrands();
    
});
</script>
 
<template>
<div>
    <BrandList v-if="!['brand-detail','brand-add'].includes(route.name)"/>
    <router-view></router-view>
</div>
</template>
 
<style scoped> 

</style>