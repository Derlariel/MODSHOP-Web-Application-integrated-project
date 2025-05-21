<script setup>
import { onMounted} from 'vue';
import { useRoute } from 'vue-router';
import BrandList from './BrandList.vue';
const route = useRoute()
import AppFooter from "../shared/AppFooter.vue";

import { useBrandStore } from '@/stores/useBrandStore';
const brandStore = useBrandStore();
onMounted(async () => {
  await brandStore.loadBrands();
});

</script>

<template>
  <div>
    <BrandList
      v-if="
        !['brand-add'].includes(route.name)
      "
    />
    <router-view></router-view>
    <AppFooter />
  </div>
</template>

<style scoped></style>
