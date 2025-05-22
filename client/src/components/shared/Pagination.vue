<script setup>
import ListModel from './ListModel.vue';
import { ref } from 'vue';

const pages = [1, 2, 3, 4, 5, 6];
const activePage = ref(1);
const paging = ref("paging")

const setActivePage = (page) => {
  activePage.value = page;
};
</script>

<template>
  <div class="flex justify-center items-center w-[full] py-1 px-1 bg-black">
    <div class="pagination-container">
    <div>
        <button class="bg-white px-2 py-.5 rounded-md text-black font-bold" @click="activePage > 1 && activePage--"><</button>
    </div>
      <ListModel :saleItems="pages" :paging="paging">
        <template #listItems="{ Item: page}">
          <button
            @click="setActivePage(page)"
            :class="[
              'pagination-item md:mx-3 h-2 w-2 md:h-8 md:w-8 rounded-xs flex items-center justify-center text-sm md:text-base font-medium transition-all duration-300 ease-out',
              activePage === page 
                ? 'bg-white text-gray-900' 
                : 'bg-gray-800 text-white hover:bg-gray-200'
            ]"
            :aria-label="`Page ${page}`"
            :aria-current="activePage === page ? 'page' : undefined"
          >
            {{ page }}
          </button>
        </template>
      </ListModel>
      <div>
        <button class="bg-white px-2 py-.5 rounded-md text-black font-bold"   @click="activePage < pages.length && activePage++"><i class="fi fi-sr-play"></i>></button>
    </div>
    </div>
  </div>
</template>

<style scoped>
.pagination-container {
  display: flex;
  align-items: center;
  justify-content: center;
}

@media (min-width: 768px) {
  .pagination-item {
    transform-origin: center;
  }
}
</style>