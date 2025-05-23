<script setup>
import ListModel from './ListModel.vue';
import { ref, defineProps, defineEmits, computed, watch } from 'vue';


const props = defineProps({
  totalPages: {
    type: Number,
    required: true
  }
})


import { useProductStore } from "@/stores/useProductStore";
const productStore = useProductStore();
const storePages = computed(() => productStore.allPages)

const emit = defineEmits(["sendPages"])

const visiblePages = computed(() => {
  const total = productStore.allPages;
  const current = activePage.value;

  const maxVisible = 10;

  let start = Math.max(1, current - Math.floor(maxVisible / 2));
  let end = start + maxVisible - 1;

  if (end > total) {
    end = total;
    start = Math.max(1, end - maxVisible + 1);
  }

  return Array.from({ length: end - start + 1 }, (_, i) => start + i);
});

const activePage = ref(1);
const paging = ref("paging")

const setActivePage = (page) => {
  const maxPage = storePages.value;

  if (page > maxPage) {
    activePage.value = 1;
  } else {
    activePage.value = page;
  }

  console.log("max-emit", maxPage);
  console.log("before-emit", activePage.value - 1);
  
  emit("sendPages", activePage.value - 1);
};

watch(() => productStore.allPages, (newVal) => {
  console.log("max-emit", newVal);
  console.log("before-emit", activePage.value - 1);
  if (activePage.value > newVal) {
    setActivePage(1)
  }
});

const first =  () => {
  setActivePage(visiblePages.value.length+3)
}

const last = () => {
  setActivePage(visiblePages.value.length+2)
}

const next = () => {
  console.log(visiblePages.value.length, "length");
  
  if (activePage.value < visiblePages.value.length+2) {
    activePage.value++
    setActivePage(activePage.value)
  }
}

const prev = () => {
  if (activePage.value > visiblePages.value[0]) {
    activePage.value--
    setActivePage(activePage.value)
  }
}
</script>

<template>
  <div class="flex justify-center items-center w-[full] py-1 px-1 bg-black">
    <div class="pagination-container">
      <div>
        <button class="bg-white px-2 py-.5 rounded-md text-black font-bold" @click="first">First</button>
    </div>
    <div>
        <button class="bg-white px-2 py-.5 rounded-md text-black font-bold" @click="prev">Previous</button>
    </div>

      <ListModel :saleItems="visiblePages" :paging="paging">
        <template #listItems="{ Item: page}">
          <button
            @click="setActivePage(page)"
            :class="[
              `itbms-page-${page-1}`,'md:mx-3 h-2 w-2 md:h-8 md:w-8 rounded-xs flex items-center justify-center text-sm md:text-base font-medium transition-all duration-300 ease-out',
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
        <button class="bg-white px-2 py-.5 rounded-md text-black font-bold" @click="next">Next</button>
    </div>
    <div>
        <button class="bg-white px-2 py-.5 rounded-md text-black font-bold" @click="last">Last</button>
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