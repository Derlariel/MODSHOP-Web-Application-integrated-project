<script setup>
import { ref } from 'vue'
import Navbar from "@/components/shared/Navbar.vue"

</script>

<template>
  <div class="w-screen min-h-screen">
    <Navbar />

    <router-view />
  </div>
</template>
