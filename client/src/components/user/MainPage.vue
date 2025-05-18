<script setup>
import { Swiper, SwiperSlide } from 'swiper/vue';
import { Autoplay, Pagination } from 'swiper/modules';
import 'swiper/css';
import 'swiper/css/pagination';
import { useProductStore } from '@/stores/useProductStore';

const productStore = useProductStore()

</script>

<template>
    <div class="h-[30em] mb-8">
      <Swiper
        :modules="[Autoplay, Pagination]"
        :slides-per-view="1"
        :space-between="0"
        :autoplay="{
          delay: 4000,
          disableOnInteraction: false,
          pauseOnMouseEnter: true
        }"
        loop
        pagination
        class="max-w-[96em] h-full m-12 rounded-lg shadow-lg"
      >
        <SwiperSlide
          v-for="(imgSrc, key) in productStore.bannerImages"
          :key="key"
        >
          <img
            :src="imgSrc"
            :alt="`Banner ${key}`"
            class="w-full object-contain"
          />
        </SwiperSlide>
      </Swiper> 
    </div>
  </template>
  
  

<style scoped>
.swiper-pagination-bullets {
  bottom: 16px !important;
}
</style>
