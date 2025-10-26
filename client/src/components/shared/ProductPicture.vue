<script setup>
import { ref, defineExpose, defineProps, computed, onBeforeUnmount, reactive } from "vue";
import { useProductStore } from "@/stores/useProductStore";
import defaultImage from "@/assets/default.jpg";
import ErrorModal from "@/components/shared/modal/ErrorModal.vue";

const productStore = useProductStore();
const selectedImage = ref(0);
const userSelectedImages = ref([]);
const selectedFiles = reactive(Array(4).fill(null));
const previewImages = reactive(Array(4).fill(null));
const existingImages = reactive(Array(4).fill(null));
const fileInputs = reactive([null, null, null, null]);

// Error handling state
const showErrorModal = ref(false);
const errorMessage = ref("");

// File size limit: 2MB
const MAX_FILE_SIZE = 2 * 1024 * 1024;

// Auto switch BASE_URL Dev/Prod
const BASE_URL = import.meta.env.DEV
  ? "http://localhost:8080/itb-mshop/sale-items-images/"
  : `${window.location.origin}${import.meta.env.BASE_URL}sale-items-images/`;

const handleImgError = (e) => {
  e.target.src = defaultImage;
};

onBeforeUnmount(() => {
  previewImages.forEach(url => {
    if (url?.startsWith('blob:')) URL.revokeObjectURL(url);
  });
  userSelectedImages.value.forEach(url => {
    if (url?.startsWith('blob:')) URL.revokeObjectURL(url);
  });
});

const emit = defineEmits(['savePic']);

const props = defineProps({
  images: Array,
  editMode: {
    type: Boolean,
    default: true
  }
});

const initializeImages = () => {
  if (props.images?.length && props.editMode) {
    for (let i = 0; i < 4; i++) {
      const item = props.images[i];
      if (!item) continue;
      const fileName = typeof item === "string" ? item : item.fileName;
      const url = fileName.startsWith("http") ? fileName : BASE_URL + fileName;
      previewImages[i] = url;
      existingImages[i] = url;
    }
  }
};

if (props.editMode) initializeImages();

const setSelectedImage = (index) => (selectedImage.value = index);

// Validate file size
const validateFile = (file) => {
  if (file.size > MAX_FILE_SIZE) {
    errorMessage.value = "The picture file size cannot be larger than 2MB.";
    showErrorModal.value = true;
    return false;
  }
  return true;
};

const handleErrorModalClose = () => {
  showErrorModal.value = false;
  errorMessage.value = "";
};

const handleFileChange = (event, i) => {
  const file = event.target.files[0];
  if (!file || !validateFile(file)) return (event.target.value = "");

  if (previewImages[i]?.startsWith("blob:")) URL.revokeObjectURL(previewImages[i]);

  existingImages[i] = null;
  selectedFiles[i] = file;
  previewImages[i] = URL.createObjectURL(file);
  emitChanges();
};

const handleMultiFileChange = (event) => {
  const files = Array.from(event.target.files);
  let emptySlots = previewImages.reduce((arr, img, i) => (!img ? [...arr, i] : arr), []);

  if (files.some(f => f.size > MAX_FILE_SIZE)) {
    errorMessage.value = "The picture file size cannot be larger than 2MB.";
    showErrorModal.value = true;
    return (event.target.value = "");
  }

  if (!emptySlots.length) {
    errorMessage.value = "Maximum 4 pictures are allowed.";
    showErrorModal.value = true;
    return (event.target.value = "");
  }

  files.slice(0, emptySlots.length).forEach((file, i) => {
    let index = emptySlots[i];
    previewImages[index] = URL.createObjectURL(file);
    selectedFiles[index] = file;
    existingImages[index] = null;
  });

  event.target.value = "";
  emitChanges();
};

const removeImage = (i) => {
  if (previewImages[i]?.startsWith("blob:")) URL.revokeObjectURL(previewImages[i]);
  previewImages[i] = null;
  selectedFiles[i] = null;
  existingImages[i] = null;
  emitChanges();
};

const swapImages = (fromIndex, toIndex) => {
  if (toIndex < 0 || toIndex > 3) return;
  [previewImages[fromIndex], previewImages[toIndex]] = [previewImages[toIndex], previewImages[fromIndex]];
  [selectedFiles[fromIndex], selectedFiles[toIndex]] = [selectedFiles[toIndex], selectedFiles[fromIndex]];
  [existingImages[fromIndex], existingImages[toIndex]] = [existingImages[toIndex], existingImages[fromIndex]];
  emitChanges();
};

const emitChanges = () => {
  const result = previewImages.map((img, i) =>
    img ? { order: i + 1, fileName: selectedFiles[i]?.name || existingImages[i], imageFile: selectedFiles[i] } : null
  ).filter(Boolean);

  emit('savePic', result);
};

const clearAllFiles = () => {
  for (let i = 0; i < 4; i++) {
    if (previewImages[i]?.startsWith("blob:")) URL.revokeObjectURL(previewImages[i]);
    previewImages[i] = null;
    selectedFiles[i] = null;
    existingImages[i] = null;
  }
  emitChanges();
};

const displayImages = computed(() =>
  previewImages.map(img => img || defaultImage)
);

const isPreviousDisabled = (i) => i === 0;
const isNextDisabled = (i) => i === 3 || !previewImages[i + 1];

defineExpose({ clearAllFiles, initializeImages });
</script>


<template>
  <ErrorModal :visible="showErrorModal" :message="errorMessage" @close="handleErrorModalClose" />

  <div class="relative">
    <div class="sticky top-20 perspective">

      <!-- ✅ Main Image -->
      <div class="bg-neutral-900 rounded-3xl flex items-center justify-center h-[450px] mb-8">
        <img
          :src="displayImages[selectedImage]"
          @error="handleImgError"
          class="object-contain max-h-full w-auto transition-all duration-300"
        />
      </div>

      <!-- ✅ Thumbnails -->
      <div class="flex flex-wrap gap-4 justify-center">
        <div
          v-for="(img, i) in displayImages"
          :key="i"
          @click="setSelectedImage(i)"
          class="cursor-pointer"
        >
          <img
            :src="img"
            @error="handleImgError"
            class="h-20 w-20 object-contain bg-neutral-800 rounded-xl"
            :class="{ 'ring-2 ring-white': selectedImage === i }"
          />
        </div>
      </div>

      <!-- ✅ Upload UI -->
      <div v-if="editMode" class="mt-8 space-y-6">
        <div class="flex justify-center gap-4">
          <label for="file-upload-multi" class="bg-blue-600 text-white px-6 py-3 rounded-lg cursor-pointer">
            เพิ่มหลายรูป
          </label>
          <input id="file-upload-multi" type="file" multiple accept="image/*" class="hidden" @change="handleMultiFileChange" />

          <button @click="clearAllFiles" class="bg-red-600 text-white px-6 py-3 rounded-lg">ล้างทั้งหมด</button>
        </div>

        <div class="flex flex-wrap justify-center gap-4">
          <div
            v-for="(img, i) in previewImages"
            :key="i"
            @click="fileInputs[i]?.click()"
            class="w-24 h-32 border flex items-center justify-center bg-gray-100 relative"
          >
            <img v-if="img" :src="img" @error="handleImgError" class="w-full h-full object-cover" />
            <span v-else class="text-gray-500 text-sm">+</span>

            <button
              v-if="img"
              @click.stop="removeImage(i)"
              class="absolute top-1 right-1 bg-red-600 text-white rounded-full w-6 h-6 text-xs flex items-center justify-center"
            >×</button>

            <input type="file" accept="image/*" class="hidden" :ref="el => fileInputs[i] = el" @change="e => handleFileChange(e, i)" />
          </div>
        </div>

      </div>
    </div>
  </div>
</template>

<style scoped>
.perspective { perspective: 1000px; }
</style>