import {createRouter, createWebHistory} from 'vue-router';
import LandingLayout from '@/layout/LandingLayout.vue';
import DefaultLayout from '@/layout/DefaultLayout.vue';
import HomePage from '@/pages/HomePage.vue';
import MainPage from '@/components/user/MainPage.vue';
import ProductManager from '@/components/user/ProductManager.vue';
import ProductDetail from '@/components/user/ProductDetail.vue';
import ProductList from '@/components/user/ProductList.vue';
import ProductAdd from '@/components/user/ProductCreate.vue';
import ProductEdit from '@/components/user/ProductEdit.vue';
import BrandManager from '@/components/user/BrandManager.vue';
import BrandDetail from '@/components/user/BrandDetail.vue';
import BrandCreate from '@/components/user/BrandCreate.vue';

import BrandEdit from '@/components/user/BrandEdit.vue';

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
            path: 'list',
            component: ProductList,
            name: 'product-list',
          },
          {
            path: 'add',
            component: ProductAdd,
            name: 'product-add',
          },

          {
            path: ':productId',
            component: ProductDetail,
            name: 'product-detail',
          },
        ],
      },
      {
        path: 'sale-items/:productId/edit',
        component: ProductEdit,
        name: 'sale-items-edit',
      },
      {
        path: 'brands',
        name: 'brand-list',
        component: BrandManager,
        children: [
          {
            path: 'add',
            component: BrandCreate,
            name: 'brand-add',
          },

          {
            path: ':brandId',
            component: BrandDetail,
            name: 'brand-detail',
          },
        ],
      },
      {
        path: 'brands/:brandId/edit',
        component: BrandEdit,
        name: 'brands-edit',
      },
      

      {
        path: '/error',
        name: 'error-page',
        component: () => import ('@/pages/ErrorCodePage.vue'),
      },
    ],
  },
  // fallback route
  {
    path: '/:pathMatch(.*)*',
    redirect: {name: 'error-page', query: {code: 404}},
  },
];

const router = createRouter ({
  history: createWebHistory (import.meta.env.BASE_URL),
  routes,
});

export default router;
