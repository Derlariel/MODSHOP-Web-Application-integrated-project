<script setup>
import Advisor from '@/assets/group-about/teacher.png';
</script>

<template>
  <div class=" min-h-screen py-16 px-4 flex flex-col justify-center items-center ">
    <div class="max-w-2xl w-full bg-white rounded-3xl shadow-lg p-8 mb-12 text-center border border-blue-100">
      <h1 class="text-3xl md:text-4xl font-bold text-blue-700 mb-2">Group Advisor</h1>
      <div class="flex flex-col items-center mt-6">
        <img :src="Advisor" alt="Advisor" class="w-48 h-48 rounded-full border-4 border-blue-200 shadow mb-4" />
        <div>
          <h2 class="text-xl font-semibold text-gray-800">Asst. Prof. Dr. Kriengkrai Porkaew</h2>
          <p class="text-gray-500">อาจารย์ที่ปรึกษากลุ่ม KK-1 วิชา INT221</p>
        </div>
      </div>
    </div>

    <div class="max-w-4xl w-full bg-white rounded-3xl shadow-lg p-8 text-center border border-blue-100">
      <h2 class="text-2xl font-semibold text-blue-600 mb-8">Project Members</h2>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-8">
        <div class="flex flex-col items-center">
          <img src="https://cdn-icons-png.flaticon.com/512/3135/3135715.png" alt="Student 1" class="w-20 h-20 rounded-full border-4 border-blue-100 shadow mb-3" />
          <div>
            <h3 class="text-lg font-medium text-gray-800">Kongphob Kongsan</h3>
            <p class="text-gray-500">Student ID: 66130500004</p>
          </div>
        </div>
        <div class="flex flex-col items-center">
          <img src="https://cdn-icons-png.flaticon.com/512/3135/3135715.png" alt="Student 2" class="w-20 h-20 rounded-full border-4 border-blue-100 shadow mb-3" />
          <div>
            <h3 class="text-lg font-medium text-gray-800">Napat Chumtham</h3>
            <p class="text-gray-500">Student ID: 66130500027</p>
          </div>
        </div>
        <div class="flex flex-col items-center">
          <img src="https://cdn-icons-png.flaticon.com/512/3135/3135715.png" alt="Student 3" class="w-20 h-20 rounded-full border-4 border-blue-100 shadow mb-3" />
          <div>
            <h3 class="text-lg font-medium text-gray-800">Pongsakorn Sinsomboonsuk</h3>
            <p class="text-gray-500">Student ID: 66130500062</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>