import { ref, computed } from 'vue'

export function useOrderFilters() {
  const defaultFilters = {
    keyword: '',
    sellerName: '',
    brandName: '',
    model: '',
    startDate: null,
    endDate: null,
    orderStatus: null,
    page: 0,
    size: 10,
    sortField: 'orderDate',
    sortDirection: 'desc'
  }

  const filters = ref({ ...defaultFilters })

  const buildQueryParams = () => {
    const params = new URLSearchParams()

    Object.entries(filters.value).forEach(([key, value]) => {
      if (value !== null && value !== '' && value !== undefined) {
        params.append(key, value.toString())
      }
    })

    return params.toString()
  }

  const hasActiveFilters = computed(() => {
    return Object.keys(defaultFilters).some((key) => {
      if (['page', 'size', 'sortField', 'sortDirection'].includes(key)) return false
      return filters.value[key]
    })
  })

  const clearFilters = () => {
    filters.value = { ...defaultFilters, size: filters.value.size }
  }

  const setPage = (page) => (filters.value.page = page)
  const setSize = (size) => {
    filters.value.size = size
    filters.value.page = 0
  }
  const setSort = (field, direction) => {
    filters.value.sortField = field
    filters.value.sortDirection = direction
    filters.value.page = 0
  }

  return {
    filters,
    buildQueryParams,
    hasActiveFilters,
    clearFilters,
    setPage,
    setSize,
    setSort
  }
}