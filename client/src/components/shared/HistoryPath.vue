<script setup>
import {defineProps} from 'vue'
import { useRouter } from 'vue-router'


import { useProductStore } from "@/stores/useProductStore";
const productStore = useProductStore();

const props = defineProps({
    main: String,
    previous: Number,
    next: Number,
    namePath: {
        type: String,
    }
})
const router = useRouter()

const back = () => {
  router.push({ name: 'product-gallery' })

}
</script>

<template>
  <div class="flex cursor-pointer font-light mb-5 space-x-2.5">
     <p v-if="main" class="itbms-home-button" @click="router.go(-1)">{{ main }}</p>
     <p v-if="!main" class="itbms-home-button" @click="back">Home</p>
    <p>/</p>
    <p
      @click="router.go(-props.next)"
      class="itbms-back-button bg-gradient-to-r from-neutral-100 to-blue-200 text-black px-4 rounded-3xl"
      :class="props.previous > 1 ? 'from-neutral-100 to-red-200' : ''"
    >
      {{props.namePath}}  
    </p>
  </div>
</template>
<style scoped></style>