<script setup>
import { useRoute, useRouter } from "vue-router";
import { useProductStore } from "@/stores/useProductStore";
import { onMounted, ref, computed } from "vue";
import ProductPicture from "@/components/shared/ProductPicture.vue";
import HistoryPath from "@/components/shared/HistoryPath.vue";
import ConfirmModal from "@/components/shared/modal/ConfirmModal.vue";
import SuccessModal from "@/components/shared/modal/SuccessModal.vue";
import { checkUpToDate } from "@/utils/validate";
import ErrorModal from "@/components/shared/modal/ErrorModal.vue";
import { useCartStore } from "@/stores/useCartStore";
import { useAuthStore } from "@/stores/useAuthStore";

// const BASE_URL = "http://localhost:8080/itb-mshop/sale-items-images/";
const BASE_URL = "http://intproj24.sit.kmutt.ac.th/kk1/itb-mshop/sale-items-images/";

const router = useRouter();
const route = useRoute();
const cart = useCartStore();
const auth = useAuthStore();
const selectedQuantity = ref(1)
const showSuccess = ref(false)
const showError = ref(false)
const errorMessage = ref("")
const productId = Number(route.params.productId);
const productStore = useProductStore();
const isLoading = ref(true);
const product = ref(null);
const isData = ref(true);

// console.log('detail', productStore.getActivePage); // DEBUG: product active page

const submit = () => {
  if (!canManage.value) return;
  router.push({
    name: "sale-items-edit",
    params: {
      productId: route.params.productId,
    },
  });
};

const title = computed(() => {
  if (!product.value) return '';

  const {
    brandName = '',
    model = '',
    ramGb = '',
    storageGb = '',
    color = ''
  } = product.value;

  return `${brandName} ${model} ${ramGb}/${storageGb}GB ${color}`.trim();
});

// Only SELLER (auth.user.role === 'SELLER') and the owner of the sale item can manage
const canManage = computed(() => {
  return !!(
    auth?.user &&
    auth.user.role === "SELLER" &&
    product.value &&
    Number(product.value.sellerId) === Number(auth.user.id)
  );
});

const showDelete = ref(false)
const deleteSaleItem = () => {
  if (!canManage.value) return;
  showDelete.value = true
}
const confirm = async () => {
  if (!canManage.value) return;
  try {
    await productStore.deleteProduct(productId);
    sessionStorage.setItem("delete-success", "true");
    router.push("/sale-items");
    sessionStorage.setItem("activePage", 1);
    productStore.setActivePage(1)
  } catch (error) {
    sessionStorage.setItem("error-message", "true");
    console.log(sessionStorage.getItem("error-message"));
    router.push("/sale-items");
  }
};

const showAddSuccess = ref(false)

const addToCart = () => {
  if (!auth.isAuthenticated || !auth.user) {
    router.push({ name: "Login" });
    return;
  }

  if (product.value) {
    // Prevent seller from adding own item to cart
    if (
      auth?.user?.role === "SELLER" &&
      Number(product.value.sellerId) === Number(auth.user.id)
    ) {
      errorMessage.value = "You cannot add your own sale item to the cart.";
      showError.value = true;
      return;
    }
    const ok = cart.addToCart(
      {
        saleItemId: product.value.id,
        sellerId: product.value.sellerId,
        sellerNickname: product.value.sellerNickname,
        name: `${product.value.brandName} ${product.value.model} ${product.value.ramGb}/${product.value.storageGb}GB ${product.value.color}`.trim(),
        price: product.value.price,
        stock: product.value.quantity
      },
      selectedQuantity.value
    )
    if (ok) showAddSuccess.value = true
  }
}


onMounted(async () => {
  isLoading.value = true;

  try {
    await productStore.loadProducts();

    product.value = await productStore.fetchProductDetail(productId);

    if (product.value && checkUpToDate(product)) {
      showSuccess.value = false;
      router.push("/sale-items");
      return;
    }
  } catch (e) {
    router.push("/sale-items")
    sessionStorage.setItem("error-message", "true")
  }

  if (sessionStorage.getItem("edit-success") === "true") {
    showSuccess.value = true;
    sessionStorage.removeItem("edit-success");
    setTimeout(() => {
      showSuccess.value = false;
    }, 3000);
  }

  isLoading.value = false;
});


</script>

<template>
  <div class="min-h-screen bg-black text-white">
    <div v-if="!isLoading && product && isData" class="pt-24 pb-20">
      <SuccessModal
        :visible="showSuccess"
        message="The sale item has been updated."
      />

      <SuccessModal
        :visible="showAddSuccess"
        message="✅ Added to cart!"
        @close="showAddSuccess = false"
      />

      <ErrorModal
        :visible="showError"
        :message="errorMessage"
        @close="showError = false"
      />

      <ConfirmModal @confirm="confirm" :visible="showDelete" />
      <div class="max-w-[1200px] mx-auto px-6">
        <HistoryPath :previous="1" :name-path="title" /> 
        <div class="itbms-row grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16">
          <ProductPicture :editMode="false" :images="product.saleItemImages" />

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
                @click.stop="addToCart"
                class="w-full bg-neutral-800 text-white rounded-full py-3.5 font-medium text-sm hover:bg-neutral-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="product.quantity === 0"
                :title="product.quantity === 0 ? 'Out of stock' : ''"
              >
                Add to Cart
              </button>
            </div>

            <div class="border-t border-neutral-800 pt-6">
              <h2 class="text-xl font-medium mb-4">Overview</h2>
              <p class="itbms-description text-gray-400 leading-relaxed">
                {{ product.description }}
              </p>
            </div>

            <div class="border-t border-neutral-800 pt-6">
              <div class="flex justify-between items-center mb-4">
                <h2 class="text-xl font-medium">Specifications</h2>
              </div>

              <div class="text-gray-400 space-y-4 animate-fadeIn">
                <div class="grid grid-cols-3 gap-4">
                  <div class="bg-neutral-800/50 rounded-xl p-4 text-center">
                    <p class="text-xs text-gray-400 mb-1">RAM</p>
                    <p class="itbms-ramGb font-medium">
                      {{
                        product.ramGb === null || product.ramGb === ""
                          ? "-"
                          : product.ramGb
                      }}GB
                    </p>
                  </div>

                  <div class="bg-neutral-800/50 rounded-xl p-4 text-center">
                    <p class="text-xs text-gray-400 mb-1">Storage</p>
                    <p class="itbms-storageGb font-medium">
                      {{
                        product.storageGb === null || product.storageGb === ""
                          ? "-"
                          : product.storageGb
                      }}GB
                    </p>
                  </div>

                  <div class="bg-neutral-800/50 rounded-xl p-4 text-center">
                    <p class="text-xs text-gray-400 mb-1">Screen</p>
                    <p class="itbms-screenSizeInch font-medium">
                      {{
                        product.screenSizeInch === null ||
                        product.screenSizeInch === ""
                          ? "-"
                          : product.screenSizeInch
                      }}"
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
                    <p class="text-sm">
                      {{
                        product.storageGb === null || product.storageGb === ""
                          ? "-"
                          : product.storageGb
                      }}
                      GB
                    </p>
                  </div>

                  <div>
                    <h3 class="text-white text-sm font-medium mb-1">Color</h3>
                    <p class="itbms-color text-sm">
                      {{
                        product.color === null || product.color === ""
                          ? "-"
                          : product.color
                      }}
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

            <div class="pt-8 flex flex-col sm:flex-row gap-4" v-if="canManage">
              <button
                type="submit"
                @click="submit"
                class="itbms-edit-button flex-1 bg-white text-black py-4 px-6 rounded-full hover:bg-gray-200 transition-colors duration-300 font-medium"
              >
                Edit
              </button>
              <button
                @click="deleteSaleItem"
                type="button"
                class="itbms-delete-button flex-1 bg-neutral-800 text-white py-4 px-6 rounded-full hover:bg-red-500 transition-colors duration-300 font-medium"
              >
                Delete
              </button>
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