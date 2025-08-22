<script setup>
import { ref, watch } from 'vue'
import { Search, X } from 'lucide-vue-next'
import BaseInput from '@/components/shared/BaseInput.vue'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: 'Search sale items...'
  }
})

const emit = defineEmits(['update:modelValue', 'search', 'clear'])

const searchText = ref(props.modelValue)

// Watch for external changes to modelValue
watch(() => props.modelValue, (newValue) => {
  searchText.value = newValue
})

// Watch for internal changes to searchText
watch(searchText, (newValue) => {
  emit('update:modelValue', newValue)
})

const handleSearch = () => {
  emit('search', searchText.value.trim())
}

const handleClear = () => {
  searchText.value = ''
  emit('update:modelValue', '')
  emit('clear')
}

const handleKeyDown = (event) => {
  if (event.key === 'Enter') {
    event.preventDefault()
    handleSearch()
  }
}
</script>

<template>
  <div class="relative w-full max-w-md mx-auto">
    <div class="relative flex items-center">
      <!-- Search Input using BaseInput -->
      <div class="relative w-full">
        <BaseInput
          v-model="searchText"
          :placeholder="placeholder"
          type="text"
          cypress="itbms-search-text"
          @keydown="handleKeyDown"
          class="itbms-search-text"
        />
        
        <!-- Search Icon -->
        <button
          @click="handleSearch"
          class="absolute left-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-300 transition-colors duration-200 z-10"
          type="button"
          title="Search"
        >
          <Search class="w-4 h-4 md:w-5 md:h-5" />
        </button>
        
        <!-- Clear Button -->
        <button
          v-if="searchText.trim()"
          @click="handleClear"
          class="itbms-search-clear-button absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-300 transition-colors duration-200 z-10"
          type="button"
          title="Clear search"
        >
          <X class="w-4 h-4 md:w-5 md:h-5" />
        </button>
      </div>
    </div>
  </div>
</template>

<style scoped>
/* Custom styling for search input to add padding for icons */
:deep(.itbms-search-text input) {
  padding-left: 2.5rem !important;
  padding-right: 2.5rem !important;
}

/* Position adjustment for icons */
.itbms-search-btn,
.itbms-search-clear-button {
  z-index: 20;
}

/* Mobile responsive adjustments */
@media (max-width: 640px) {
  :deep(.itbms-search-text input) {
    font-size: 0.875rem;
    padding-left: 2.25rem !important;
    padding-right: 2.25rem !important;
  }
  
  .itbms-search-btn,
  .itbms-search-clear-button {
    left: 0.625rem;
  }
  
  .itbms-search-clear-button {
    right: 0.625rem;
    left: auto;
  }
}
</style>
