<script setup>
import { ref, onMounted, getCurrentInstance } from "vue";
import { useBrandStore } from "@/stores/useBrandStore";
import { useRouter } from "vue-router";
import SuccessModal from "../shared/modal/SuccessModal.vue";
import ListModel from "../shared/ListModel.vue";
import HistoryPath from "../shared/HistoryPath.vue";
const { proxy } = getCurrentInstance();
const router = useRouter();
const brandStore = useBrandStore();
const isLoading = ref(true);
const viewType = ref("list");
const showSuccessModal = ref(false);
const alertMessage = ref("");

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
});

const formatDate = (dateString) => {
  if (!dateString) return "-";
  return proxy.$formatToThaiTime(dateString);
};

const handleAddBrand = () => {
  router.push("/brands/add");
};

const goToBrandEdit = (brandId) => {
  router.push({
    name: 'brands-edit',
    params: { brandId }
  });
};

</script>

<template>
  <div class="min-h-screen bg-black text-white pt-24 pb-16 px-4">
    <div class="max-w-7xl mx-auto">

      <div v-if="isLoading" class="text-center py-12">
        <div
          class="animate-spin rounded-full h-12 w-12 border-t-2 border-b-2 border-white mx-auto"
        ></div>
        <p class="mt-4 text-gray-400">Loading brands...</p>
      </div>

      <div v-else>
        <SuccessModal :visible="showSuccessModal" :message="alertMessage" />

        <div class="flex justify-between mb-6">
          <HistoryPath
            name-path="Manage brand"
            :previous="1"
            previousPathName="Sale Items List"
            previousPath="/sale-items/list"
          />

          <button
            @click="handleAddBrand"
            class="itbms-brand-add bg-white text-black px-4 py-2 rounded hover:bg-gray-200 transition-colors duration-200 font-medium"
          >
            Add Brand
          </button>
        </div>

        <ListModel
          :items="brandStore.brands"
          :viewType="viewType"
          @update:viewType="viewType = $event"
        >
          <template #tableHeader>
            <th class="px-4 py-3 font-medium">ID</th>
            <th class="px-4 py-3 font-medium">Name</th>
            <th class="px-4 py-3 font-medium">Website URL</th>
            <th class="px-4 py-3 font-medium">Active</th>
            <th class="px-4 py-3 font-medium">Country</th>
            <th class="px-4 py-3 font-medium">Created On</th>
            <th class="px-4 py-3 font-medium">Updated On</th>
            <th class="px-4 py-3 font-medium">Actions</th>
          </template>

          <template #listItems="{ Item: brand, viewType }">
            <template v-if="viewType === 'list'">
              <td class="px-4 py-3 itbms-brand-id">
                {{ brand.id }}
              </td>
              <td class="px-4 py-3 itbms-brand-name">
                {{ brand.name }}
              </td>
              <td class="px-4 py-3 itbms-brand-url">
                {{ brand.websiteUrl || "-" }}
              </td>
              <td class="px-4 py-3 itbms-brand-active">
                {{ brand.isActive === true ? "Yes" : "No" }}
              </td>
              <td class="px-4 py-3 itbms-brand-country">
                {{ brand.countryOfOrigin || "-" }}
              </td>
              <td class="px-4 py-3 itbms-brand-created">
                {{ formatDate(brand.createdOn) }}
              </td>
              <td class="px-4 py-3 itbms-brand-updated">
                {{ formatDate(brand.updatedOn) }}
              </td>
              <td class="px-4 py-3">
                <div class="flex space-x-2">
                  <button
                    @click.stop="goToBrandEdit(brand.id)"
                    class="itbms-brand-edit-button bg-white text-black px-3 py-1.5 rounded hover:bg-gray-300 transition-colors duration-200"
                  >
                    EDIT
                  </button>
                  <button
                    class="itbms-brand-delete-button bg-neutral-800 text-white px-3 py-1.5 rounded hover:bg-black transition-colors duration-200 text-sm font-medium"
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
td {
  cursor: pointer;
}
</style>
