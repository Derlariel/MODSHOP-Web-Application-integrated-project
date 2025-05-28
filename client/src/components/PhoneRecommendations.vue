<script setup>
import { Swiper, SwiperSlide } from "swiper/vue";
import { EffectFade, Navigation, Pagination } from "swiper/modules";
import "swiper/css";
import "swiper/css/navigation";
import "swiper/css/pagination";
import "swiper/css/effect-fade";
import { onMounted, ref } from "vue";
import { useProductStore } from "@/stores/useProductStore";
import DEFALUT_IMAGE from "@/assets/default.jpg";

const products = useProductStore();
const isLoaded = ref(false);

onMounted(async () => {
  await products.loadProducts(); 
  isLoaded.value = true;
});

const breakPoint = {
  '640': { slidesPerView: 1, spaceBetween: 16 },
  '768': { slidesPerView: 2, spaceBetween: 20 },
  '1024': { slidesPerView: 4, spaceBetween: 24 },
};


</script>

<template>
  <div class="bg-white">
    <!-- Headline Section -->
    <div class="container mx-auto max-w-screen-xl px-4 sm:px-6 lg:px-8">
      <h1 class="text-3xl sm:text-4xl md:text-5xl font-extrabold text-center py-12 sm:py-16">
        Newest Phones Collection
      </h1>
    </div>

    <!-- Full Width Swiper Section -->
    <div class="w-full px-4 sm:px-6 lg:px-8">
      <Swiper
        v-if="isLoaded"
        :modules="[EffectFade, Navigation, Pagination]"
        :breakpoints="breakPoint"
        navigation
        pagination
        class="w-full"
      >
        <SwiperSlide
          v-for="product in products.latestProduct"
          :key="product.id"
          class="bg-white shadow-xl rounded-xl p-4 sm:p-6 h-full flex flex-col justify-between"
        >
          <img
            :src="DEFALUT_IMAGE"
            alt="product image"
            class="h-60 sm:h-72 lg:h-80 xl:h-96 w-full object-contain mb-4"
          />
          <div class="text-center space-x-2 my-2 font-bold text-xl sm:text-2xl">
            <p class="itmbs-model">{{ product.model }}</p>
          </div>
          <p class="itmbs-price text-green-500 text-base sm:text-lg font-medium text-center mb-2">
            Price: {{ product.price }}
          </p>
          <div class="text-center mt-auto">
            <button
              class="bg-blue-500 px-6 sm:px-8 py-2 rounded-xl text-white font-semibold hover:bg-blue-600 transition"
            >
              Buy
            </button>
          </div>
        </SwiperSlide>
      </Swiper>
    </div>
  </div>
</template>