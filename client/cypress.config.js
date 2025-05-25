import { defineConfig } from "cypress";

export default defineConfig({
  e2e: {
    specPattern: 'cypress/e2e/**/*.{cy,spec}.{js,jsx,ts,tsx}',
    // baseUrl: 'http://intproj24.sit.kmutt.ac.th/kk1',
    // baseUrl: 'http://ip24kk1.sit.kmutt.ac.th/kk1',
    // baseAPI: 'http://intproj24.sit.kmutt.ac.th/kk1/itb-mshop',
    // experimentalRunAllSpecs: true,
    baseUrl: 'http://localhost:5173/kk1',
    baseAPI: 'http://localhost:8080/itb-mshop',
    setupNodeEvents(on, config) {
      // implement node event listeners here
    },
  },
});
