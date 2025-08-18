<script setup>
import { ref, defineExpose, defineProps, computed, onBeforeUnmount, reactive } from "vue";
import { useProductStore } from "@/stores/useProductStore";
import defaultImage from "@/assets/default.jpg";

const productStore = useProductStore();
const selectedImage = ref(0);
const userSelectedImages = ref([]);
const selectedFiles = reactive(Array(4).fill(null));
const previewImages = reactive(Array(4).fill(null));
const existingImages = reactive(Array(4).fill(null));
const fileInputs = reactive([null, null, null, null]);

const BASE_URL = "http://localhost:8080/itb-mshop/sale-items-images/";
// const BASE_URL = "http://intproj24.sit.kmutt.ac.th/kk1/itb-mshop/sale-items-images/";

onBeforeUnmount(() => {
  previewImages.forEach(url => {
    if (url?.startsWith('blob:')) {
      URL.revokeObjectURL(url);
    }
  });
  userSelectedImages.value.forEach(url => {
    if (url?.startsWith('blob:')) {
      URL.revokeObjectURL(url);
    }
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
      if (typeof item === "string") {
        const url = item.startsWith("http") ? item : BASE_URL + item;
        previewImages[i] = url;
        existingImages[i] = url;
        selectedFiles[i] = null;
      } else if (item?.fileName) {
        const url = item.fileName.startsWith("http")
          ? item.fileName
          : BASE_URL + item.fileName;
        previewImages[i] = url;
        existingImages[i] = url;
        selectedFiles[i] = null;
      } else {
        previewImages[i] = null;
        existingImages[i] = null;
        selectedFiles[i] = null;
      }
    }
  }
};

if (props.editMode) {
  initializeImages();
}

const setSelectedImage = (index) => {
  selectedImage.value = index;
};

const handleFileChange = (event, i) => {
  const file = event.target.files[0];
  if (!file) return;

  if (previewImages[i]?.startsWith("blob:")) {
    URL.revokeObjectURL(previewImages[i]);
  }

  existingImages[i] = null;
  selectedFiles[i] = file;
  previewImages[i] = URL.createObjectURL(file);
  emitChanges();
};

const handleMultiFileChange = (event) => {
  const files = Array.from(event.target.files);
  let emptySlots = previewImages.reduce((acc, img, i) => {
    if (!img) acc.push(i);
    return acc;
  }, []);

  files.slice(0, emptySlots.length).forEach((file, idx) => {
    const slotIndex = emptySlots[idx];
    if (previewImages[slotIndex]?.startsWith("blob:")) {
      URL.revokeObjectURL(previewImages[slotIndex]);
    }
    existingImages[slotIndex] = null;
    selectedFiles[slotIndex] = file;
    previewImages[slotIndex] = URL.createObjectURL(file);
  });

  if (files.length > emptySlots.length) {
    alert(`คุณสามารถเพิ่มได้สูงสุด ${emptySlots.length} รูปภาพ`);
  }

  event.target.value = "";
  emitChanges();
};

const removeImage = (i) => {
  if (previewImages[i]?.startsWith("blob:")) {
    URL.revokeObjectURL(previewImages[i]);
  }
  previewImages[i] = null;
  selectedFiles[i] = null;
  existingImages[i] = null;
  emitChanges();
};

const swapImages = (fromIndex, toIndex) => {
  [previewImages[fromIndex], previewImages[toIndex]] = [
    previewImages[toIndex],
    previewImages[fromIndex],
  ];
  [selectedFiles[fromIndex], selectedFiles[toIndex]] = [
    selectedFiles[toIndex],
    selectedFiles[fromIndex],
  ];
  [existingImages[fromIndex], existingImages[toIndex]] = [
    existingImages[toIndex],
    existingImages[fromIndex],
  ];
  emitChanges();
};

const emitChanges = () => {
  if (props.editMode) {
    const result = [];

    // Debug state before emitting
    console.log("previewImages:", previewImages);
    console.log("existingImages:", existingImages);
    console.log("selectedFiles:", selectedFiles);
    console.log("props.images:", props.images);

    // Process images based on current previewImages state
    previewImages.forEach((preview, index) => {
      if (preview) {
        const isNewImage = !existingImages[index]; // New image if not in existingImages
        if (isNewImage) {
          // New image from selectedFiles
          if (selectedFiles[index]) {
            result.push({
              order: index + 1,
              fileName: selectedFiles[index].name,
              status: 'NEW',
              imageFile: selectedFiles[index]
            });
          }
        } else {
          // Existing image (from props.images)
          const originalImage = props.images.find(img => {
            const fileName = typeof img === "string" ? img : img?.fileName;
            const fullUrl = fileName && (fileName.startsWith("http") ? fileName : BASE_URL + fileName);
            return fullUrl === existingImages[index];
          });
          if (originalImage) {
            const originalIndex = props.images.indexOf(originalImage);
            result.push({
              order: index + 1,
              fileName: typeof originalImage === "string" ? originalImage : originalImage.fileName,
              status: originalIndex === index ? 'ONLINE' : 'MOVE',
              imageFile: null
            });
          }
        }
      }
    });

    // Process deleted images from props.images
    props.images?.forEach((img, index) => {
      const fileName = typeof img === "string" ? img : img?.fileName;
      const fullUrl = fileName && (fileName.startsWith("http") ? fileName : BASE_URL + fileName);
      if (fullUrl && !existingImages.includes(fullUrl)) {
        result.push({
          order: index + 1,
          fileName: fileName,
          status: 'DELETE',
          imageFile: null
        });
      }
    });

    // Sort result by order
    result.sort((a, b) => a.order - b.order);

    console.log("Emitting images:", result);
    emit('savePic', result);
  } else {
    // Non-edit mode: Emit an array of File objects only
    const result = selectedFiles.filter(file => file !== null);
    console.log("Emitting images (non-edit mode):", result);
    emit('savePic', result);
  }
};
const clearAllFiles = () => {
  for (let i = 0; i < 4; i++) {
    if (previewImages[i]?.startsWith("blob:")) {
      URL.revokeObjectURL(previewImages[i]);
    }
    previewImages[i] = null;
    selectedFiles[i] = null;
    existingImages[i] = null;
  }
  emitChanges();
};

const displayImages = computed(() => {
  if (props.editMode) {
    const sourceImages = previewImages.filter(img => img !== null);
    const filled = [...sourceImages];
    while (filled.length < 4) {
      filled.push(defaultImage);
    }
    return filled.slice(0, 4);
  }
  
  const sourceImages = userSelectedImages.value.length
    ? userSelectedImages.value
    : props.images?.length
    ? props.images.map(img => BASE_URL + (typeof img === "string" ? img : img.fileName))
    : productStore.productImages;

  const filled = [...sourceImages];
  while (filled.length < 4) {
    filled.push(defaultImage);
  }

  return filled.slice(0, 4);
});

const isPreviousDisabled = (index) => index === 0;
const isNextDisabled = (index) => index === 3;

defineExpose({
  clearAllFiles,
  initializeImages
});
</script>

<template>
  <div class="relative">
    <div class="sticky top-20 perspective">
      <!-- Main Image Display -->
      <div
        class="bg-gradient-to-br from-neutral-800 to-neutral-900 rounded-3xl overflow-hidden flex items-center justify-center h-[450px] mb-8 transform-style-3d hover:rotate-y-5 hover:rotate-x-3 transition-transform duration-700"
      >
        <img
          class="object-contain max-h-full w-auto transition-all duration-700 hover:scale-105 hover:translate-z-20"
          :src="displayImages[selectedImage] || defaultImage"
          alt="product"
        />
        <div
          class="absolute bottom-0 left-0 right-0 h-40 bg-gradient-to-t from-black/30 to-transparent"
        ></div>
      </div>

      <!-- Thumbnail Images -->
      <div class="flex flex-wrap gap-4 justify-center">
        <div
          v-for="(img, i) in displayImages"
          :key="i"
          @click="setSelectedImage(i)"
          class="relative cursor-pointer group perspective"
        >
          <div
            class="transform-style-3d hover:rotate-y-10 transition-transform duration-300"
          >
            <img
              :src="img"
              class="h-20 w-20 object-contain bg-neutral-800 rounded-xl shadow-sm transition-all duration-300 group-hover:shadow-lg"
              :class="[
                { 'ring-2 ring-white': selectedImage === i },
                `itbms-picture-file${i}`,
              ]"
              alt="thumbnail"
            />
          </div>
          <div
            v-if="selectedImage === i"
            class="absolute bottom-0 left-0 right-0 h-1 bg-white rounded-b-lg"
          ></div>
        </div>
      </div>

      <!-- AR View Button -->
      <div class="mt-8 flex justify-center">
        <button
          class="flex items-center space-x-2 bg-white/10 backdrop-blur-md px-4 py-2 rounded-full text-sm font-medium hover:bg-white/20 transition-colors"
        >
          <svg
            xmlns="http://www.w3.org/2000/svg"
            class="h-5 w-5"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M21 12V7a2 2 0 0 0-2-2H5a2 2 0 0 0-2 2v5"></path>
            <path d="M9 22h6"></path>
            <path d="M12 17v5"></path>
            <path d="M12 12a3 3 0 1 0 0-6 3 3 0 0 0 0 6Z"></path>
            <path d="M3 12h18"></path>
          </svg>
          <span>ดูใน AR</span>
        </button>
      </div>

      <!-- Edit Mode: Image Upload Interface -->
      <div v-if="editMode" class="mt-8 space-y-6">
        <!-- Upload Buttons -->
        <div class="flex justify-center gap-4">
          <label
            for="file-upload-multi"
            class="flex items-center space-x-2 bg-blue-500 text-white px-6 py-3 rounded-lg hover:bg-blue-700 cursor-pointer transform transition duration-200 hover:scale-105"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4"></path>
              <polyline points="7,10 12,15 17,10"></polyline>
              <line x1="12" y1="15" x2="12" y2="3"></line>
            </svg>
            <span>เลือกหลายรูปภาพ</span>
          </label>
          <input
            id="file-upload-multi"
            type="file"
            multiple
            accept="image/*"
            class="hidden"
            @change="handleMultiFileChange"
          />

          <button
            @click="clearAllFiles"
            class="flex items-center space-x-2 bg-red-500 text-white px-6 py-3 rounded-lg hover:bg-red-700 transform transition duration-200 hover:scale-105"
          >
            <svg xmlns="http://www.w3.org/2000/svg" class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M3 6h18"></path>
              <path d="M19 6v14c0 1-1 2-2 2H7c-1 0-2-1-2-2V6"></path>
              <path d="M8 6V4c0-1 1-2 2-2h4c1 0 2 1 2 2v2"></path>
            </svg>
            <span>ล้างทั้งหมด</span>
          </button>
        </div>

        <!-- Image Slots -->
        <div class="flex flex-wrap justify-center gap-4">
          <div
            v-for="(img, i) in previewImages"
            :key="i"
            class="relative w-22 h-30 rounded-lg overflow-hidden border-2 border-dashed border-gray-300 cursor-pointer flex flex-col items-center justify-center bg-gray-50 hover:bg-gray-100 transition-colors"
            :class="{ 'border-solid border-blue-400 bg-blue-50': img }"
            @click="fileInputs[i].click()"
          >
            <img
              v-if="img"
              :src="img"
              alt="preview"
              class="w-full h-32 object-cover mb-1"
            />
            <div
              v-else
              class="text-gray-400 select-none h-32 flex flex-col items-center justify-center w-full mb-1"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-8 w-8 mb-2" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2"></rect>
                <circle cx="8.5" cy="8.5" r="1.5"></circle>
                <polyline points="21,15 16,10 5,21"></polyline>
              </svg>
              <span class="text-xs">เพิ่มรูปภาพ</span>
            </div>

            <!-- Remove Button -->
            <button
              v-if="img"
              @click.stop="removeImage(i)"
              class="absolute top-2 right-2 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-sm hover:bg-red-600 transition"
              aria-label="ลบรูปภาพ"
            >
              &times;
            </button>

            <!-- Move Buttons -->
            <div v-if="img" class="absolute bottom-2 left-2 flex space-x-1">
              <button
                @click.stop="swapImages(i, i - 1)"
                :class="[
                  'bg-blue-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs transition',
                  isPreviousDisabled(i) ? 'opacity-50 cursor-not-allowed' : 'hover:bg-blue-600'
                ]"
                aria-label="ย้ายไปช่องก่อนหน้า"
                :disabled="isPreviousDisabled(i)"
              >
                ←
              </button>
              <button
                @click.stop="swapImages(i, i + 1)"
                :class="[
                  'bg-blue-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs transition',
                  isNextDisabled(i) ? 'opacity-50 cursor-not-allowed' : 'hover:bg-blue-600'
                ]"
                aria-label="ย้ายไปช่องถัดไป"
                :disabled="isNextDisabled(i)"
              >
                →
              </button>
            </div>

            <input
              type="file"
              accept="image/*"
              class="hidden"
              :ref="(el) => (fileInputs[i] = el)"
              @change="(e) => handleFileChange(e, i)"
            />
          </div>
        </div>

        <!-- File Names -->
        <div class="flex justify-center gap-4">
          <div
            v-for="(img, i) in previewImages"
            :key="'filename-' + i"
            class="w-32 text-center text-xs text-gray-600 truncate"
          >
            {{
              selectedFiles[i]?.name ||
              (existingImages[i] ? existingImages[i].split("/").pop() : "")
            }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.perspective {
  perspective: 1000px;
}

.transform-style-3d {
  transform-style: preserve-3d;
}

.hover\:rotate-y-5:hover {
  transform: rotateY(5deg);
}

.hover\:rotate-x-3:hover {
  transform: rotateX(3deg);
}

.hover\:rotate-y-10:hover {
  transform: rotateY(10deg);
}

.hover\:translate-z-20:hover {
  transform: translateZ(20px);
}

.itbms-picture-file0,
.itbms-picture-file1,
.itbms-picture-file2,
.itbms-picture-file3 {
  /* Styling for picture files if needed */
}
</style>