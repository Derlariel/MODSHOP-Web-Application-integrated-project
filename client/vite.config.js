import { defineConfig } from "vite";
import vue from "@vitejs/plugin-vue";
import tailwindcss from "@tailwindcss/vite";

export default defineConfig({
  base: '/itb-mshop/', // Set the base path here
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
