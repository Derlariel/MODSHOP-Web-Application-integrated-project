import { createRouter, createWebHistory } from "vue-router";
import HomePage from "@/pages/HomePage.vue";
import ProductList from "@/components/user/ProductList.vue";
import MainPage from "@/components/user/MainPage.vue";

const routes = [
  { path: "/", component: HomePage },
  { path: "/home", component: MainPage },
  { path: "/v1/sale-items", component: ProductList },
];

const router = createRouter({
  history: createWebHistory(),
  routes,
});

export default router;
