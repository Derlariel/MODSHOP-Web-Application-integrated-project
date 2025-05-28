<script setup>
import { ref, watch } from 'vue';

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


</script>

<template>
  <div class="container mx-auto px-2 sm:px-4">


    <!-- Gallery View -->
    <div
      v-if="currentView === 'gallery'"
      class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-y-6 sm:gap-y-8 gap-x-4 sm:gap-x-6"
    >
      <div v-for="(item, index) in saleItems" :key="index">
        <slot name="listItems" :Item="item" :viewType="currentView" />
      </div>
    </div>

    <!-- Table View -->
    <div v-else class="responsive-table-wrapper">
      <div class="overflow-x-auto w-full">
        <div class="min-w-[600px] shadow-md rounded-md">
          <table class="w-full bg-black border border-neutral-800 text-sm sm:text-base">
            <thead>
              <tr class="bg-neutral-900 text-white text-left">
                <slot name="tableHeader" />
              </tr>
            </thead>
            <tbody>
              <template v-if="saleItems && saleItems.length > 0">
                <tr
                  v-for="(item, index) in saleItems"
                  :key="index"
                  class="border-t border-neutral-800 hover:bg-neutral-800 transition-colors itbms-row"
                >
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
  </div>
</template>

<style scoped>
.responsive-table-wrapper {
  width: 100%;
  overflow-x: auto;
}

:deep(table) {
  min-width: 600px;
  width: 100%;
  border-collapse: collapse;
}

:deep(td),
:deep(th) {
  white-space: normal;
  word-wrap: break-word;
  max-width: 200px;
  padding: 0.5rem;
}

@media (max-width: 640px) {
  :deep(td),
  :deep(th) {
    padding: 0.25rem 0.5rem;
    font-size: 0.875rem;
    max-width: 150px;
  }
}

:deep(th) {
  font-weight: 600;
  text-transform: capitalize;
}

:deep(.itbms-row:hover) {
  background-color: rgba(255, 255, 255, 0.1);
}
</style>
