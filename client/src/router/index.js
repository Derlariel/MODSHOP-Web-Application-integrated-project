import { createRouter,createWebHistory } from "vue-router";
import HomePage from "@/pages/HomePage.vue";
import ProductList from "@/components/user/ProductList.vue";
import MainPage from "@/components/user/MainPage.vue";
const routes = [
    {path:'/',component:HomePage},
    {path:'/sale-items',component:ProductList},
    {path:'/main-page',component:MainPage}

]

const router = createRouter({
    history:createWebHistory(),
    routes
})

export default router