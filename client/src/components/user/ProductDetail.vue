<script setup>
import { useRoute, useRouter } from 'vue-router';
import { useProductStore } from "@/stores/useProductStore";
import { computed } from 'vue';
const { params } = useRoute()

const { params: { productId } } = useRoute();
const productStore = useProductStore();
const product = computed(() => productStore.getProductById(Number(productId)))

</script>
 
<template>
<div v-if="product">
    <div class="grid grid-cols-2">

        <div class="w-full">
            <div class="flex justify-center items-center">
                <img class="h-100" :src="productStore.productImages[product.id]" alt="">
            </div>
            <div class="flex justify-center -space-x-9">
                <div v-for="i in 5" >
                 <img class="h-30 w-30" :src="productStore.productImages[product.id]" alt="">
            </div>
            </div>
        </div>

        <div class="mt-10">
            <div class="flex space-x-2 text-2xl">
                <h1>{{ product.brandName }}</h1>
                <h1>{{ product.model }}</h1>
                <div class="flex">
                    <h1>{{ product.storageGb }}</h1>
                    <h1>GB</h1>
                </div>
                <h1>{{ product.color }}</h1>
            </div>
            <div>
                <p>Brand: {{ product.brandName }}</p>
            </div>
            <div>
                <h2> {{ product.price }}</h2>
            </div>
            <div>
                <p>{{ product.description }}</p>
                <div>
                    <p>{{ product.ramGb }}</p>
                    <p>{{ product.screenSizeInch }}</p>
                    <p>{{ product.storageGb }}</p>
                    <p>{{ product.color }}</p>
                </div>
            </div>
        </div>
    </div>
</div>
</template>
 
<style scoped>

</style>