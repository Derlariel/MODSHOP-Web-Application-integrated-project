import { createApp } from 'vue';
import './style.css';
import App from './App.vue';
import { createPinia } from 'pinia';
import timeFormat from '@/plugin/timeFormat';
import router from '@/router/index';
import { useAuthStore } from '@/stores/useAuthStore';

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

// Hydrate auth state on startup for persistence across reloads/tabs
const auth = useAuthStore();
auth.initPersistence();
// Optionally validate with backend; if session cookie is valid, refresh minimal user claims
auth.refreshUserData().finally(() => {
	app.mount('#app');
});
