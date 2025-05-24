<script setup>
import 'swiper/css';
import 'swiper/css/pagination';
import 'swiper/css/effect-fade';
import { ref, onMounted, onUnmounted } from 'vue';

import AppFooter from '@/components/shared/AppFooter.vue';

const selectedBrand = ref(null);

// Featured brands for Summer Sale
const featuredBrands = [
  {
    id: 1,
    name: "Apple",
    tagline: "Think Different",
    logo: "/placeholder.svg?height=80&width=80&text=Apple",
    color: "from-gray-100 to-white",
    products: ["iPhone", "MacBook", "iPad", "Apple Watch"]
  },
  {
    id: 2,
    name: "Beats",
    tagline: "Hear What You Want",
    logo: "/placeholder.svg?height=80&width=80&text=Beats",
    color: "from-gray-200 to-gray-50",
    products: ["Beats Studio", "Beats Solo", "Powerbeats", "Beats Pill"]
  },
  {
    id: 3,
    name: "Belkin",
    tagline: "Quality & Innovation",
    logo: "/placeholder.svg?height=80&width=80&text=Belkin",
    color: "from-gray-100 to-white",
    products: ["Chargers", "Cables", "Screen Protectors", "Cases"]
  },
  {
    id: 4,
    name: "Logitech",
    tagline: "Defy Logic",
    logo: "/placeholder.svg?height=80&width=80&text=Logitech",
    color: "from-gray-200 to-gray-50",
    products: ["Keyboards", "Mice", "Webcams", "Speakers"]
  },
  {
    id: 5,
    name: "Sony",
    tagline: "Be Moved",
    logo: "/placeholder.svg?height=80&width=80&text=Sony",
    color: "from-gray-100 to-white",
    products: ["Headphones", "Speakers", "Cameras", "TVs"]
  },
  {
    id: 6,
    name: "Samsung",
    tagline: "Do What You Can't",
    logo: "/placeholder.svg?height=80&width=80&text=Samsung",
    color: "from-gray-200 to-gray-50",
    products: ["Galaxy", "Monitors", "TVs", "Accessories"]
  }
];

const selectBrand = (brand) => {
  selectedBrand.value = selectedBrand.value === brand ? null : brand;
};


onMounted(() => {
  window.addEventListener('scroll', handleScroll);
  handleScroll();
});

onUnmounted(() => {
  window.removeEventListener('scroll', handleScroll);
});
</script>

<template>
  <div class="bg-white text-black min-h-screen">

    <!-- Apple Watch section -->
    <section class="bg-white text-center pt-25 pb-24 overflow-hidden">
      <div class="max-w-[980px] mx-auto px-4">
        <div class="flex flex-col items-center">
          <div class="flex items-center justify-center">
            <svg class="h-6 w-6 text-black mr-1" viewBox="0 0 14 17" fill="currentColor" xmlns="http://www.w3.org/2000/svg">
              <path d="M13.0729 5.68537C12.9799 5.76364 11.1418 6.86971 11.1418 9.23003C11.1418 11.9843 13.5589 13.196 13.6519 13.2432C13.6364 13.3058 13.2658 14.5175 12.3883 15.7603C11.6038 16.8663 10.7883 17.9724 9.53275 17.9724C8.27724 17.9724 7.91426 17.2501 6.48673 17.2501C5.09159 17.2501 4.5 17.9724 3.33588 17.9724C2.17176 17.9724 1.30386 16.9291 0.457981 15.8231C-0.543266 14.4797 -1.45455 12.2411 -1.45455 10.0963C-1.45455 6.55677 1.05687 4.70708 3.52316 4.70708C4.74628 4.70708 5.75951 5.49677 6.51163 5.49677C7.21736 5.49677 8.33509 4.67537 9.71263 4.67537C10.1677 4.67537 12.0058 4.70708 13.0729 5.68537ZM9.09829 2.78001C9.6283 2.15767 10.0058 1.28535 10.0058 0.413029C10.0058 0.303126 9.99025 0.193223 9.95916 0.114959C9.11886 0.146311 8.11772 0.673107 7.49129 1.36712C7.00258 1.86221 6.54627 2.73453 6.54627 3.62214C6.54627 3.74734 6.57736 3.87254 6.58945 3.91954C6.65164 3.93519 6.75622 3.95084 6.8608 3.95084C7.61292 3.95084 8.52221 3.44045 9.09829 2.78001Z" />
            </svg>
            <span class="text-2xl font-semibold tracking-tight">WATCH</span>
          </div>
          <p class="text-sm font-medium text-gray-500 mt-1">SERIES 10</p>
          <h2 class="text-4xl md:text-5xl font-semibold mt-2 mb-4">Thinstant classic.</h2>
          
          <div class="flex space-x-4 mt-2">
            <a href="#" class="bg-blue-600 text-white rounded-full px-5 py-1 text-sm font-normal hover:bg-blue-700 transition-all duration-300 transform hover:scale-105 active:scale-95">
              Learn more
            </a>
            <a href="#" class="border border-gray-300 rounded-full px-5 py-1 text-sm font-normal text-gray-800 hover:border-gray-400 transition-all duration-300 transform hover:scale-105 active:scale-95">
              Buy
            </a>
          </div>
          
          <div class="mt-8 max-w-4xl mx-auto relative overflow-hidden group">
            <img src="/placeholder.png?height=500&width=1000&text=Apple+Watch+Series+10" alt="Apple Watch Series 10" 
                 class="w-full transition-transform duration-700 group-hover:scale-[1.02]" />
            <div class="absolute inset-0 bg-gradient-to-t from-white to-transparent opacity-0 group-hover:opacity-10 transition-opacity duration-700"></div>
          </div>
        </div>
      </div>
    </section>

    <!-- Featured Brands Section (Summer Sale) -->
    <section class="bg-gray-50 py-24">
      <div class="max-w-[980px] mx-auto px-4">
        <h2 class="text-4xl font-semibold text-center mb-2">Summer Sale</h2>
        <p class="text-xl text-gray-500 text-center mb-12">Exclusive deals from premium brands.</p>
        
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <div v-for="brand in featuredBrands" :key="brand.id" 
               @click="selectBrand(brand)"
               class="bg-gradient-to-b cursor-pointer rounded-2xl p-6 transition-all duration-500 group relative overflow-hidden shadow-sm hover:shadow-lg"
               :class="[`${brand.color}`, selectedBrand === brand ? 'ring-2 ring-blue-500 scale-[1.02]' : '']">
            
            <!-- Brand content -->
            <div class="relative z-10 flex flex-col items-center">
              <div class="w-20 h-20 mb-4 transition-transform duration-500 group-hover:scale-110 group-active:scale-95">
                <img :src="brand.logo" :alt="brand.name" class="w-full h-full object-contain" />
              </div>
              
              <h3 class="text-xl font-semibold text-gray-900 mb-1 transition-all duration-300 group-hover:translate-y-[-2px]">{{ brand.name }}</h3>
              <p class="text-gray-500 mb-4 italic text-sm">{{ brand.tagline }}</p>
              
              <!-- Products list that appears when selected -->
              <div class="overflow-hidden transition-all duration-500 w-full"
                   :class="selectedBrand === brand ? 'max-h-40 opacity-100' : 'max-h-0 opacity-0'">
                <div class="pt-4 border-t border-gray-200 mt-2">
                  <p class="font-medium text-gray-700 mb-2">Featured Products:</p>
                  <ul class="text-sm text-gray-600 space-y-1">
                    <li v-for="(product, index) in brand.products" :key="index" class="flex items-center">
                      <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 text-blue-500 mr-2" viewBox="0 0 20 20" fill="currentColor">
                        <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd" />
                      </svg>
                      {{ product }}
                    </li>
                  </ul>
                </div>
              </div>
              
              <div class="mt-4 w-full">
                <a href="#" class="inline-block bg-blue-600 text-white font-normal py-1 px-4 rounded-full transition-all duration-300 text-sm transform group-hover:scale-105 group-active:scale-95 hover:bg-blue-700 w-full text-center">
                  Shop Now
                </a>
              </div>
            </div>
            
            <!-- Decorative elements -->
            <div class="absolute bottom-0 right-0 w-24 h-24 bg-gradient-to-tl from-gray-200/50 to-transparent rounded-full -mr-8 -mb-8 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
            <div class="absolute top-0 left-0 w-16 h-16 bg-gradient-to-br from-gray-200/50 to-transparent rounded-full -ml-6 -mt-6 opacity-0 group-hover:opacity-100 transition-opacity duration-500"></div>
          </div>
        </div>
        
        <div class="mt-12 text-center">
          <a href="#" class="text-blue-600 font-normal text-lg inline-flex items-center hover:underline group">
            See all brands
            <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1 transform transition-transform duration-300 group-hover:translate-x-1" viewBox="0 0 20 20" fill="currentColor">
              <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
            </svg>
          </a>
        </div>
      </div>
    </section>

    <!-- iPhone section -->
    <section class="bg-white text-center py-24 overflow-hidden">
      <div class="max-w-[980px] mx-auto px-4">
        <h2 class="text-5xl font-semibold mb-2">iPhone</h2>
        <p class="text-2xl mb-4">Meet the iPhone 16 family.</p>
        
        <div class="flex space-x-4 justify-center mt-2">
          <a href="#" class="bg-blue-600 text-white rounded-full px-5 py-1 text-sm font-normal hover:bg-blue-700 transition-all duration-300 transform hover:scale-105 active:scale-95">
            Learn more
          </a>
          <a href="#" class="border border-gray-300 rounded-full px-5 py-1 text-sm font-normal text-gray-800 hover:border-gray-400 transition-all duration-300 transform hover:scale-105 active:scale-95">
            Shop iPhone
          </a>
        </div>
        
        <p class="mt-6 text-transparent bg-clip-text bg-gradient-to-r from-blue-600 to-purple-500 font-medium">
          Built for Apple Intelligence.
        </p>
        
        <div class="mt-8 max-w-4xl mx-auto relative overflow-hidden group">
          <img src="/placeholder.png?height=500&width=1000&text=iPhone+16+Family" alt="iPhone 16 Family" 
               class="w-full transition-transform duration-700 group-hover:scale-[1.02]" />
          <div class="absolute inset-0 bg-gradient-to-t from-white to-transparent opacity-0 group-hover:opacity-10 transition-opacity duration-700"></div>
        </div>
      </div>
    </section>

    <!-- Two-column section -->
    <section class="bg-gray-50 py-4">
      <div class="max-w-[980px] mx-auto px-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- iPad Pro -->
          <div class="bg-white rounded-3xl p-8 text-center overflow-hidden group relative">
            <!-- Subtle gradient overlay that appears on hover -->
            <div class="absolute inset-0 bg-gradient-to-b from-gray-50 to-white opacity-0 group-hover:opacity-100 transition-opacity duration-700"></div>
            
            <div class="relative z-10">
              <h2 class="text-3xl font-semibold mb-1 transition-transform duration-300 group-hover:translate-y-[-2px]">iPad Pro</h2>
              <p class="text-lg text-gray-500 mb-4 transition-transform duration-300 group-hover:translate-y-[-2px]">Supercharged by M2.</p>
              <div class="flex justify-center space-x-4 mb-6">
                <a href="#" class="text-blue-600 font-normal text-sm inline-flex items-center group-hover:underline transition-all duration-300">
                  Learn more
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1 transform transition-transform duration-300 group-hover:translate-x-1" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
                  </svg>
                </a>
                <a href="#" class="text-blue-600 font-normal text-sm inline-flex items-center group-hover:underline transition-all duration-300">
                  Buy
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1 transform transition-transform duration-300 group-hover:translate-x-1" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
                  </svg>
                </a>
              </div>
              <img src="/placeholder.png?height=300&width=400&text=iPad+Pro" alt="iPad Pro" 
                   class="w-full max-w-xs mx-auto transform transition-all duration-700 group-hover:scale-105 group-hover:translate-y-[-5px]" />
            </div>
          </div>
          
          <!-- MacBook Air -->
          <div class="bg-white rounded-3xl p-8 text-center overflow-hidden group relative">
            <!-- Subtle gradient overlay that appears on hover -->
            <div class="absolute inset-0 bg-gradient-to-b from-gray-50 to-white opacity-0 group-hover:opacity-100 transition-opacity duration-700"></div>
            
            <div class="relative z-10">
              <h2 class="text-3xl font-semibold mb-1 transition-transform duration-300 group-hover:translate-y-[-2px]">MacBook Air</h2>
              <p class="text-lg text-gray-500 mb-4 transition-transform duration-300 group-hover:translate-y-[-2px]">Supercharged by M3.</p>
              <div class="flex justify-center space-x-4 mb-6">
                <a href="#" class="text-blue-600 font-normal text-sm inline-flex items-center group-hover:underline transition-all duration-300">
                  Learn more
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1 transform transition-transform duration-300 group-hover:translate-x-1" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
                  </svg>
                </a>
                <a href="#" class="text-blue-600 font-normal text-sm inline-flex items-center group-hover:underline transition-all duration-300">
                  Buy
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1 transform transition-transform duration-300 group-hover:translate-x-1" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
                  </svg>
                </a>
              </div>
              <img src="/placeholder.png?height=300&width=400&text=MacBook+Air" alt="MacBook Air" 
                   class="w-full max-w-xs mx-auto transform transition-all duration-700 group-hover:scale-105 group-hover:translate-y-[-5px]" />
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Second two-column section -->
    <section class="bg-gray-50 py-4 mb-12">
      <div class="max-w-[980px] mx-auto px-4">
        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <!-- AirPods Pro -->
          <div class="bg-white rounded-3xl p-8 text-center overflow-hidden group relative">
            <!-- Subtle gradient overlay that appears on hover -->
            <div class="absolute inset-0 bg-gradient-to-b from-gray-50 to-white opacity-0 group-hover:opacity-100 transition-opacity duration-700"></div>
            
            <div class="relative z-10">
              <h2 class="text-3xl font-semibold mb-1 transition-transform duration-300 group-hover:translate-y-[-2px]">AirPods Pro</h2>
              <p class="text-lg text-gray-500 mb-4 transition-transform duration-300 group-hover:translate-y-[-2px]">Adaptive Audio. Now playing.</p>
              <div class="flex justify-center space-x-4 mb-6">
                <a href="#" class="text-blue-600 font-normal text-sm inline-flex items-center group-hover:underline transition-all duration-300">
                  Learn more
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1 transform transition-transform duration-300 group-hover:translate-x-1" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
                  </svg>
                </a>
                <a href="#" class="text-blue-600 font-normal text-sm inline-flex items-center group-hover:underline transition-all duration-300">
                  Buy
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1 transform transition-transform duration-300 group-hover:translate-x-1" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
                  </svg>
                </a>
              </div>
              <img src="/placeholder.png?height=300&width=400&text=AirPods+Pro" alt="AirPods Pro" 
                   class="w-full max-w-xs mx-auto transform transition-all duration-700 group-hover:scale-105 group-hover:translate-y-[-5px]" />
            </div>
          </div>
          
          <!-- Apple Vision Pro -->
          <div class="bg-white rounded-3xl p-8 text-center overflow-hidden group relative">
            <!-- Subtle gradient overlay that appears on hover -->
            <div class="absolute inset-0 bg-gradient-to-b from-gray-50 to-white opacity-0 group-hover:opacity-100 transition-opacity duration-700"></div>
            
            <div class="relative z-10">
              <h2 class="text-3xl font-semibold mb-1 transition-transform duration-300 group-hover:translate-y-[-2px]">Vision Pro</h2>
              <p class="text-lg text-gray-500 mb-4 transition-transform duration-300 group-hover:translate-y-[-2px]">Welcome to the era of spatial computing.</p>
              <div class="flex justify-center space-x-4 mb-6">
                <a href="#" class="text-blue-600 font-normal text-sm inline-flex items-center group-hover:underline transition-all duration-300">
                  Learn more
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1 transform transition-transform duration-300 group-hover:translate-x-1" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
                  </svg>
                </a>
                <a href="#" class="text-blue-600 font-normal text-sm inline-flex items-center group-hover:underline transition-all duration-300">
                  Buy
                  <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4 ml-1 transform transition-transform duration-300 group-hover:translate-x-1" viewBox="0 0 20 20" fill="currentColor">
                    <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd" />
                  </svg>
                </a>
              </div>
              <img src="/placeholder.png?height=300&width=400&text=Vision+Pro" alt="Vision Pro" 
                   class="w-full max-w-xs mx-auto transform transition-all duration-700 group-hover:scale-105 group-hover:translate-y-[-5px]" />
            </div>
          </div>
        </div>
      </div>
    </section>

    <AppFooter />
    
    
  </div>
</template>

<style scoped>
/* Apple-style font */
@font-face {
  font-family: 'SF Pro Display';
  src: local('SF Pro Display'), local('SFProDisplay-Regular');
  font-weight: normal;
  font-style: normal;
}

:deep(*) {
  font-family: 'SF Pro Display', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}
</style>