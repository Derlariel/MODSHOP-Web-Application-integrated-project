import {createRouter, createWebHistory} from 'vue-router';
import LandingLayout from '@/layout/LandingLayout.vue';
import DefaultLayout from '@/layout/DefaultLayout.vue';
import HomePage from '@/pages/HomePage.vue';
import MainPage from '@/components/user/MainPage.vue';
import ProductList from '@/components/user/ProductList.vue';
import ProductManager from '@/components/user/ProductManager.vue';
import ProductDetail from '@/components/user/ProductDetail.vue';
import NotFound from '@/pages/NotFound.vue';
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
        path: 'v1/sale-items',
        name: 'Product',
        component: ProductList,
      },
      {
        path: '/v1/sale-items',
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
      path:'/error',
      name: 'NotFound',
      component: NotFound,
     }
    ],
  },
];

const router = createRouter ({
  history: createWebHistory (),
  routes,
});

export default router;
