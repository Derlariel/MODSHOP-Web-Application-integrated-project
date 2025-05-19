<script setup>
import { List, GalleryThumbnails } from 'lucide-vue-next';
import { ref, watch } from 'vue';
import { useAppStore } from '@/stores/useAppStore';

const props = defineProps({
  saleItems: Array,
  viewType: {
    type: String,
    default: 'gallery',
    validator: value => ['gallery', 'list'].includes(value),
  }
});

const emit = defineEmits(['update:viewType']);
const currentView = ref(props.viewType);

watch(() => props.viewType, (val) => {
  currentView.value = val;
});

const toggleView = (type) => {
  currentView.value = type;
  emit('update:viewType', type);
};

const appStore = useAppStore();
</script>

<template>
  <div class="container mx-auto px-2 sm:px-4">
    <!-- Toggle View Buttons -->
    <div v-if="appStore.adminMode" class="flex justify-end space-x-2 mb-4">
      <button @click="toggleView('gallery')"
        :class="['p-2 rounded-md', currentView === 'gallery' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-black']">
        <GalleryThumbnails class="w-5 h-5" />
      </button>
      <button @click="toggleView('list')"
        :class="['p-2 rounded-md', currentView === 'list' ? 'bg-blue-500 text-white' : 'bg-gray-200 text-black']">
        <List class="w-5 h-5" />
      </button>
    </div>

    <!-- Views -->
    <div v-if="currentView === 'gallery'" class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-y-8 gap-x-10">
      <div v-for="(item, index) in saleItems" :key="index">
        <slot name="listItems" :Item="item" :viewType="currentView" />
      </div>
    </div>

    <div v-else class="responsive-table-wrapper">
      <!-- Table Header -->
      <div class="overflow-x-auto shadow-md rounded-md">
        <table class="min-w-full bg-black border border-neutral-800">
          <thead>
            <tr class="bg-neutral-900 text-white text-left">
              <slot name="tableHeader" />
            </tr>
          </thead>
          <tbody>
            <template v-if="saleItems && saleItems.length > 0">
              <tr v-for="(item, index) in saleItems" :key="index" class="border-t border-neutral-800 hover:bg-neutral-800 transition-colors itbms-row">
                <slot name="listItems" :Item="item" :viewType="currentView" />
              </tr>
            </template>
            <template v-else>
              <tr>
                <td colspan="9" class="py-4 text-center text-gray-400">no sale item</td>
              </tr>
            </template>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style scoped>
.responsive-table-wrapper {
  width: 100%;
}

:deep(td), :deep(th) {
  white-space: normal;
  word-wrap: break-word;
  max-width: 200px;
  padding: 0.5rem;
}

@media (max-width: 640px) {
  :deep(td), :deep(th) {
    padding: 0.25rem 0.5rem;
    font-size: 0.875rem;
    max-width: 150px;
  }
}

:deep(table) {
  border-collapse: collapse;
  width: 100%;
}

:deep(th) {
  font-weight: 600;
  text-transform: capitalize;
}

/* Better hover effect */
:deep(.itbms-row:hover) {
  background-color: rgba(255, 255, 255, 0.1);
}
</style>