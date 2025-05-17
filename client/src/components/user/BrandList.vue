<script setup>
import { ref, onMounted, computed } from "vue";
import { useBrandStore } from "@/stores/useBrandStore";
import { useRouter } from "vue-router";
import SuccessModal from "../shared/modal/SuccessModal.vue";
import ConfirmModal from "../shared/modal/ConfirmModal.vue";
import ErrorModal from "../shared/modal/ErrorModal.vue";
import ListModel from "../shared/ListModel.vue";
import SkeletonLoader from "../shared/SkeletonLoader.vue";

const router = useRouter();
const brandStore = useBrandStore();

const brands = computed(() => brandStore.getBrands());
const isLoading = ref(true);
const showDeleteModal = ref(false);
const showSuccessModal = ref(false);
const showErrorModal = ref(false);

const selectedBrandId = ref(null);
const selectedBrandName = ref("");

const alertMessage = ref("");
const errorMessage = ref("");
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

const deleteBrand = (brandId, brandName) => {
  selectedBrandId.value = brandId;
  selectedBrandName.value = brandName;
  showDeleteModal.value = true;
};

const confirmDelete = async () => {
  try {
    const result = await brandStore.deleteBrand(selectedBrandId.value);

    if (result && result.error) {
      if (
        result.error.includes &&
        result.error.includes("associated with sale items")
      ) {
        errorMessage.value = `Cannot delete brand "${selectedBrandName.value}" because it has associated sale items.`;
      } else {
        errorMessage.value = brandStore.error || "Cannot delete this brand.";
      }
      showErrorModal.value = true;
      setTimeout(() => {
        showErrorModal.value = false;
      }, 5000);
    } else {
      sessionStorage.setItem("brand-delete-success", "true");
      alertMessage.value = "The brand has been deleted.";
      showSuccessModal.value = true;

      setTimeout(() => {
        showSuccessModal.value = false;
      }, 3000);
    }
  } catch (error) {
    console.error("Failed to delete brand:", error);

    if (
      error.response &&
      error.response.data &&
      error.response.data.message &&
      error.response.data.message.includes("associated sale items")
    ) {
      errorMessage.value = `Cannot delete brand "${selectedBrandName.value}" because it has associated sale items.`;
    } else {
      errorMessage.value =
        "An unexpected error occurred while deleting the brand.";
    }

    showErrorModal.value = true;

    setTimeout(() => {
      showErrorModal.value = false;
    }, 5000);
  } finally {
    showDeleteModal.value = false;
    selectedBrandId.value = null;
    selectedBrandName.value = "";
  }
};

const cancelDelete = () => {
  showDeleteModal.value = false;
  selectedBrandId.value = null;
};

const navigateToSaleItems = () => {
  router.push({ name: "product-list" });
};
</script>

<template>
  <div class="min-h-screen bg-black text-white pt-24 pb-16 px-4">
    <div class="max-w-7xl mx-auto">
      <div v-if="isLoading">
        <SkeletonLoader />
      </div>

      <div v-else>
        <ConfirmModal
          :visible="showDeleteModal"
          @confirm="confirmDelete"
          @cancel="cancelDelete"
          :message="`Are you sure you want to delete the brand '${selectedBrandName}'?`"
        />

        <SuccessModal :visible="showSuccessModal" :message="alertMessage" />
        <ErrorModal
          :visible="showErrorModal"
          :message="errorMessage"
          @close="showErrorModal = false"
          title="Cannot Delete Brand"
        />

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
            <template v-if="viewType === 'list'" class="itbms-row">
              <td class="px-6 py-4 itbms-brand-id">{{ brand.id }}</td>
              <td class="px-6 py-4 itbms-brand-name">{{ brand.name }}</td>
              <td class="px-6 py-4 itbms-brand-websiteUrl">
                {{ brand.websiteUrl || "-" }}
              </td>
              <td class="px-6 py-4 itbms-brand-countryOfOrigin">
                {{ brand.countryOfOrigin || "-" }}
              </td>
              <td class="px-6 py-4 itbms-brand-isActive">
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
                    class="itbms-delete-button bg-neutral-800 text-white px-3 py-1.5 rounded hover:bg-red-600 transition-colors duration-200 text-sm font-medium"
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
