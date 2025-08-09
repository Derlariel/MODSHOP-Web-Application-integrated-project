<script setup>
import { ref, defineExpose } from "vue";

const showModal = ref(false);
const selectedFiles = ref([]);
const previewImages = ref([]);
const selectedImage = ref(null); 

const openModal = () => {
  showModal.value = true;
};

const closeModal = () => {
  showModal.value = false;
  clearFiles();
};

const clearFiles = () => {
  selectedFiles.value = [];
  previewImages.value.forEach((url) => URL.revokeObjectURL(url));
  previewImages.value = [];
  selectedImage.value = null; 
};

const handleFileChange = (event) => {
  const files = Array.from(event.target.files);
  const totalFiles = selectedFiles.value.length + files.length;

  if (totalFiles > 4) {
    alert("You can upload up to 4 images only.");
    files.splice(4 - selectedFiles.value.length);
  }

  selectedFiles.value.push(...files);

  previewImages.value.forEach((url) => URL.revokeObjectURL(url));
  previewImages.value = selectedFiles.value.map((file) =>
    URL.createObjectURL(file)
  );
};

const removeImage = (index) => {
  URL.revokeObjectURL(previewImages.value[index]);
  selectedFiles.value.splice(index, 1);
  previewImages.value.splice(index, 1);
  
 
  if (selectedImage.value === index) {
    selectedImage.value = null;
  } else if (selectedImage.value > index) {
    selectedImage.value--; 
  }
};

const selectImage = (index) => {
  selectedImage.value = index;
};

const uploadFiles = async () => {
  if (selectedFiles.value.length === 0) {
    alert("Please select files before uploading.");
    return;
  }

  const formData = new FormData();
  selectedFiles.value.forEach((file) => {
    formData.append("files", file);
  });

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

          <label
            for="file-upload"
            class="block w-full mb-6 cursor-pointer border-2 border-dashed border-blue-500 rounded-lg py-10 text-center text-blue-500 font-medium hover:bg-blue-50 transition"
          >
            Click to upload
          </label>
          <input
            id="file-upload"
            type="file"
            multiple
            accept="image/*"
            @change="handleFileChange"
            class="hidden"
          />

          <div class="flex flex-wrap justify-center gap-4 mb-6">
            <div
              v-for="(img, i) in previewImages"
              :key="i"
              class="relative w-24 h-24 rounded-lg overflow-hidden border border-gray-300"
              @click="selectImage(i)"
            >
              <img
                :src="img"
                alt="preview"
                class="w-full h-full object-cover cursor-pointer"
              />
              <button
                @click.stop="removeImage(i)"
                :class="[
                  'absolute top-1 right-1 bg-red-500 text-white rounded-full w-6 h-6 flex items-center justify-center text-sm hover:bg-red-600 transition',
                  { 'ring-2 ring-white': selectedImage === i },
                  getPictureClass(i, false)
                ]"
                aria-label="Remove image"
              >
                &times;
              </button>
            </div>
          </div>

          <div class="mb-4 p-3 bg-gray-100 rounded text-sm">
            <p class="font-semibold mb-2">Class names ที่จะใช้:</p>
            <div class="grid grid-cols-2 gap-2">
              <div v-for="i in 4" :key="i" class="text-gray-700">
                <span class="font-mono">{{ getPictureClass(i - 1, false) }}</span> 
                → 
                <span class="font-mono text-red-600">{{ getPictureClass(i - 1, true) }}</span>
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
}</style>