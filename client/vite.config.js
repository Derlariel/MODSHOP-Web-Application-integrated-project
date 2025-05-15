import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import tailwindcss from "@tailwindcss/vite";
import { fileURLToPath, URL } from "node:url";

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
    '/itb-mshop': {
      target: 'http://localhost:8080',
      changeOrigin: true,
    }
  }
}
});