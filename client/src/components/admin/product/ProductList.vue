<script setup>
import { ref, onMounted, computed } from "vue";
import { useAuthStore } from "@/stores/useAuthStore";
import ListModel from "@/components/shared/ListModel.vue";
import { useRouter } from "vue-router";
import ConfirmModal from "@/components/shared/modal/ConfirmModal.vue";
import SuccessModal from "@/components/shared/modal/SuccessModal.vue";
import ErrorModal from "@/components/shared/modal/ErrorModal.vue";
import SkeletonLoader from "@/components/shared/SkeletonLoader.vue";
import Pagination from "@/components/shared/Pagination.vue";

const router = useRouter();
const auth = useAuthStore();
const isLoading = ref(true);
const viewType = ref("list");
const selectedProductId = ref(null);
const showDeleteModal = ref(false);
const showSuccessModal = ref(false);
const showErrorModal = ref(false);
const alertMessage = ref("");
const errorMessage = ref("");
const sellerProducts = ref([]);

// Pagination state
const currentPage = ref(0); // Backend uses 0-based indexing
const totalPages = ref(0);
const pageSize = ref(10);

// Use sellerProducts instead of productStore
const products = computed(() => sellerProducts.value);

const handleItemClick = (productId) => {
  router.push({
    name: "product-detail",
    params: { productId },
  });
};

const handleEditClick = (productId) => {
  router.push({
    path: `/sale-items/${productId}/edit`,
  });
};

const handleDeleteClick = (productId) => {
  selectedProductId.value = productId;
  showDeleteModal.value = true;
};

const confirmDelete = async () => {
  try {
    // Delete product using API call directly
    const response = await fetch(
      `${import.meta.env.VITE_BASE_URL}/v1/sale-items/${selectedProductId.value}`,
      {
        method: 'DELETE',
        headers: {
          Authorization: `Bearer ${sessionStorage.getItem("accessToken")}`,
          "Content-Type": "application/json",
        },
      }
    );

    if (!response.ok) {
      throw new Error('Failed to delete product');
    }

    // Reload seller's products after deletion
    await loadSellerProducts();

    alertMessage.value = "The sale item has been deleted.";
    showSuccessModal.value = true;
    sessionStorage.setItem("delete-success", "true");

    setTimeout(() => {
      showSuccessModal.value = false;
    }, 3000);
  } catch (error) {
    console.error("Failed to delete product:", error);
    errorMessage.value = "Failed to delete the sale item. Please try again.";
    showErrorModal.value = true;
  } finally {
    showDeleteModal.value = false;
  }
};

const cancelDelete = () => {
  showDeleteModal.value = false;
  selectedProductId.value = null;
};

// New function to load seller's products using PBI25 API
async function loadSellerProducts(page = currentPage.value) {
  try {
    if (!auth.user || !auth.user.id) {
      throw new Error('User not authenticated');
    }

    const response = await fetch(
      `${import.meta.env.VITE_BASE_URL}/v2/sellers/${auth.user.id}/sale-items?page=${page}&size=${pageSize.value}`,
      {
        headers: {
          Authorization: `Bearer ${sessionStorage.getItem("accessToken")}`,
          "Content-Type": "application/json",
        },
      }
    );

    if (!response.ok) {
      if (response.status === 401) {
        throw new Error('Unauthorized - Invalid token');
      } else if (response.status === 403) {
        throw new Error('Forbidden - User not active or access denied');
      } else if (response.status === 400) {
        throw new Error('Bad Request - Invalid parameters');
      }
      throw new Error(`HTTP ${response.status}: ${response.statusText}`);
    }

    const data = await response.json();
    sellerProducts.value = data.content || [];
    totalPages.value = data.totalPages || 0;
    currentPage.value = page;
    
    if (sellerProducts.value.length === 0 && totalPages.value === 0) {
      router.push({ name: "error-page", query: { code: "NODATA" } });
      return;
    }
  } catch (error) {
    console.error("Failed to load seller products:", error);
    errorMessage.value = error.message || "Failed to load sale items. Please try again.";
    showErrorModal.value = true;
  }
}

async function initProducts() {
  try {
    await loadSellerProducts();
  } catch (err) {
    console.error("Load products failed:", err);
  } finally {
    isLoading.value = false;
  }
}

// Handle pagination events
const handlePageChange = async (page) => {
  // Convert from 1-based (UI) to 0-based (backend)
  const backendPage = page - 1;
  isLoading.value = true;
  try {
    await loadSellerProducts(backendPage);
  } catch (err) {
    console.error("Page change failed:", err);
  } finally {
    isLoading.value = false;
  }
};

onMounted(async () => {
  await initProducts();

  if (sessionStorage.getItem("add-success") === "true") {
    alertMessage.value = "The sale item has been successfully added.";
    showSuccessModal.value = true;  
    sessionStorage.removeItem("add-success");
    setTimeout(() => {
      showSuccessModal.value = false;
    }, 2000);
  }

  if (sessionStorage.getItem("delete-success") === "true") {
    alertMessage.value = "The sale item has been deleted.";
    showSuccessModal.value = true;
    sessionStorage.removeItem("delete-success");

    setTimeout(() => {
      showSuccessModal.value = false;
    }, 3000);
  }
});
</script>

<template>
  <div class="min-h-screen bg-black text-white pt-24 pb-16 px-4">
    <div class="max-w-7xl mx-auto">
      <div v-if="isLoading" class="text-center py-12">
        <div class="flex flex-wrap justify-center gap-4">
          <SkeletonLoader />
        </div>
      </div>

      <div v-else>
        <ConfirmModal
          :visible="showDeleteModal"
          @confirm="confirmDelete"
          @cancel="cancelDelete"
          message="Do you want to delete this sale item?"
          class="itbms-message"
        />

        <SuccessModal :visible="showSuccessModal" :message="alertMessage" class="itbms-message" />
        
        <ErrorModal :visible="showErrorModal" :message="errorMessage" @close="showErrorModal = false" />

        <div class="flex justify-end mb-6 space-x-4">
          <router-link
            to="/sale-items/add"
            class="itbms-sale-item-add bg-white text-black px-4 py-2 rounded hover:bg-gray-200 transition-colors duration-200 font-medium"
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

        <ListModel
          :saleItems="products"
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
            <th class="px-4 py-3 font-medium">Price (THB)</th>
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
                class="px-4 py-3 itbms-price"
              >
                {{ product.price?.toLocaleString() || "-" }}
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

    <!-- Pagination Component -->
    <Pagination 
      v-if="!isLoading && totalPages > 1" 
      :totalPages="totalPages" 
      @sendPages="handlePageChange" 
    />
  </div>
</template>

<style scoped>
.itbms-row:hover td {
  background-color: rgba(255, 255, 255, 0.05);
  cursor: pointer;
}
</style>