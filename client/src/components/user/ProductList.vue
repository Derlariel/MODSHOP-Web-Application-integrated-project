<script setup>
import ListModel from "../shared/ListModel.vue";
import Navbar from "../shared/Navbar.vue";
import { onMounted } from "vue";
import { useProductStore } from "@/stores/useProductStore";
import I14PROMAX from "@/assets/apple/iPhone-14-Pro-Max-Space-Black.webp";
import I14 from "@/assets/apple/iPhone_14_Midnight.png";
import I13PRO from "@/assets/apple/iphone-13-pro-blue-select.png";
import SE2020 from "@/assets/apple/iPhone_SE3_Starlight.webp";
import I14PLUS from "@/assets/apple/iPhone_14_Plus_Blue-square_medium.webp";
import S23ULTRA from "@/assets/samsung/Samsung-Galaxy-S23-Ultra.webp";
import { useRouter } from "vue-router";

const router = useRouter();

const props = defineProps({
  viewType: {
    type: String,
    default: "gallery",
  },
});

const productStore = useProductStore();
const productImages = productStore.productImages

onMounted(async () => {
  await productStore.loadProducts();
});

const detail = (productId) => {
  router.push({
    name: "product-detail",
    params: {
      productId: productId
    }
  })
};
</script>

<template>
  <div>
    <ListModel :saleItems="productStore.allProducts" :viewType="viewType">
      <template #listItems="{ Item: product, viewType }">
        <div
          @click="detail(product.id)"
          v-if="viewType === 'gallery'"
          class="bg-white rounded-xs overflow-hidden hover:shadow-xs transition-shadow"
        >
          <img
            :src="productImages[product.id]"
            class="w-full h-50 object-cover bg-gray-100"
            alt=""
          />
          <div class="p-4">
            <h2 class="text-lg font-semibold text-gray-800">
              {{ product.brandName }}
            </h2>
            <p class="text-gray-700 font-medium mb-1">
              Model: {{ product.model }}
            </p>
            <p class="text-sm text-gray-600 mb-2">
              RAM: {{ product.ramGb }} GB / {{ product.storageGb }} GB
            </p>
            <p class="text-[#E46A6A] font-bold text-xl">฿{{ product.price }}</p>
          </div>
        </div>
      </template>
    </ListModel>
  </div>
</template>

<style scoped>

</style>
