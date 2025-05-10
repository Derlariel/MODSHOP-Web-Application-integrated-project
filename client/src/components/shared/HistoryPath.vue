<script setup>
import {defineProps} from 'vue'
import {useRouter} from 'vue-router'

const props = defineProps({
    previous: Number,
    next: Number,
    namePath: {
        type: String,
    }
})
const router = useRouter()
</script>

<template>
  <div class="flex cursor-pointer font-light mb-5 space-x-2.5">
    <p @click="router.go(-props.previous)">Home</p>
    <p>/</p>
    <p
      @click="router.go(-props.next)"
      class="bg-gradient-to-r from-neutral-100 to-blue-200 text-black px-4 rounded-3xl"
      :class="props.previous > 1 ? 'from-neutral-100 to-red-200' : ''"
    >
      {{props.namePath}}
    </p>
  </div>
</template>
<style scoped></style>
