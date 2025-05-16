<script setup>

</script>
 
<template>
  <div class="p-4 max-w-sm w-full">
    <div class="animate-pulse space-y-4">
      <div class="relative overflow-hidden rounded-xl bg-neutral-800 h-48 w-full shadow-lg">
        <div class="shimmer absolute inset-0"></div>
        
        <div class="absolute inset-0 flex items-center justify-center">
          <div class="loading-spinner">
            <svg class="animate-spin h-12 w-12 text-white opacity-70" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
              <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
              <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
            </svg>
            <div class="mt-2 text-white text-sm">Loading...</div>
          </div>
        </div>
      </div>

      <div class="space-y-3">
        <div class="h-6 bg-neutral-800 rounded-md flex items-center">
          <div class="h-full w-1/4 bg-neutral-700 rounded-l-md"></div>
        </div>
        <div class="h-4 bg-neutral-800 rounded-md w-5/6"></div>
        <div class="h-4 bg-neutral-800 rounded-md w-4/6"></div>
      </div>

      <div class="flex justify-between items-center mt-6">
        <div class="h-8 bg-neutral-700 rounded-md w-1/3"></div>
        <div class="h-10 bg-white bg-opacity-20 rounded-md w-1/4"></div>
      </div>
    </div>
  </div>
</template>
 
<style scoped>
.shimmer {
  background: linear-gradient(
    to right,
    rgba(255, 255, 255, 0) 0%,
    rgba(255, 255, 255, 0.05) 50%,
    rgba(255, 255, 255, 0) 100%
  );
  animation: shimmer 2s infinite;
  transform: translateX(-100%);
}

@keyframes shimmer {
  100% {
    transform: translateX(100%);
  }
}


</style>