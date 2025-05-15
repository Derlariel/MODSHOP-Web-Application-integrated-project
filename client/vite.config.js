import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import tailwindcss from "@tailwindcss/vite";
import { fileURLToPath, URL } from "node:url";
import path from "node:path";

export default defineConfig({
  base: '/kk1/',
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)), 
    },
  },
  plugins: [
    vue(),
    tailwindcss(),
  ],
  preview: {
  port: 4173,
  proxy: {
    '/kk1/itb-mshop': {
      target: 'http://localhost:8080',
      changeOrigin: true,
      // ไม่ต้องกังวลเรื่อง CORS
      rewrite: (path) => path.replace(/^\/kk1\/itb-mshop/, ''),
    }
  }
}
});
