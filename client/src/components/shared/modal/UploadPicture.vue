<script setup>
import { ref, defineExpose, defineProps, reactive, defineEmits } from "vue";

const emit = defineEmits(["sendSelectPictures"]);

const BASE_URL = "http://localhost:8080/itb-mshop/sale-items-images/";

const props = defineProps({
  images: {
    type: Array,
    default: () => [],
  },
});

const showModal = ref(false);

const previewImages = reactive(Array(4).fill(null));
const selectedFiles = reactive(Array(4).fill(null));
const existingImages = reactive(Array(4).fill(null));

const selectedImage = ref(null);

const openModal = () => {
  if (props.images?.length) {
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
  } else {
    for (let i = 0; i < 4; i++) {
      previewImages[i] = null;
      existingImages[i] = null;
      selectedFiles[i] = null;
    }
  }
  showModal.value = true;
};

const fileInputs = reactive([null, null, null, null]);

const clearFiles = () => {
  for (let i = 0; i < 4; i++) {
    if (previewImages[i]?.startsWith("blob:")) {
      URL.revokeObjectURL(previewImages[i]);
    }
    previewImages[i] = null;
    selectedFiles[i] = null;
    existingImages[i] = null;
  }
  selectedImage.value = null;
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
};

const removeImage = (i) => {
  if (previewImages[i]?.startsWith("blob:")) {
    URL.revokeObjectURL(previewImages[i]);
  }
  previewImages[i] = null;
  selectedFiles[i] = null;
  existingImages[i] = null;
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
};


const sendPic = () => {
  console.log("🟠 sendPic clicked");  // ให้แน่ใจว่าเรียกเข้ามาที่ฟังก์ชัน

  const filledSlots = previewImages.filter((img) => img !== null);
  console.log("Selected images to send:", filledSlots);  // ตรวจสอบข้อมูลที่ถูกเลือก

    closeModal();  // ปิด modal

  emit("sendSelectPictures", filledSlots); // ส่งข้อมูลไปยัง parent

  console.log("Modal closed and data sent.");
};

const findEmptySlot = () => {
  return previewImages.findIndex((img) => img === null);
};
const closeModal = () => {
  showModal.value = false;
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
    alert(`You can only add up to ${emptySlots.length} images.`);
  }


  event.target.value = "";
};

const uploadFiles = async () => {
  const formData = new FormData();
  selectedFiles.forEach((file) => {
    if (file) formData.append("images", file);
  });

  const existingFileNames = existingImages
    .filter((img) => img !== null)
    .map((img) => img.split("/").pop());

  formData.append("existingImages", JSON.stringify(existingFileNames));

  if (formData.has("images") === false && existingFileNames.length === 0) {
    alert("Please select or keep at least one image before uploading.");
    return;
  }

  console.log("=== Data to send to backend ===");
  console.log("Existing images filenames:", existingFileNames);

  for (let pair of formData.entries()) {
    console.log(pair[0] + ":", pair[1]);
  }
};

const getPictureClass = (index, isCleared = false) => {
  const baseClass = `itbms-picture-file${index + 1}`;
  return isCleared ? `${baseClass}-clear` : baseClass;
};

const isPreviousDisabled = (index) => index === 0;
const isNextDisabled = (index) => index === 3;

defineExpose({
  openModal,
  closeModal,
});
</script>

<template>
  <div>
    <transition name="fade">
      <div
        v-if="showModal"
        class="fixed inset-0 flex items-center justify-center z-50 bg-opacity-20 backdrop-blur-md"
      >
        <div
          class="bg-white rounded-xl w-full max-w-3xl p-6 relative shadow-lg"
          @click.stop
        >
          <h3 class="text-2xl font-semibold mb-6">
            Upload Pictures (Up to 4 images)
          </h3>

          <div class="flex flex-wrap justify-center gap-4 mb-6">
            <div
              v-for="(img, i) in previewImages"
              :key="i"
              class="relative w-24 h-32 rounded-lg overflow-hidden border border-gray-300 cursor-pointer flex flex-col items-center justify-center bg-gray-200"
              @click="fileInputs[i].click()"
            >
              <img
                v-if="img"
                :src="img"
                alt="preview"
                class="w-full h-24 object-cover mb-1"
              />
              <div
                v-else
                class="text-gray-500 select-none h-24 flex items-center justify-center w-full mb-1"
              >
                + Add
              </div>

              <button
                v-if="img"
                @click.stop="removeImage(i)"
                :class="[
                  'absolute top-1 right-1 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-sm hover:bg-red-600 transition',
                  getPictureClass(i, false),
                ]"
                aria-label="Remove image"
              >
                &times;
              </button>

              <div v-if="img" class="absolute bottom-1 left-1 flex space-x-1">
                <button
                  @click.stop="swapImages(i, i - 1)"
                  :class="[
                    'bg-blue-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-xs transition',
                    isPreviousDisabled(i) ? 'opacity-50 cursor-not-allowed' : 'hover:bg-blue-600'
                  ]"
                  aria-label="Move to previous slot"
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
                  aria-label="Move to next slot"
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

          <div class="flex justify-center gap-4 mb-6">
            <div
              v-for="(img, i) in previewImages"
              :key="'filename-' + i"
              class="w-24 text-center text-xs text-gray-700 truncate"
            >
              {{
                selectedFiles[i]?.name ||
                existingImages[i]?.split("/").pop() ||
                ""
              }}
            </div>
          </div>

          <div class="flex justify-end space-x-4">
            <button
              @click="closeModal"
              class="px-5 py-2 rounded bg-gray-300 hover:bg-gray-400 transition"
            >
              Cancel
            </button>

            <label
              for="file-upload-multi"
              class="block mb-6 cursor-pointer px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-700 w-max"
            >
              Select Multiple Images
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
              @click="sendPic"
              class="px-5 py-2 rounded bg-orange-500 text-white hover:bg-orange-700 transition"
            >
              xxx
            </button>
          </div>

          <button
            @click="closeModal"
            class="absolute top-4 right-4 text-gray-600 hover:text-gray-900 text-3xl font-bold leading-none"
            aria-label="Close modal"
          >
            &times;
          </button>
        </div>
      </div>
    </transition>
  </div>
</template>

<style scoped>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.itbms-picture-file1,
.itbms-picture-file2,
.itbms-picture-file3,
.itbms-picture-file4 {
}

.itbms-picture-file1-clear,
.itbms-picture-file2-clear,
.itbms-picture-file3-clear,
.itbms-picture-file4-clear {
  opacity: 0.5;
}
</style>
