<script setup>
import { useRoute, useRouter } from "vue-router";
import { useProductStore } from "@/stores/useProductStore";
import { onMounted, ref, computed } from "vue";
import ProductPicture from "@/components/shared/ProductPicture.vue";
import RemoveSaleItemPicture from "@/components/shared/RemoveSaleItemPicture.vue";
import HistoryPath from "@/components/shared/HistoryPath.vue";
import ConfirmModal from "@/components/shared/modal/ConfirmModal.vue";
import SuccessModal from "@/components/shared/modal/SuccessModal.vue";
import { checkUpToDate } from "@/utils/validate";

const router = useRouter();
const route = useRoute();
const productId = Number(route.params.productId);
const productStore = useProductStore();
const isLoading = ref(true);
const product = ref(null);
const isData = ref(true);
const isEditMode = ref(false);
const showDelete = ref(false);
const showSuccess = ref(false);

const submit = () => {
  isEditMode.value = true; // เปิด edit mode แสดง RemoveSaleItemPicture
}

const title = computed(() => {
  if (!product.value) return '';
  const { brandName = '', model = '', ramGb = '', storageGb = '', color = '' } = product.value;
  return `${brandName} ${model} ${ramGb}/${storageGb}GB ${color}`.trim();
});

const normalizedPictures = computed(() => {
  const raw = product.value?.pictures ?? product.value?.saleItemImages ?? [];
  return raw.map((img, idx) => ({
    id: img.id ?? img.imageId ?? img.saleItemImageId ?? idx+1,
    fileName: img.fileName ?? img.name ?? img.imageName ?? '',
    position: Number(img.position ?? img.seq ?? (idx + 1)),
  })).sort((a,b)=>a.position - b.position);
});

const deleteSaleItem = () => {
  showDelete.value = true;
}

const confirm = async () => {
  try {
    await productStore.deleteProduct(productId);
    sessionStorage.setItem("delete-success", "true");
    router.push("/sale-items");
    localStorage.setItem("activePage", 1);
    productStore.setActivePage(1)
  } catch (error) {
    sessionStorage.setItem("error-message", "true");
    router.push("/sale-items");
  }
};

const handlePicturesUpdate = (updatedPictures) => {
  // updatedPictures is array from BE: replace product.pictures
  if (product.value) product.value.pictures = updatedPictures;
};
function makePreviewEntries(files = []) {
  return files.map((file, idx) => {
    const objectUrl = URL.createObjectURL(file);
    return {
      id: `tmp-${Date.now()}-${idx}`,      // ชั่วคราว id แบบ string
      fileName: objectUrl,                // ให้ component ใช้เป็น src preview
      position: null,                     // จะรีคอมพิวต์ต่อไป
      isNew: true,                        // flag ว่าไฟล์ยังไม่อัปโหลด
      file: file                          // เก็บไฟล์ของจริง เพื่อใช้เมื่อจะอัปโหลดจริง
    };
  });
}
function handleNewUploads(files) {
  if (!product.value) return;
  // เอา existing visible pictures (ถ้ามี) และต่อด้วย preview ใหม่
  const existing = (product.value.pictures ?? product.value.saleItemImages ?? [])
    .map((p, idx) => ({
      id: p.id ?? p.imageId ?? `exist-${idx}`,
      fileName: p.fileName ?? p.name ?? p.imageName ?? '',
      position: Number(p.position ?? p.seq ?? (idx + 1)),
      isNew: false
    }));

  const previews = makePreviewEntries(files);

  // รวมกัน และตั้ง position ชั่วคราวเป็น 1..N
  const combined = [...existing, ...previews];
  combined.sort((a,b) => (a.position || 9999) - (b.position || 9999));
  combined.forEach((p, i) => p.position = i + 1);

  // เขียนกลับไปที่ product (so RemoveSaleItemPicture จะเห็น)
  product.value.pictures = combined;

  // ถ้าต้องการให้ store.selectedProduct ก็ sync ด้วย
  productStore.selectedProduct = product.value;
}
function handleSaved() {
  // Ensure we always exit edit mode (finally-like)
  try {
    if (typeof window !== 'undefined' && window.sessionStorage) {
      try {
        sessionStorage.setItem('edit-success', 'true');
      } catch (e) {
        // Some browsers / privacy modes throw when setItem is blocked — log and continue
        console.warn('sessionStorage.setItem failed (ignored):', e);
      }
    }
  } finally {
    // Always exit edit mode so UI doesn't hang
    isEditMode.value = false;
  }
}
onMounted(async () => {
  isLoading.value = true;
  try {
    await productStore.loadProducts();
    const p = await productStore.fetchProductDetail(productId);
    product.value = p;
    // sync store's selectedProduct as well (useful)
    productStore.selectedProduct = p;

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
    setTimeout(() => { showSuccess.value = false; }, 3000);
  }

  isLoading.value = false;
});
</script>

<template>
  <div class="min-h-screen bg-black text-white">
    <div v-if="!isLoading && product && isData" class="pt-24 pb-20">
      <SuccessModal :visible="showSuccess" message="The sale item has been updated." />
      <ConfirmModal @confirm="confirm" :visible="showDelete" />

      <div class="max-w-[1200px] mx-auto px-6">
        <HistoryPath :previous="1" :name-path="title" /> 
        <div class="itbms-row grid grid-cols-1 lg:grid-cols-2 gap-12 lg:gap-16">
         <!-- in ProductDetail.vue template -->
<ProductPicture
  v-if="!isEditMode"
  :images="product.saleItemImages"
  @savePic="handleNewUploads"
/>

<RemoveSaleItemPicture
  v-else
  :sale-item="product"
  :initial-pictures="normalizedPictures"
  @update="handlePicturesUpdate"
  @saved="handleSaved"
  @cancel="() => { isEditMode=false; }"
/>

          <div class="flex flex-col gap-8">
            <!-- Product Info -->
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
                <span class="itbms-storageGb">{{ product.storageGb === null ? "-" : product.storageGb }}</span>
                <span class="itbms-storageGb-unit ml-1">GB</span>
                <span class="mx-2">•</span>
                <span class="itbms-color">{{ product.color === null ? "-" : product.color }}</span>
              </div>
            </div>

            <div class="itbms-price text-3xl font-medium">
              ฿{{ product.price.toLocaleString() }}
            </div>

            <div class="space-y-4">
              <button class="w-full bg-white text-black rounded-full py-3.5 font-medium text-sm hover:bg-gray-200 transition-colors">
                Buy
              </button>
              <button class="w-full bg-neutral-800 text-white rounded-full py-3.5 font-medium text-sm hover:bg-neutral-700 transition-colors">
                Add to Bag
              </button>
            </div>

            <!-- Overview, Specs ฯลฯ เหมือนเดิม -->
            <div class="border-t border-neutral-800 pt-6">
              <h2 class="text-xl font-medium mb-4">Overview</h2>
              <p class="itbms-description text-gray-400 leading-relaxed">{{ product.description }}</p>
            </div>

            <!-- Edit / Delete Buttons -->
            <div class="pt-8 flex flex-col sm:flex-row gap-4">
              <button type="submit" @click="submit" class="itbms-edit-button flex-1 bg-white text-black py-4 px-6 rounded-full hover:bg-gray-200 transition-colors duration-300 font-medium">
                Edit
              </button>
              <button @click="deleteSaleItem" type="button" class="itbms-delete-button flex-1 bg-neutral-800 text-white py-4 px-6 rounded-full hover:bg-red-500 transition-colors duration-300 font-medium">
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
.animate-fadeIn {
  animation: fadeIn 0.3s ease-out forwards;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>
