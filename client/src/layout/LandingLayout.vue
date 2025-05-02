<script setup>
import { ref } from "vue";
import Navbar from "@/components/shared/Navbar.vue";

const adminMode = ref(false);
</script>

<template>
  <div>
    <Navbar/>
    <router-view />
  </div>
</template>
