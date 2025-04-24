import { ref, watch } from 'vue';

const defaultTheme = 'light';

const savedTheme = localStorage.getItem('theme') || defaultTheme;
const theme = ref(savedTheme);

document.documentElement.setAttribute('data-theme', theme.value);

watch(theme, (newTheme) => {
  document.documentElement.setAttribute('data-theme', newTheme);
  localStorage.setItem('theme', newTheme);
});

const toggleTheme = () => {
  theme.value = theme.value === 'light' ? 'dracula' : 'light';
};

export function useTheme() {
  return { theme, toggleTheme };
}
