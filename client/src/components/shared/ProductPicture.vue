2
<script setup>
import { ref, defineProps, computed } from "vue";
import { useProductStore } from "@/stores/useProductStore";
import defaultImage from "@/assets/default.jpg";
import UploadPicture from "./modal/UploadPicture.vue";
const productStore = useProductStore();
const selectedImage = ref(1);
const uploadModalRef = ref(null);

const props = defineProps({
  images: Array,
});

const setSelectedImage = (index) => {
  selectedImage.value = index;
};

function openUploadModal() {
  uploadModalRef.value?.openModal();
}

const BASE_URL = "http://localhost:8080/itb-mshop/";

const displayImages = computed(() => {
  const imgs = props.images?.length
    ? props.images.map(img => BASE_URL + img)
    : productStore.productImages;

  const filled = [...imgs];
  while (filled.length < 4) {
    filled.push(defaultImage);
  }

  return filled.slice(0, 4);
});
</script>

<template>
  <div class="relative">
    <div class="sticky top-20 perspective">
      <div
        class="bg-gradient-to-br from-neutral-800 to-neutral-900 rounded-3xl overflow-hidden flex items-center justify-center h-[450px] mb-8 transform-style-3d hover:rotate-y-5 hover:rotate-x-3 transition-transform duration-700"
      >
        <img
          class="object-contain max-h-full w-auto transition-all duration-700 hover:scale-105 hover:translate-z-20"
          :src="displayImages[selectedImage] || defaultImage"
          alt="product"
        />
        <div
          class="absolute bottom-0 left-0 right-0 h-40 bg-gradient-to-t from-black/30 to-transparent"
        ></div>
      </div>

      <div class="flex flex-wrap gap-4 justify-center">
        <div
          v-for="(img, i) in displayImages"
          :key="i"
          @click="setSelectedImage(i)"
          class="relative cursor-pointer group perspective"
        >
          <div
            class="transform-style-3d hover:rotate-y-10 transition-transform duration-300"
          >
            <img
              :src="img"
              class="h-20 w-20 object-contain bg-neutral-800 rounded-xl shadow-sm transition-all duration-300 group-hover:shadow-lg"
              :class="[
                { 'ring-2 ring-white': selectedImage === i },
                `itbms-picture-file${i}`,
              ]"
              alt="thumbnail"
            />
          </div>
          <div
            v-if="selectedImage === i"
            class="absolute bottom-0 left-0 right-0 h-1 bg-white rounded-b-lg"
          ></div>
        </div>
      </div>

      <div class="mt-8 flex justify-center">
        <button
          class="flex items-center space-x-2 bg-white/10 backdrop-blur-md px-4 py-2 rounded-full text-sm font-medium hover:bg-white/20 transition-colors"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-5 w-5"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M21 12V7a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v5"></path>
            <path d="M9 22h6"></path>
            <path d="M12 17v5"></path>
            <path d="M12 12a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z"></path>
            <path d="M3 12h18"></path>
          </svg>
          <span>View in AR</span>
        </button>
      </div>
      <div class="mt-8 flex justify-center">
        <button
          @click="openUploadModal"
          class="bg-orange-500 text-white px-6 py-3 rounded-lg hover:bg-orange-700 transform transition duration-200 hover:scale-105"
        >
          Upload Pictures
        </button>
      </div>

      <UploadPicture :images="images" ref="uploadModalRef" />
    </div>
  </div>
</template>

<style scoped></style>
