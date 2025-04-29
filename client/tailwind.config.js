// tailwind.config.js
export default {
  content: ["./index.html", "./src/**/*.{vue,js,ts,jsx,tsx}"],
  theme: {
    extend: {
      backgroundImage: {
        'landing-gradient': 'linear-gradient(135deg, #383838 3%, #080808 48%)',
      },
      fontFamily: {
        sans: ['Poppins', ...defaultTheme.fontFamily.sans],
      },
    },    
  },
  plugins: [require("daisyui")],
  daisyui: {
    themes: ["light", "dracula"],
  },
};
