<script setup>
import { ref, defineExpose, defineProps, reactive } from "vue";

const BASE_URL = "http://localhost:8080/itb-mshop/";

const props = defineProps({
  images: {
    type: Array,
    default: () => [],
  },
});

const showModal = ref(false);

// previewImages เก็บ URL ของรูปแสดง (string หรือ null) 4 ช่อง
const previewImages = reactive(Array(4).fill(null));
// selectedFiles เก็บไฟล์จริงที่ผู้ใช้เลือกใหม่ (File หรือ null)
const selectedFiles = reactive(Array(4).fill(null));
// existingImages เก็บ URL รูปเดิมจาก props ที่ยังไม่ถูกลบ (string หรือ null)
const existingImages = reactive(Array(4).fill(null));

const selectedImage = ref(null);

// โหลดรูปเดิมจาก props ลง previewImages และ existingImages ตอนเปิด modal
const openModal = () => {
  if (props.images?.length) {
    for (let i = 0; i < 4; i++) {
      if (props.images[i]) {
        const url = props.images[i].startsWith("http")
          ? props.images[i]
          : BASE_URL + props.images[i];
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

// ใช้ ref เก็บ input file แต่ละช่อง
const fileInputs = reactive([null, null, null, null]);

const closeModal = () => {
  showModal.value = false;
  clearFiles();
};

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

// เมื่อเลือกไฟล์ในช่องที่ i
const handleFileChange = (event, i) => {
  const file = event.target.files[0];
  if (!file) return;

  // ถ้ารูปเดิมในช่องนี้เป็น blob URL ให้ล้างก่อน
  if (previewImages[i]?.startsWith("blob:")) {
    URL.revokeObjectURL(previewImages[i]);
  }

  // ลบรูปเดิมที่ช่องนี้ (เพราะเลือกไฟล์ใหม่แทนที่แล้ว)
  existingImages[i] = null;

  selectedFiles[i] = file;
  previewImages[i] = URL.createObjectURL(file);
};

// ลบรูปในช่องที่ i
const removeImage = (i) => {
  if (previewImages[i]?.startsWith("blob:")) {
    URL.revokeObjectURL(previewImages[i]);
  }
  previewImages[i] = null;
  selectedFiles[i] = null;
  existingImages[i] = null;
};

const selectImage = (index) => {
  selectedImage.value = index;
};

// เพิ่มรูปหลายไฟล์จาก input หลัก ลงช่องว่างตามลำดับ
const handleMultiFileChange = (event) => {
  const files = Array.from(event.target.files);
  let emptySlots = previewImages.reduce((acc, img, i) => {
    if (!img) acc.push(i);
    return acc;
  }, []);

  if (files.length > emptySlots.length) {
    alert(`You can only add up to ${emptySlots.length} images.`);
  }

  files.slice(0, emptySlots.length).forEach((file, idx) => {
    const slotIndex = emptySlots[idx];

    if (previewImages[slotIndex]?.startsWith("blob:")) {
      URL.revokeObjectURL(previewImages[slotIndex]);
    }

    existingImages[slotIndex] = null;
    selectedFiles[slotIndex] = file;
    previewImages[slotIndex] = URL.createObjectURL(file);
  });

  event.target.value = "";
};

const uploadFiles = async () => {
  const formData = new FormData();

  // append ไฟล์ใหม่
  selectedFiles.forEach((file) => {
    if (file) formData.append("files", file);
  });

  // เตรียมส่งชื่อไฟล์รูปเดิมที่ยังไม่ถูกลบ
  const existingFileNames = existingImages
    .filter((img) => img !== null)
    .map((img) => {
      return img.split("/").pop();
    });

  formData.append("existingImages", JSON.stringify(existingFileNames));

  if (formData.has("files") === false && existingFileNames.length === 0) {
    alert("Please select or keep at least one image before uploading.");
    return;
  }

  // จำลองข้อมูลที่ส่งไป backend
  console.log("=== Data to send to backend ===");
  console.log("Existing images filenames:", existingFileNames);

  for (let pair of formData.entries()) {
    if (pair[0] === "files") {
      console.log(pair[0] + ":", pair[1].name); // แสดงชื่อไฟล์แต่ละไฟล์
    } else {
      console.log(pair[0] + ":", pair[1]);
    }
  }

  // ตัวอย่างการส่งจริง (comment ไว้สำหรับ debug)
  /*
  try {
    const res = await fetch("/api/upload", {
      method: "POST",
      body: formData,
    });

    if (!res.ok) throw new Error("Upload failed.");

    alert("Upload successful.");
    closeModal();
  } catch (error) {
    alert(error.message);
  }
  */
};


const getPictureClass = (index, isCleared = false) => {
  const baseClass = `itbms-picture-file${index + 1}`;
  return isCleared ? `${baseClass}-clear` : baseClass;
};

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
              class="relative w-24 h-24 rounded-lg overflow-hidden border border-gray-300 cursor-pointer flex items-center justify-center bg-gray-200"
              @click="fileInputs[i].click()"
            >
              <img
                v-if="img"
                :src="img"
                alt="preview"
                class="w-full h-full object-cover"
              />
              <div v-else class="text-gray-500 select-none">+ Add</div>

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

              <!-- input file ซ่อน สำหรับแต่ละช่อง -->
              <input
                type="file"
                accept="image/*"
                class="hidden"
                :ref="(el) => (fileInputs[i] = el)"
                @change="(e) => handleFileChange(e, i)"
              />
            </div>
          </div>

          <div class="mb-4 p-3 bg-gray-100 rounded text-sm">
            <p class="font-semibold mb-2">Class names ที่จะใช้:</p>
            <div class="grid grid-cols-2 gap-2">
              <div v-for="i in 4" :key="i" class="text-gray-700">
                <span class="font-mono">{{
                  getPictureClass(i - 1, false)
                }}</span>
                →
                <span class="font-mono text-red-600">{{
                  getPictureClass(i - 1, true)
                }}</span>
              </div>
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
              @click="uploadFiles"
              class="px-5 py-2 rounded bg-orange-500 text-white hover:bg-orange-700 transition"
            >
              Save
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
