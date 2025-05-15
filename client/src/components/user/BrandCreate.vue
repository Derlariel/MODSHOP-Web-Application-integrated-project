<script setup>
import { useRouter } from "vue-router";
import { computed } from "vue";
import BrandForm from "./BrandForm.vue";
import { useBrandStore } from "@/stores/useBrandStore";
import HistoryPath from "../shared/HistoryPath.vue";

const router = useRouter();
const brandStore = useBrandStore();

const add = async (data) => {
  const result = await brandStore.addBrand(data);
  
  if (result.success) {
    sessionStorage.setItem("brand-add-success", "true");
    router.push("/brands");
  }
};

const previousPath = computed(() => {
  return {
    name: "Brand List",
    path: "/brands"
  };
});
</script>
 
<template>
  <div class="min-h-screen bg-black text-white">
    <div class="pt-24 pb-20">
      <div class="max-w-[1200px] mx-auto px-6">
        <HistoryPath
          name-path="Add Brand"
          :previous="1"
          :previousPathName="previousPath.name"
          :previousPath="previousPath.path"
        />
        <div class="max-w-2xl mx-auto mt-10">
          <BrandForm @submit="add" />
        </div>
      </div>
    </div>
  </div>
</template>
 
<style scoped></style>