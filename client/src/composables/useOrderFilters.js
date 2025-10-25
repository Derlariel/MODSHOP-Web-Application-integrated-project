import { ref, computed } from 'vue'

export function useOrderFilters() {
  const filters = ref({
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
  })

  const buildQueryParams = () => {
    const params = new URLSearchParams()

    if (filters.value.keyword?.trim()) {
      params.append('keyword', filters.value.keyword.trim())
    }
    if (filters.value.sellerName?.trim()) {
      params.append('sellerName', filters.value.sellerName.trim())
    }
    if (filters.value.brandName?.trim()) {
      params.append('brandName', filters.value.brandName.trim())
    }
    if (filters.value.model?.trim()) {
      params.append('model', filters.value.model.trim())
    }
    if (filters.value.startDate) {
      params.append('startDate', filters.value.startDate)
    }
    if (filters.value.endDate) {
      params.append('endDate', filters.value.endDate)
    }
    if (filters.value.orderStatus) {
      params.append('orderStatus', filters.value.orderStatus)
    }

    params.append('page', filters.value.page.toString())
    params.append('size', filters.value.size.toString())
    params.append('sortField', filters.value.sortField)
    params.append('sortDirection', filters.value.sortDirection)

    return params.toString()
  }

  const hasActiveFilters = computed(() => {
    return filters.value.keyword ||
      filters.value.sellerName ||
      filters.value.brandName ||
      filters.value.model ||
      filters.value.startDate ||
      filters.value.endDate ||
      filters.value.orderStatus
  })

  const clearFilters = () => {
    filters.value = {
      keyword: '',
      sellerName: '',
      brandName: '',
      model: '',
      startDate: null,
      endDate: null,
      orderStatus: null,
      page: 0,
      size: filters.value.size,
      sortField: 'orderDate',
      sortDirection: 'desc'
    }
  }

  const setPage = (page) => {
    filters.value.page = page
  }

  const setSize = (size) => {
    filters.value.size = size
    filters.value.page = 0
  }

  const setSort = (sortField, sortDirection) => {
    filters.value.sortField = sortField
    filters.value.sortDirection = sortDirection
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
