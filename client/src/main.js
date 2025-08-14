import { createApp } from 'vue';
import './style.css';
import App from './App.vue';
import { createPinia } from 'pinia';
import timeFormat from '@/plugin/timeFormat';
import router from '@/router/index';

import favicon from '@/assets/icon.png'; 

const link = document.createElement('link');
link.rel = 'icon';
link.type = 'image/png';
link.href = favicon;
document.head.appendChild(link);

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(timeFormat);
app.use(router);

app.mount('#app');
