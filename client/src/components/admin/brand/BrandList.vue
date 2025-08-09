<script setup>
import { ref, onMounted, computed } from "vue";
import { useBrandStore } from "@/stores/useBrandStore";
import { useRouter } from "vue-router";
import SuccessModal from "@/components/shared/modal/SuccessModal.vue";
import ConfirmModal from "@/components/shared/modal/ConfirmModal.vue";
import ListModel from "@/components/shared/ListModel.vue";
import SkeletonLoader from "@/components/shared/SkeletonLoader.vue";

const router = useRouter();
const brandStore = useBrandStore();
const brands = computed(() => brandStore.getBrands());
const isLoading = ref(true);
const showDeleteModal = ref(false);
const showErrorModal = ref(false);
const showSuccessModal = ref(false);
const showNotFoundModal = ref(false);
const selectedBrandId = ref(null);
const selectedBrandName = ref("");
const alertMessage = ref("");
const errorMessage = ref("");
const notFoundMessage = ref("");
const viewType = ref("list");

onMounted(async () => {
  try {
    await brandStore.loadBrands();
  } catch (error) {
    console.error("Failed to load brands:", error);
  } finally {
    isLoading.value = false;
  }

  if (sessionStorage.getItem("brand-add-success") === "true") {
    alertMessage.value = "The brand has been added.";
    showSuccessModal.value = true;
    sessionStorage.removeItem("brand-add-success");
    setTimeout(() => {
      showSuccessModal.value = false;
    }, 3000);
  }

  if (sessionStorage.getItem("brand-edit-success") === "true") {
    alertMessage.value = "The brand has been updated.";
    showSuccessModal.value = true;
    sessionStorage.removeItem("brand-edit-success");
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

const addBrand = () => {
  router.push({ name: "brand-add" });
};

const editBrand = (brandId) => {
  router.push({ name: "brands-edit", params: { brandId } });
};

const deleteBrand = async (brandId, brandName) => {
  selectedBrandId.value = brandId;
  selectedBrandName.value = brandName;

  try {
    const brandInfo = await brandStore.getBrandById(brandId);

    if (brandInfo.noOfSaleItems > 0) {
      errorMessage.value = `Delete ${brandName} is not allowed. There are sale items with ${brandName} brand.`;
      showErrorModal.value = true;
      return;
    }

    showDeleteModal.value = true;
  } catch (error) {
    console.error("Failed to check brand details:", error);
    showDeleteModal.value = true;
  }
};

const confirmDelete = async () => {
  try {
    await brandStore.deleteBrand(selectedBrandId.value);
    await brandStore.loadBrands();

    alertMessage.value = "The brand has been deleted.";
    showSuccessModal.value = true;
    setTimeout(() => {
      showSuccessModal.value = false;
    }, 3000);
  } catch (error) {
    console.error("Failed to delete brand:", error);

    if (error.response && error.response.status === 409) {
      errorMessage.value = `Delete ${selectedBrandName.value} is not allowed. There are sale items with ${selectedBrandName.value} brand.`;
      showErrorModal.value = true;
    } else {
      notFoundMessage.value =
        "An error has occurred, the brand does not exist.";
      showNotFoundModal.value = true;
    }
  } finally {
    showDeleteModal.value = false;
    selectedBrandId.value = null;
    selectedBrandName.value = "";
  }
};

const cancelDelete = () => {
  showDeleteModal.value = false;
  selectedBrandId.value = null;
  selectedBrandName.value = "";
};

const navigateToSaleItems = () => {
  router.push({ name: "product-list" });
};

const closeErrorModal = () => {
  showErrorModal.value = false;
};
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
          :message="`Do you want to delete ${selectedBrandName} brand?`"
          class="itbms-message"
        />

        <SuccessModal
          :visible="showSuccessModal"
          :message="alertMessage"
          class="itbms-message"
        />

        <div
          v-if="showErrorModal"
          class="fixed inset-0 z-50 flex items-center justify-center px-4"
          @click="closeErrorModal"
        >
          <div class="absolute inset-0 bg-black/30 backdrop-blur-md"></div>

          <div
            class="relative bg-white rounded-2xl overflow-hidden max-w-md w-full transform transition-all shadow-xl"
            @click.stop
          >
            <div class="p-6 sm:p-8">
              <div class="text-center mb-6">
                <h3 class="text-xl font-semibold text-gray-900 mb-2">
                  Delete Confirmation
                </h3>
                <p class="text-gray-600 itbms-message">
                  {{ errorMessage }}
                </p>
              </div>

              <div class="flex flex-col space-y-3">
                <button
                  @click="closeErrorModal"
                  class="itbms-cancel-button w-full py-3 px-4 rounded-full bg-gray-200 text-gray-800 font-medium hover:bg-gray-300 transition-colors focus:outline-none focus:ring-2 focus:ring-gray-500 focus:ring-offset-2"
                >
                  Cancel
                </button>
              </div>
            </div>
          </div>
        </div>

        <div
          v-if="showNotFoundModal"
          class="fixed inset-0 z-50 flex items-center justify-center px-4"
        >
          <div class="absolute inset-0 bg-black/30 backdrop-blur-md"></div>

          <div
            class="relative bg-white rounded-2xl overflow-hidden max-w-md w-full transform transition-all shadow-xl"
          >
            <div class="p-6 sm:p-8">
              <div class="text-center mb-6">
                <h3 class="text-xl font-semibold text-gray-900 mb-2">Error</h3>
                <p class="text-gray-600 itbms-message">
                  {{ notFoundMessage }}
                </p>
              </div>
              <div class="flex justify-center">
                <button
                  @click="showNotFoundModal = false"
                  class="py-2 px-4 bg-gray-200 rounded-full text-gray-800"
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>

        <div class="flex justify-between items-center mb-8">
          <div class="flex cursor-pointer font-light space-x-2.5">
            <p class="itbms-item-list" @click="navigateToSaleItems">
              Sale Item List
            </p>
            <p>/</p>
            <p
              class="bg-gradient-to-r from-neutral-100 to-blue-200 text-black px-4 rounded-3xl"
            >
              Brand List
            </p>
          </div>

          <button
            @click="addBrand"
            class="itbms-add-button bg-white text-black px-4 py-2 rounded hover:bg-gray-200 transition-colors duration-200 font-medium"
          >
            Add Brand
          </button>
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
              <td class="px-6 py-4 itbms-id">{{ brand.id }}</td>
              <td class="px-6 py-4 itbms-name">{{ brand.name }}</td>
              <td class="px-6 py-4 itbms-websiteUrl">
                {{ brand.websiteUrl || "-" }}
              </td>
              <td class="px-6 py-4 itbms-countryOfOrigin">
                {{ brand.countryOfOrigin || "-" }}
              </td>
              <td class="px-6 py-4 itbms-isActive">
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
                    @click="deleteBrand(brand.id, brand.name)"
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
