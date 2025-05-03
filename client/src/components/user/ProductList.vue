<script setup>
import ListModel from "../shared/ListModel.vue";
import Navbar from "../shared/Navbar.vue";
import SideBar from "../shared/SideBar.vue";
import { computed, onMounted, ref } from "vue";
import { useProductStore } from "@/stores/useProductStore";
import DEFAULT_IMAGE from "@/assets/default.jpg";
import { useRouter } from "vue-router";

const router = useRouter();
const isLoading = ref(true);

const props = defineProps({
  viewType: {
    type: String,
    default: "gallery",
  },
});

const productStore = useProductStore();
const productImages = productStore.productImages;
const product = computed(() => productStore.allProducts);

onMounted(async () => {
  await productStore.loadProducts();

  if (product.value.length === 0) {
    router.push({ name: "error-page", query: { code: 503 } });
    return;
  }

  isLoading.value = false;
});

const detail = (productId) => {
  router.push({
    name: "product-detail",
    params: {
      productId: productId,
    },
  });
};
</script>


<template>
  <div class="flex h-screen">
    <!-- Sidebar ด้านซ้าย -->
    <SideBar />

    <!-- Content (ListModel) -->
    <div class="flex-1 overflow-y-auto p-4">
      <ListModel :saleItems="productStore.allProducts" :viewType="viewType">
        <template #listItems="{ Item: product, viewType }">
          <div
            @click="detail(product.id)"
            v-if="viewType === 'gallery'"
            class="bg-white rounded-md overflow-hidden hover:shadow-md transition-shadow cursor-pointer flex flex-col h-full"
          >
            <div class="bg-gray-200 w-full h-72 flex items-center justify-center overflow-hidden">
              <img :src="productImages[Number(product.id)] || DEFAULT_IMAGE" class="max-h-full object-contain" alt="" />
            </div>
            <div class="p-4 flex flex-col justify-between flex-1">
              <div>
                <h2 class="text-base font-semibold text-gray-800">
                  {{ product.brandName }}
                </h2>
                <p class="text-sm text-gray-700 font-medium mb-1">
                  Model: {{ product.model }}
                </p>
                <p class="text-sm text-gray-600 mb-2">
                  RAM: {{ product.ramGb }} GB / {{ product.storageGb }} GB
                </p>
              </div>
              <p class="text-[#E46A6A] font-bold text-lg mt-auto">฿{{ product.price }}</p>
            </div>
          </div>
        </template>
      </ListModel>
    </div>
  </div>
</template>
