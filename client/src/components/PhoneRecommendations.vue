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
  '640': { slidesPerView: 1 },
  '768': { slidesPerView: 2 },
  '1024': { slidesPerView: 3 }
};

const productImages = products.productImages;
</script>

<template>
  <div class="bg-white">
    <h1 class="text-5xl font-extrabold text-center py-16 mb-4">
      Newest Phones Collection
    </h1>
    <Swiper
      v-if="isLoaded"
      :modules="[EffectFade, Navigation, Pagination]"
      :slides-per-view="3"
      :space-between="30"
      navigation
      pagination
      :breakpoints="breakPoint"
      class="w-full px-8"
    >
      <SwiperSlide
        v-for="product in products.latestProduct"
        :key="product.id"
        class="bg-white shadow-xl rounded-xl p-6"
      >
        <img :src="productImages[product.id] || DEFALUT_IMAGE " alt="" class="h-80 mx-auto object-contain" />
        <div class="text-center space-x-2 mb-2 font-bold text-2xl">
          <p class="itmbs-model">{{ product.model }}</p>
        </div>
        <p class="itmbs-price text-green-500 text-lg font-medium text-center mb-2">
          Price : {{ product.price }}
        </p>
        <div class="text-center mb-4">
          <button class="bg-blue-500 px-8 py-2 rounded-xl text-white font-semibold hover:bg-blue-600 transition">
            Buy
          </button>
        </div>
      </SwiperSlide>
    </Swiper>
  </div>
</template>
