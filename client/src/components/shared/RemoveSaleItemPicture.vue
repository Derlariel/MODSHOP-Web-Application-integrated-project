<script setup>
import { ref, computed } from 'vue'

const isEditMode = ref(false)
const pictures = ref([
  { id: 1, filename: 'item-photo-1.jpg', url: 'https://via.placeholder.com/200x200/4F46E5/white?text=Photo+1' },
  { id: 2, filename: 'item-photo-2.jpg', url: 'https://via.placeholder.com/200x200/7C3AED/white?text=Photo+2' },
  { id: 3, filename: 'item-photo-3.jpg', url: 'https://via.placeholder.com/200x200/059669/white?text=Photo+3' },
  { id: 4, filename: 'item-photo-4.jpg', url: 'https://via.placeholder.com/200x200/DC2626/white?text=Photo+4' }
])
const originalPictures = ref([])
const newFiles = ref([])
const saleItem = ref({
  id: 1,
  title: 'Vintage Leather Jacket',
  price: '$150',
  description: 'High-quality vintage leather jacket in excellent condition.',
  seller: 'John Doe'
})

const isModified = computed(() => {
  if (!isEditMode.value) return false
  return JSON.stringify(pictures.value) !== JSON.stringify(originalPictures.value) || newFiles.value.length > 0
})

function handleEditClick() {
  isEditMode.value = true
  originalPictures.value = [...pictures.value]
}

function handleCancelEdit() {
  isEditMode.value = false
  pictures.value = [...originalPictures.value]
  originalPictures.value = []
  newFiles.value = []
}

function handleDeletePicture(pictureId) {
  pictures.value = pictures.value.filter(pic => pic.id !== pictureId).map((pic, index) => ({
    ...pic,
    id: index + 1
  }))
}

function handleMoveUp(pictureId) {
  const idx = pictures.value.findIndex(pic => pic.id === pictureId)
  if (idx > 0) {
    const temp = [...pictures.value]
    ;[temp[idx], temp[idx - 1]] = [temp[idx - 1], temp[idx]]
    pictures.value = temp
  }
}

function handleMoveDown(pictureId) {
  const idx = pictures.value.findIndex(pic => pic.id === pictureId)
  if (idx < pictures.value.length - 1) {
    const temp = [...pictures.value]
    ;[temp[idx], temp[idx + 1]] = [temp[idx + 1], temp[idx]]
    pictures.value = temp
  }
}

function handleFileUpload(e) {
  const files = Array.from(e.target.files)
  const newPicFiles = files.map((file, index) => ({
    file,
    filename: file.name,
    url: URL.createObjectURL(file),
    id: pictures.value.length + newFiles.value.length + index + 1,
    isNew: true
  }))
  newFiles.value = [...newFiles.value, ...newPicFiles]
}

function removeNewFile(index) {
  newFiles.value = newFiles.value.filter((_, i) => i !== index)
}

async function handleSave() {
  try {
    const updateData = {
      saleItemId: saleItem.value.id,
      pictures: pictures.value.map((pic, index) => ({
        ...pic,
        order: index + 1
      })),
      newFiles: newFiles.value,
      deletedFiles: originalPictures.value.filter(
        orig => !pictures.value.find(pic => pic.id === orig.id)
      )
    }
    console.log('Sending update request to backend:', updateData)

    await new Promise(res => setTimeout(res, 1000))

    pictures.value = [...pictures.value, ...newFiles.value.map(f => ({ ...f, isNew: false }))]
    newFiles.value = []
    originalPictures.value = []
    isEditMode.value = false
    alert('Pictures updated successfully!')
  } catch (err) {
    console.error('Error updating pictures:', err)
    alert('Error updating pictures. Please try again.')
  }
}
</script>
<template>
  <div class="max-w-4xl mx-auto p-6 bg-white">
    <!-- Sale Item Details Header -->
    <div class="mb-8 p-6 bg-gray-50 rounded-lg">
      <h1 class="text-2xl font-bold text-gray-800 mb-2">{{ saleItem.title }}</h1>
      <p class="text-xl font-semibold text-green-600 mb-2">{{ saleItem.price }}</p>
      <p class="text-gray-600 mb-2">{{ saleItem.description }}</p>
      <p class="text-sm text-gray-500">Seller: {{ saleItem.seller }}</p>
    </div>

    <!-- Pictures Section -->
    <div class="mb-6">
      <div class="flex justify-between items-center mb-4">
        <h2 class="text-xl font-semibold text-gray-800">Pictures</h2>

        <!-- Edit/Save/Cancel Buttons -->
        <div v-if="!isEditMode">
          <button
            @click="handleEditClick"
            class="flex items-center px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z">
              </path>
            </svg>
            Edit
          </button>
        </div>
        <div v-else class="flex gap-2">
          <button
            @click="handleCancelEdit"
            class="px-4 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors"
          >
            Cancel
          </button>
          <button
            @click="handleSave"
            :disabled="!isModified"
            :class="[
              'flex items-center px-4 py-2 rounded-lg transition-colors',
              isModified 
                ? 'bg-green-600 text-white hover:bg-green-700' 
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
            ]"
          >
            <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M8 7H5a2 2 0 00-2 2v9a2 2 0 002 2h14a2 2 0 002-2V9a2 2 0 00-2-2h-3m-1 4l-3-3m0 0l-3 3m3-3v12">
              </path>
            </svg>
            Save
          </button>
        </div>
      </div>

      <!-- File Upload (only in edit mode) -->
      <div v-if="isEditMode" class="mb-4 p-4 border-2 border-dashed border-gray-300 rounded-lg">
        <label class="flex flex-col items-center cursor-pointer">
          <svg class="w-8 h-8 text-gray-400 mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12">
            </path>
          </svg>
          <span class="text-sm text-gray-600">Click to upload new pictures</span>
          <input
            ref="fileInput"
            type="file"
            multiple
            accept="image/*"
            @change="handleFileUpload"
            class="hidden"
          />
        </label>
      </div>

      <!-- Pictures Grid -->
      <div class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
        <!-- Existing Pictures -->
        <div
          v-for="(picture, index) in pictures"
          :key="picture.id"
          class="relative group bg-white border rounded-lg overflow-hidden shadow-sm"
        >
          <img :src="picture.url" :alt="`Item picture ${index + 1}`" class="w-full h-48 object-cover" />

          <div
            v-if="isEditMode"
            class="absolute inset-0 bg-black bg-opacity-50 opacity-0 group-hover:opacity-100 transition-opacity"
          >
            <button
              @click="handleDeletePicture(picture.id)"
              class="absolute top-2 right-2 p-1 bg-red-600 text-white rounded-full hover:bg-red-700 transition-colors"
              title="Delete picture"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12">
                </path>
              </svg>
            </button>

            <div class="absolute top-2 left-2 flex flex-col gap-1">
              <button
                @click="handleMoveUp(picture.id)"
                :disabled="index === 0"
                :class="[
                  'p-1 rounded-full transition-colors text-white',
                  index === 0 
                    ? 'bg-gray-400 cursor-not-allowed' 
                    : 'bg-blue-600 hover:bg-blue-700'
                ]"
                title="Move up"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 15l7-7 7 7">
                  </path>
                </svg>
              </button>
              <button
                @click="handleMoveDown(picture.id)"
                :disabled="index === pictures.length - 1"
                :class="[
                  'p-1 rounded-full transition-colors text-white',
                  index === pictures.length - 1 
                    ? 'bg-gray-400 cursor-not-allowed' 
                    : 'bg-blue-600 hover:bg-blue-700'
                ]"
                title="Move down"
              >
                <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 9l-7 7-7-7">
                  </path>
                </svg>
              </button>
            </div>
          </div>

          <div class="p-2 bg-gray-50">
            <p class="text-xs text-gray-600 truncate">{{ picture.filename }}</p>
          </div>
        </div>

        <!-- New Files -->
        <div
          v-for="(file, index) in newFiles"
          :key="`new-${index}`"
          class="relative group bg-white border rounded-lg overflow-hidden shadow-sm border-green-300"
        >
          <img :src="file.url" :alt="`New picture ${index + 1}`" class="w-full h-48 object-cover" />

          <div class="absolute inset-0 bg-black bg-opacity-50 opacity-0 group-hover:opacity-100 transition-opacity">
            <button
              @click="removeNewFile(index)"
              class="absolute top-2 right-2 p-1 bg-red-600 text-white rounded-full hover:bg-red-700 transition-colors"
              title="Remove new picture"
            >
              <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                  d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16">
                </path>
              </svg>
            </button>
          </div>

          <div class="p-2 bg-green-50 border-t border-green-300">
            <p class="text-xs text-green-700 truncate font-medium">{{ file.filename }} (New)</p>
          </div>
        </div>
      </div>

      <div v-if="pictures.length === 0 && newFiles.length === 0" class="text-center py-12 text-gray-500">
        <p>No pictures uploaded yet.</p>
        <p v-if="isEditMode" class="text-sm mt-2">Upload some pictures using the upload area above.</p>
      </div>
    </div>

    <!-- Status Messages -->
    <div v-if="isEditMode" class="mt-4 p-3 bg-blue-50 border border-blue-200 rounded-lg">
      <p class="text-sm text-blue-700">
        <strong>Edit Mode:</strong>
        You can delete pictures, reorder them, or upload new pictures.
        {{ isModified ? ' Changes detected - click "Save" to update.' : ' No changes made yet.' }}
      </p>
    </div>
  </div>
</template>


