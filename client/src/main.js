import { createApp } from 'vue'
import './style.css'
import App from './App.vue'
import { createPinia } from 'pinia'
import timeFormat from '@/plugin/timeFormat'
import router from '@/router/index'
const app = createApp(App)
const pinia = createPinia() 
app.use(pinia)
app.use(timeFormat)
app.use(router)
app.mount('#app')
