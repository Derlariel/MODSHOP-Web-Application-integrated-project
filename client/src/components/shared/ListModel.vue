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
  <div class="container mx-auto px-4">
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

    <div v-else>
      <!-- Table Header -->
      <div class="overflow-x-auto">
        <table class="min-w-full bg-black border border-neutral-800 rounded-md">
          <thead>
            <tr class="bg-neutral-900 text-white text-left">
              <slot name="tableHeader" />
            </tr>
          </thead>
          <tbody>
            <template v-if="saleItems && saleItems.length > 0">
              <tr v-for="(item, index) in saleItems" :key="index" class="border-t border-neutral-800 hover:bg-neutral-800 transition-colors">
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