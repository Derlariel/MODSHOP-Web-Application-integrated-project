import { createRouter, createWebHistory } from 'vue-router'
import LandingLayout from '@/layout/LandingLayout.vue'
import DefaultLayout from '@/layout/DefaultLayout.vue'
import HomePage from '@/pages/HomePage.vue'
import MainPage from '@/components/user/MainPage.vue'
import ProductList from '@/components/user/ProductList.vue'

const routes = [
  {
    path: '/',
    component: LandingLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: HomePage
      }
    ]
  },
  {
    path: '/',
    component: DefaultLayout,
    children: [
      {
        path: 'main',
        name: 'Main',
        component: MainPage
      },
      {
        path: 'v1/sale-items',
        name: 'Product',
        component: ProductList
      }
    ]
  }
]


const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
