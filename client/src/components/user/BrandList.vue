<script setup>
import { ref, onMounted, computed } from "vue";
import { useBrandStore } from "@/stores/useBrandStore";
import { useRouter } from "vue-router";
import SuccessModal from "../shared/modal/SuccessModal.vue";
import ConfirmModal from "../shared/modal/ConfirmModal.vue";
import ListModel from "../shared/ListModel.vue";

const router = useRouter();
const brandStore = useBrandStore();
const brands = computed(() => brandStore.getBrands());
const isLoading = ref(true);
const showDeleteModal = ref(false);
const showSuccessModal = ref(false);
const selectedBrandId = ref(null);
const alertMessage = ref("");
const viewType = ref("list"); // Default to list view

// Load brands on component mount
onMounted(async () => {
  try {
    await brandStore.loadBrands();
  } catch (error) {
    console.error("Failed to load brands:", error);
  } finally {
    isLoading.value = false;
  }

  // Check for success message from session storage
  if (sessionStorage.getItem("brand-add-success") === "true") {
    alertMessage.value = "The brand has been added.";
    showSuccessModal.value = true;
    sessionStorage.removeItem("brand-add-success");
    setTimeout(() => {
      showSuccessModal.value = false;
    }, 3000);
  }

  if (sessionStorage.getItem("brand-delete-success") === "true") {
    alertMessage.value = "The brand has been deleted.";
    showSuccessModal.value = true;
    sessionStorage.removeItem("brand-delete-success");
    setTimeout(() => {
      showSuccessModal.value = false;
    }, 3000);
  }
});

// Navigate to add brand page
const addBrand = () => {
  router.push({ name: "brand-add" });
};

// Navigate to edit brand page
const editBrand = (brandId) => {
  router.push({ name: "brands-edit", params: { brandId } });
};

// Show delete confirmation modal
const deleteBrand = (brandId) => {
  selectedBrandId.value = brandId;
  showDeleteModal.value = true;
};

// Confirm brand deletion
const confirmDelete = async () => {
  try {
    await brandStore.deleteBrand(selectedBrandId.value);
    await brandStore.loadBrands(); // Reload brands

    alertMessage.value = "The brand has been deleted.";
    showSuccessModal.value = true;
    setTimeout(() => {
      showSuccessModal.value = false;
    }, 3000);
  } catch (error) {
    console.error("Failed to delete brand:", error);
    alertMessage.value = "The brand could not be deleted.";
    showSuccessModal.value = true;
  } finally {
    showDeleteModal.value = false;
    selectedBrandId.value = null;
  }
};

// Cancel deletion
const cancelDelete = () => {
  showDeleteModal.value = false;
  selectedBrandId.value = null;
};

// Navigate to sale items list
const navigateToSaleItems = () => {
  router.push({ name: "product-list" });
};
</script>

<template>
  <div class="min-h-screen bg-black text-white pt-24 pb-16 px-4">
    <div class="max-w-7xl mx-auto">
      <!-- Loading state -->
      <div v-if="isLoading" class="text-center py-12">
        <div
          class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-white mx-auto"
        ></div>
        <p class="mt-4">Loading brands...</p>
      </div>

      <!-- Loaded content -->
      <div v-else>
        <!-- Modals -->
        <ConfirmModal
          :visible="showDeleteModal"
          @confirm="confirmDelete"
          @cancel="cancelDelete"
          message="Are you sure you want to delete this brand?"
        />

        <SuccessModal :visible="showSuccessModal" :message="alertMessage" />

        <div class="flex justify-between items-center mb-8">
          <div class="flex space-x-4">
            <button
              @click="navigateToSaleItems"
              class="itbms-item-list bg-neutral-800 text-white px-4 py-2 rounded hover:bg-neutral-700 transition-colors duration-200 font-medium"
            >
              Sale Item List
            </button>
            <button
              @click="addBrand"
              class="itbms-add-button bg-white text-black px-4 py-2 rounded hover:bg-gray-200 transition-colors duration-200 font-medium"
            >
              Add Brand
            </button>
          </div>
        </div>

        <ListModel :saleItems="brands" v-model:viewType="viewType">
          <template #tableHeader>
            <th class="px-6 py-3 text-left font-medium">ID</th>
            <th class="px-6 py-3 text-left font-medium">Brand Name</th>
            <th class="px-6 py-3 text-left font-medium">Website URL</th>
            <th class="px-6 py-3 text-left font-medium">Country</th>
            <th class="px-6 py-3 text-left font-medium">Status</th>
            <th class="px-6 py-3 text-center font-medium">Actions</th>
          </template>

          <template #listItems="{ Item: brand, viewType }">
            <template v-if="viewType === 'list'">
              <td class="px-6 py-4 itbms-brand-id">{{ brand.id }}</td>
              <td class="px-6 py-4 itbms-brand-name">{{ brand.name }}</td>
              <td class="px-6 py-4 itbms-brand-website">
                {{ brand.websiteUrl || "-" }}
              </td>
              <td class="px-6 py-4 itbms-brand-country">
                {{ brand.countryOfOrigin || "-" }}
              </td>
              <td class="px-6 py-4 itbms-brand-active">
                <span
                  :class="
                    brand.isActive
                      ? 'bg-green-100 text-green-800'
                      : 'bg-red-100 text-red-800'
                  "
                  class="px-2 py-1 text-xs font-medium rounded-full"
                >
                  {{ brand.isActive ? "Active" : "Inactive" }}
                </span>
              </td>
              <td class="px-6 py-4 text-center">
                <div class="flex justify-center space-x-3">
                  <button
                    @click="editBrand(brand.id)"
                    class="itbms-edit-button bg-white text-black px-3 py-1.5 rounded hover:bg-gray-300 transition-colors duration-200"
                  >
                    Edit
                  </button>
                  <button
                    @click="deleteBrand(brand.id)"
                    class="itbms-delete-button bg-neutral-800 text-white px-3 py-1.5 rounded hover:bg-black transition-colors duration-200 text-sm font-medium"
                  >
                    Delete
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
.itbms-row:hover {
  background-color: rgba(255, 255, 255, 0.05);
}
</style>
