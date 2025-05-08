<script setup>
import { useRoute, useRouter } from "vue-router";
import { useProductStore } from "@/stores/useProductStore";
import { onMounted, ref } from "vue";
import ProductPicture from "./ProductPicture.vue";
import DEFAULT_IMAGE from "@/assets/default.jpg";

const router = useRouter();
const route = useRoute();
const productId = Number(route.params.productId);
const productStore = useProductStore();
const isLoading = ref(true);
const product = ref(null);
const isData = ref(true);


onMounted(async () => {
  await productStore.loadProducts();
  const result = await productStore.fetchProductDetail(productId);
  product.value = result;

  if (productStore.products === null || !product.value) {
    sessionStorage.setItem(
      "error-message",
      "The requested sale item does not exist."
    );
    router.push("/sale-items");
    return;
  }

  isLoading.value = false;
});
</script>

<template>
  <div class="min-h-screen bg-black text-white">
    <div v-if="!isLoading && product && isData" class="pt-24 pb-20">
      <div class="max-w-[1200px] mx-auto px-6">
        <div class="itbms-row grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16">
          <ProductPicture />

          <div class="flex flex-col gap-8">
            <div class="itbms-row space-y-1">
              <div class="flex flex-wrap items-baseline gap-x-3 gap-y-1">
                <h1 class="itbms-brand text-3xl font-semibold tracking-tight">
                  {{ product.brandName }}
                </h1>
                <h1 class="itbms-model text-3xl font-semibold tracking-tight">
                  {{ product.model }}
                </h1>
              </div>

              <div class="flex items-center text-xl text-gray-400">
                <span class="itbms-storageGb">{{
                  product.storageGb === null ? "-" : product.storageGb
                }}</span>
                <span class="itbms-storageGb-unit ml-1">GB</span>
                <span class="mx-2">•</span>
                <span class="itbms-color">{{
                  product.color === null ? "-" : product.color
                }}</span>
              </div>
            </div>

            <div class="itbms-price text-3xl font-medium">
              ฿{{ product.price.toLocaleString() }}
            </div>

            <div class="space-y-4">
              <button
                class="w-full bg-white text-black rounded-full py-3.5 font-medium text-sm hover:bg-gray-200 transition-colors"
              >
                Buy
              </button>
              <button
                class="w-full bg-neutral-800 text-white rounded-full py-3.5 font-medium text-sm hover:bg-neutral-700 transition-colors"
              >
                Add to Bag
              </button>
            </div>

            <div class="border-t border-neutral-800 pt-6">
              <h2 class="text-xl font-medium mb-4">Overview</h2>
              <p class="itbms-description text-gray-400 leading-relaxed">
                {{
                  product.description 
                }}
              </p>
            </div>

            <div class="border-t border-neutral-800 pt-6">
              <div class="flex justify-between items-center mb-4">
                <h2 class="text-xl font-medium">Specifications</h2>
                <button
                  @click="toggleSpecifications"
                  class="text-sm text-blue-400 hover:underline"
                >
                  {{ showSpecifications ? "Hide details" : "Show details" }}
                </button>
              </div>

              <div class="text-gray-400 space-y-4 animate-fadeIn">
                <div class="grid grid-cols-3 gap-4">
                  <div class="bg-neutral-800/50 rounded-xl p-4 text-center">
                    <p class="text-xs text-gray-400 mb-1">RAM</p>
                    <p class="itbms-ramGb font-medium">{{ product.ramGb }}GB</p>
                  </div>

                  <div class="bg-neutral-800/50 rounded-xl p-4 text-center">
                    <p class="text-xs text-gray-400 mb-1">Storage</p>
                    <p class="itbms-storageGb font-medium">
                      {{ product.storageGb }}GB
                    </p>
                  </div>

                  <div class="bg-neutral-800/50 rounded-xl p-4 text-center">
                    <p class="text-xs text-gray-400 mb-1">Screen</p>
                    <p class="itbms-screenSizeInch font-medium">
                      {{ product.screenSizeInch }}"
                    </p>
                  </div>
                </div>

                <div class="grid grid-cols-2 gap-4">
                  <div>
                    <h3 class="text-white text-sm font-medium mb-1">Display</h3>
                    <p class="itbms-screenSizeInch text-sm">
                      {{
                        product.screenSizeInch === null
                          ? "-"
                          : product.screenSizeInch
                      }}
                      <span class="itbms-screenSizeInch-unit">Inches</span>
                    </p>
                  </div>

                  <div>
                    <h3 class="text-white text-sm font-medium mb-1">Memory</h3>
                    <p class="itbms-ramGb text-sm">
                      {{ product.ramGb === null ? "-" : product.ramGb }}
                      <span class="itbms-ramGb-unit">GB RAM</span>
                    </p>
                  </div>

                  <div>
                    <h3 class="text-white text-sm font-medium mb-1">Storage</h3>
                    <p class="text-sm">{{ product.storageGb }} GB</p>
                  </div>

                  <div>
                    <h3 class="text-white text-sm font-medium mb-1">Color</h3>
                    <p class="text-sm">
                      {{ product.color === null ? "-" : product.color }}
                    </p>
                  </div>
                </div>

                <div>
                  <h3 class="text-white text-sm font-medium mb-1">
                    Availability
                  </h3>
                  <p class="itbms-quantity text-sm">
                    {{ product.quantity > 0 ? "In Stock" : "Out of Stock" }}
                    <span class="text-xs"
                      >({{ product.quantity }} units available)</span
                    >
                  </p>
                </div>
              </div>
            </div>

            <div class="border-t border-neutral-800 pt-6">
              <a
                href="#"
                class="flex items-center text-blue-400 hover:underline"
              >
                <span>Compare all models</span>
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  class="h-4 w-4 ml-1"
                  viewBox="0 0 20 20"
                  fill="currentColor"
                >
                  <path
                    fill-rule="evenodd"
                    d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z"
                    clip-rule="evenodd"
                  />
                </svg>
              </a>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style>
.perspective {
  perspective: 1000px;
}
.transform-style-3d {
  transform-style: preserve-3d;
}
.rotate-y-5 {
  transform: rotateY(5deg);
}
.rotate-y-10 {
  transform: rotateY(10deg);
}
.rotate-x-3 {
  transform: rotateX(3deg);
}
.translate-z-20 {
  transform: translateZ(20px);
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
.animate-fadeIn {
  animation: fadeIn 0.3s ease-out forwards;
}
</style>
