<script setup>
import { ref, onMounted, computed,watch } from "vue";
import { useProductStore } from "@/stores/useProductStore";
import ListModel from "../shared/ListModel.vue";
import { useRouter } from "vue-router";
import ConfirmModal from "../shared/modal/ConfirmModal.vue";
import SuccessModal from "../shared/modal/SuccessModal.vue";
import HistoryPath from "../shared/HistoryPath.vue";

const router = useRouter();
const productStore = useProductStore();
const isLoading = ref(true);
const viewType = ref("list");
const products = ref([]);
const selectedProductId = ref(null);
const showDeleteModal = ref(false);
const showSuccessModal = ref(false);
const alertMessage = ref("");

const sortedProducts = computed(() => {
  if (!products.value || products.value.length === 0) return [];

  return [...products.value].sort((productA, productB) => {
    const dateProductA = new Date(productA.createdOn);
    const dateProductB = new Date(productB.createdOn);
    return dateProductA - dateProductB;
  });
});

const handleItemClick = (productId) => {
  router.push({
    name: "product-detail",
    params: { productId },
    query: { from: 'list' }
  });
};

const handleEditClick = (productId) => {
  router.push({
    path: `/sale-items/${productId}/edit`,
    query: { 
      from: 'list',
      returnTo: 'list'
    }
  });
};

const handleDeleteClick = (productId) => {
  selectedProductId.value = productId;
  showDeleteModal.value = true;
};

const confirmDelete = async () => {
  try {
    await productStore.deleteProduct(selectedProductId.value);
    products.value = products.value.filter(
      (p) => p.id !== selectedProductId.value
    );

    alertMessage.value = "The sale item has been deleted.";
    showSuccessModal.value = true;

    setTimeout(() => {
      showSuccessModal.value = false;
    }, 3000);
  } catch (error) {
    console.error("Failed to delete product:", error);
  } finally {
    showDeleteModal.value = false;
  }
};

const cancelDelete = () => {
  showDeleteModal.value = false;
  selectedProductId.value = null;
};

async function fetchAllProductDetails() {
  try {
    await productStore.loadProducts();
    const productsList = productStore.allProducts;
    if (!productsList || productsList.length === 0) {
      products.value = [];
      router.push({ path: "/error", query: { code: "NODATA" } });
      return;
    }

    const detailedProducts = [];
    for (const product of productsList) {
      const detailedProduct = await productStore.fetchProductDetail(product.id);
      if (detailedProduct) {
        detailedProducts.push(detailedProduct);
      }
    }
    if (detailedProducts.length === 0) {
      router.push({ path: "/error", query: { code: "NODATA" } });
      return;
    }
    products.value = detailedProducts;
  } catch (error) {
    console.error("Failed to fetch product details:", error);
    router.push({
      path: "/error",
      query: { code: "ERROR", message: error.message },
    });
  } finally {
    isLoading.value = false;
  }
}

onMounted(async () => {
  await fetchAllProductDetails();

  checkForSuccess();
});

const checkForSuccess = () => {
  if (sessionStorage.getItem("edit-success") === "true") {
    alertMessage.value = "The sale item has been updated.";
    showSuccessModal.value = true;
    sessionStorage.removeItem("edit-success");

    setTimeout(() => {
      showSuccessModal.value = false;
    }, 3000);
  }
  
  if (sessionStorage.getItem("delete-success") === "true") {
    alertMessage.value = "The sale item has been deleted.";
    showSuccessModal.value = true;
    sessionStorage.removeItem("delete-success");

    setTimeout(() => {
      showSuccessModal.value = false;
    }, 3000);
  }
};

watch(
  () => router.currentRoute.value.fullPath,
  () => {
    checkForSuccess();
  }
);
</script>

<template>
  <div class="min-h-screen bg-black text-white pt-24 pb-16 px-4">
    <div class="max-w-7xl mx-auto">
      <div v-if="isLoading" class="text-center py-12">
        <div
          class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-white mx-auto"
        ></div>
        <p class="mt-4 text-gray-400">Loading sale items...</p>
      </div>

      <div v-else>
        <!-- Modals -->
        <ConfirmModal
          :visible="showDeleteModal"
          @confirm="confirmDelete"
          @cancel="cancelDelete"
        />

        <SuccessModal :visible="showSuccessModal" :message="alertMessage" />

        <div class="flex justify-between mb-6 space-x-4">
          <div>
            <HistoryPath
              name-path="Sale Items List"
              :previous="1"
              previousPathName="Sale Items"
              previousPath="/sale-items"
            />
          </div>
          <div class="flex space-x-4">
            <router-link
              :to="{ path: '/sale-items/add', query: { from: 'list' } }"
              class="itbms-sale-item-add bg-white text-black px-4 py-2 rounded hover:bg-gray-200"
            >
              Add Sale Item
            </router-link>
            <router-link
              to="/brands"
              class="itbms-manage-brand bg-neutral-800 text-white px-4 py-2 rounded hover:bg-neutral-700 hover:ring-1 hover:ring-white transition-colors duration-200 font-medium"
            >
              Manage Brand
            </router-link>
          </div>
        </div>

        <ListModel
          :items="sortedProducts"
          :viewType="viewType"
          @update:viewType="viewType = $event"
        >
          <template #tableHeader>
            <th class="px-4 py-3 font-medium">ID</th>
            <th class="px-4 py-3 font-medium">Brand</th>
            <th class="px-4 py-3 font-medium">Model</th>
            <th class="px-4 py-3 font-medium">Ram (GB)</th>
            <th class="px-4 py-3 font-medium">Storage (GB)</th>
            <th class="px-4 py-3 font-medium">Color</th>
            <th class="px-4 py-3 font-medium">Screen Size (Inches)</th>
            <th class="px-4 py-3 font-medium">Price (THB)</th>
            <th class="px-4 py-3 font-medium">Quantity</th>
            <th class="px-4 py-3 font-medium">Actions</th>
          </template>

          <template #listItems="{ Item: product, viewType }">
            <template v-if="viewType === 'list'">
              <td
                @click="handleItemClick(product.id)"
                class="px-4 py-3 itbms-id"
              >
                {{ product.id }}
              </td>
              <td
                @click="handleItemClick(product.id)"
                class="px-4 py-3 itbms-brand"
              >
                {{ product.brandName }}
              </td>
              <td
                @click="handleItemClick(product.id)"
                class="px-4 py-3 itbms-model"
              >
                {{ product.model }}
              </td>
              <td
                @click="handleItemClick(product.id)"
                class="px-4 py-3 itbms-ramGb"
              >
                {{ product.ramGb || "-" }}
              </td>
              <td
                @click="handleItemClick(product.id)"
                class="px-4 py-3 itbms-storageGb"
              >
                {{ product.storageGb || "-" }}
              </td>
              <td
                @click="handleItemClick(product.id)"
                class="px-4 py-3 itbms-color"
              >
                {{ product.color || "-" }}
              </td>
              <td
                @click="handleItemClick(product.id)"
                class="px-4 py-3 itbms-screenSizeInch"
              >
                {{ product.screenSizeInch || "-" }}
              </td>
              <td
                @click="handleItemClick(product.id)"
                class="px-4 py-3 itbms-price"
              >
                {{ product.price?.toLocaleString() || "-" }}
              </td>
              <td
                @click="handleItemClick(product.id)"
                class="px-4 py-3 itbms-quantity"
              >
                {{ product.quantity || "-" }}
              </td>
              <td class="px-4 py-3">
                <div class="flex space-x-2">
                  <button
                    @click.stop="handleEditClick(product.id)"
                    class="itbms-edit-button bg-white text-black px-3 py-1.5 rounded hover:bg-gray-300 transition-colors duration-200"
                  >
                    EDIT
                  </button>
                  <button
                    @click.stop="handleDeleteClick(product.id)"
                    class="itbms-delete-button bg-neutral-800 text-white px-3 py-1.5 rounded hover:bg-black transition-colors duration-200 text-sm font-medium"
                  >
                    DELETE
                  </button>
                </div>
              </td>
            </template>
          </template>
        </ListModel>
      </div>
    </div>
  </div>
</template>

<style scoped>
.itbms-row:hover td {
  background-color: rgba(255, 255, 255, 0.05);
  cursor: pointer;
}
</style>
