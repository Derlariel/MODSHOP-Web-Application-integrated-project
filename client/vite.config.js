import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import tailwindcss from "@tailwindcss/vite";
export default defineConfig({
  resolve:{
    alias: {
      '@': '/src',
    }
  },
  plugins: [
    vue(),
    tailwindcss(),
    {
      daisyui: {
        themes: ["light", "dracula"],
      },
    },
  ],
});
