<script setup>
import { useProductStore } from '@/stores/useProductStore.js'
const productStore = useProductStore()

function remove(id) {
  productStore.deleteProduct(id)
}

function update(id) {
  productStore.updateProduct({
    id,
    name: 'ชื่อใหม่แม่งเลย ' + Math.floor(Math.random() * 100)
  })
}

function addNew() {
  productStore.addProduct({
    name: 'มือถือเทพสัส ' + Math.floor(Math.random() * 999),
    description: 'มือถือสุดเฟี้ยวสำหรับ dev เทพๆ แบบเรา',
    stock: 10,
    price: 99999,
    brand: 'DevBrand',
    createdOn: new Date().toISOString().slice(0, 19).replace('T', ' '),
    updatedOn: new Date().toISOString().slice(0, 19).replace('T', ' ')
  })
}
</script>

<template>
  <div class="p-8 max-w-2xl mx-auto space-y-6">
    <h1 class="text-3xl font-bold text-center text-indigo-600">📱 Product Store Demo</h1>

    <ul class="space-y-4">
      <li
        v-for="p in productStore.allProducts"
        :key="p.id"
        class="p-4 bg-white rounded-2xl shadow flex items-center justify-between"
      >
        <div>
          <h2 class="text-xl font-semibold text-gray-800">{{ p.name }}</h2>
          <p class="text-gray-500">{{ p.price.toLocaleString() }} บาท</p>
        </div>
        <div class="space-x-2">
          <button
            @click="remove(p.id)"
            class="px-3 py-1 bg-red-500 text-white rounded hover:bg-red-600"
          >
            🗑 Delete
          </button>
          <button
            @click="update(p.id)"
            class="px-3 py-1 bg-yellow-400 text-gray-800 rounded hover:bg-yellow-500"
          >
            ✏ Update Name
          </button>
        </div>
      </li>
    </ul>

    <div class="text-center">
      <button
        @click="addNew"
        class="mt-4 px-6 py-2 bg-green-500 text-white font-semibold rounded-full hover:bg-green-600"
      >
        ➕ Add Product
      </button>
    </div>
  </div>
</template>

<style scoped>
</style>
