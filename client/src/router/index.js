import { createRouter, createWebHistory } from 'vue-router';
import LandingLayout from '@/layout/LandingLayout.vue';
import DefaultLayout from '@/layout/DefaultLayout.vue';
import HomePage from '@/pages/HomePage.vue';
import MainPage from '@/components/user/MainPage.vue';
import ProductList from '@/components/user/ProductList.vue';
import ProductManager from '@/components/user/ProductManager.vue';
import ProductDetail from '@/components/user/ProductDetail.vue';

const routes = [
  {
    path: '/',
    component: LandingLayout,
    children: [
      {
        path: '',
        name: 'Home',
        component: HomePage,
      },
    ],
  },
  {
    path: '/',
    component: DefaultLayout,
    children: [
      {
        path: 'main',
        name: 'Main',
        component: MainPage,
      },
      {
        path: 'sale-items',
        component: ProductManager,
        children: [
          {
            path: ':productId',
            component: ProductDetail,
            name: 'product-detail',
          },
        ],
      },
      {
        path: 'sale-items',
        component: ProductManager,
        children: [
          {
            path: ':productId',
            component: ProductDetail,
            name: 'product-detail',
          },
        ],
      },
      {
        path: '/error',
        name: 'error-page',
        component: () => import('@/pages/ErrorCodePage.vue'),
      },
    ],
  },
  // fallback route
  {
    path: '/:pathMatch(.*)*',
    redirect: { name: 'error-page', query: { code: 404 } },
  },
];

const router = createRouter({
  history: createWebHistory(''),
  routes,
});

export default router;
