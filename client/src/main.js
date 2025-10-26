import { createApp } from 'vue';
import './style.css';
import App from './App.vue';
import { createPinia } from 'pinia';
import timeFormat from '@/plugin/timeFormat';
import router from '@/router/index';
import { useAuthStore } from '@/stores/useAuthStore';
import { refreshToken } from '@/middleware/interception';
import favicon from '@/assets/icon.png'; 
import imgFallback from '@/directives/imgFallback';

// set favicon
const link = document.createElement('link');
link.rel = 'icon';
link.type = 'image/png';
link.href = favicon;
document.head.appendChild(link);

// create app
const app = createApp(App);
const pinia = createPinia();
app.use(pinia);
app.use(timeFormat);
app.use(router);
app.directive("img", imgFallback);
// mount app **ก่อน** refresh token
app.mount('#app');

// init auth
const auth = useAuthStore();
auth.initPersistence();

// refresh token async **แต่ไม่ block mount**
refreshToken()
  .then(async (ok) => {
    if (ok) {
      await auth.refreshUserData();
    } else {
      auth.user = null;
      localStorage.removeItem('userClaims');
      // แนะนำ: redirect ไป login แทน reload
      // if (router.currentRoute.value.name !== 'Login') {
      //   router.push({ name: 'Login' });
      // }
    }
  })
  .catch((err) => console.error("Token refresh failed:", err));
