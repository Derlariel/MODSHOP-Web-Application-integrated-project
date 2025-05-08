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
      <div class="grid grid-cols-7 gap-4 px-2 py-3 bg-gray-100 font-semibold text-gray-600 text-sm border rounded-t-md">
        <slot name="listHeader" />
      </div>
      <div v-for="(item, index) in saleItems" :key="index" class="px-2 py-3 ">
        <slot name="listItems" :Item="item" :viewType="currentView" />
      </div>
    </div>
  </div>
</template>
