<script setup>
import { ref, computed, watch, onMounted } from 'vue';
import FilterSort from '@/components/shared/FilterSort.vue';
import ProductGallery from '@/components/user/ProductGallery.vue';
import { useProductStore } from '@/stores/useProductStore';

const BASE_URL = import.meta.env.VITE_BASE_URL;
const allItems = ref([]);
const filters = ref({
  filterBrands: [],
  size: 10,
  sortField: 'createdOn',
  sortDirection: 'asc',
  activePage: 1
});

const productStore = useProductStore();

const fetchSaleItems = async () => {
  try {
    const res = await fetch(`${BASE_URL}/v1/sale-items`);
    if (!res.ok) throw new Error('Failed to fetch sale items');
    const data = await res.json();
    allItems.value = data;
  } catch (err) {
    console.error('Error fetching sale items:', err);
  }
};

onMounted(() => {
  fetchSaleItems();
});

const filteredSaleItems = computed(() => {
  let items = [...allItems.value];
  const { filterBrands, sortField, sortDirection } = filters.value;

  if (filterBrands.length > 0) {
    items = items.filter(i => filterBrands.includes(i.brand?.name));
  }

  if (sortField && sortDirection) {
    items.sort((a, b) => {
      const aValue = sortField.split('.').reduce((o, k) => o?.[k], a);
      const bValue = sortField.split('.').reduce((o, k) => o?.[k], b);
      if (aValue < bValue) return sortDirection === 'asc' ? -1 : 1;
      if (aValue > bValue) return sortDirection === 'asc' ? 1 : -1;
      return 0;
    });
  }

  return items;
});

watch(() => filters.value, (newVal) => {
  productStore.setActivePage(newVal.activePage);
}, { deep: true });
</script>

<template>
  <div class="p-4">
    <FilterSort @update:filters="val => filters.value = val" />

    <div v-if="filteredSaleItems.length === 0" class="text-center text-gray-500 py-8">
      No sale item
    </div>

    <ProductGallery v-else :saleItems="filteredSaleItems" :viewType="filters.value.viewType || 'gallery'" />
  </div>
</template>

<style scoped>
</style>
