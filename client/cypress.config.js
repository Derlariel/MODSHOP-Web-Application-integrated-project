import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    specPattern: 'cypress/e2e/**/*.{cy,spec}.{js,jsx,ts,tsx}',
    baseUrl: 'http://ip24kk1.sit.kmutt.ac.th/',
    baseAPI: 'http://ip24kk1.sit.kmutt.ac.th:8000/itb-mshop',
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },
});
