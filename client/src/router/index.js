import { createRouter, createWebHistory } from "vue-router";
import { useAuthStore } from "@/stores/useAuthStore";
import { useProductStore } from "@/stores/useProductStore";

function requireAuth(to, from, next) {
  const auth = useAuthStore();
  if (!auth.isAuthenticated || !auth.user) {
    next({ name: "Login" });
  } else {
    next();
  }
}

function requireSeller(to, from, next) {
  const auth = useAuthStore();
  if (!auth.isAuthenticated || !auth.user) {
    next({ name: "Login" });
  } else if (auth.user.role === "SELLER") {
    next();
  } else if (auth.user.role === "BUYER") {
    next({ name: "product-gallery" });
  } else {
    next({ name: "product-gallery" });
  }
}

async function requireOwner(to, from, next) {
  const auth = useAuthStore();
  if (!auth.isAuthenticated || !auth.user || auth.user.role !== "SELLER") {
    return next({ name: "product-gallery" });
  }
  try {
    const productStore = useProductStore();
    const detail = await productStore.fetchProductDetail(
      Number(to.params.productId)
    );
    if (detail && Number(detail.sellerId) === Number(auth.user.id)) {
      return next();
    }
  } catch (e) {}
  return next({ name: "product-gallery" });
}
import LandingLayout from "@/layout/LandingLayout.vue";
import DefaultLayout from "@/layout/DefaultLayout.vue";
import HomePage from "@/pages/HomePage.vue";
import MainPage from "@/components/MainPage.vue";
import ProductManager from "@/components/admin/product/ProductManager.vue";
import ProductDetail from "@/components/shared/ProductDetail.vue";
import ProductList from "@/components/admin/product/ProductList.vue";
import ProductAdd from "@/components/admin/product/ProductCreate.vue";
import ProductEdit from "@/components/admin/product/ProductEdit.vue";
import BrandManager from "@/components/admin/brand/BrandManager.vue";
import BrandCreate from "@/components/admin/brand/BrandCreate.vue";
import BrandEdit from "@/components/admin/brand/BrandEdit.vue";
import About from "@/pages/About.vue";
import Register from "@/pages/Register.vue";
import Login from "@/pages/Login.vue";
import Profile from "@/pages/Profile.vue";
import ProfileEdit from "@/pages/ProfileEdit.vue";
import Cart from "@/pages/CartPage.vue";
import CartPage from "@/pages/CartPage.vue";
import YourOrdersPage from "@/pages/YourOrdersPage.vue";
import YourOrderPage from "@/pages/YourOrderPage.vue";
import SaleOrdersPage from "@/pages/SaleOrdersPage.vue";
import ResetPasswordPage from "@/pages/ResetPasswordPage.vue";
const routes = [
  {
    path: "/",
    component: LandingLayout,
    children: [
      {
        path: "",
        name: "Home",
        component: HomePage,
      },
    ],
  },
  { path: "/register", name: "Register", component: Register },
  { path: "/login", name: "Login", component: Login },
  { path: "/signin", name: "SignIn", component: Login },
  {
    path: "/forgot-password",
    name: "ForgotPasswordPage",
    component: ResetPasswordPage,
  },
  {
  path: "/reset-password",
  name: "ResetPasswordPage",
  component: ResetPasswordPage,
},
  {
    path: "/",
    component: DefaultLayout,
    children: [
      {
        path: "main",
        name: "Main",
        component: MainPage,
      },
      {
        path: "profile",
        name: "Profile",
        component: Profile,
        beforeEnter: requireAuth,
      },
      {
        path: "profile/edit",
        name: "ProfileEdit",
        component: ProfileEdit,
        beforeEnter: requireAuth,
      },
      {
        path: "sale-items",
        name: "product-gallery",
        component: ProductManager,
        children: [
          {
            path: "list",
            component: ProductList,
            name: "product-list",
            beforeEnter: requireSeller,
          },
          {
            path: "add",
            component: ProductAdd,
            name: "product-add",
            beforeEnter: requireSeller,
          },
          {
            path: ":productId",
            component: ProductDetail,
            name: "product-detail",
          },
        ],
      },
      {
        path: "sale-items/:productId/edit",
        component: ProductEdit,
        name: "sale-items-edit",
        beforeEnter: requireOwner,
      },
      {
        path: "brands",
        name: "brands-list",
        component: BrandManager,
        children: [
          {
            path: "add",
            component: BrandCreate,
            name: "brand-add",
          },
        ],
      },
      {
        path: "brands/:brandId/edit",
        component: BrandEdit,
        name: "brands-edit",
      },
      {
        path: "about",
        name: "about",
        component: About,
      },

      {
        path: "cart",
        name: "CartPage",
        component: CartPage,
        beforeEnter: requireAuth,
      },
      {
        path: "your-orders",
        name: "YourOrdersPage",
        component: YourOrdersPage,
        beforeEnter: requireAuth,
      },
      {
        path: "your-orders/:orderId",
        name: "YourOrderPage",
        component: YourOrderPage,
        beforeEnter: requireAuth,
      },
      {
        path: "sale-orders",
        name: "SaleOrdersPage",
        component: SaleOrdersPage,
        beforeEnter: requireSeller,
      },
      {
        path: "change-password",
        name: "ChangePasswordPage",
        component: ResetPasswordPage,
        beforeEnter: requireAuth,
      },

      {
        path: "/error",
        name: "error-page",
        component: () => import("@/pages/ErrorCodePage.vue"),
      },
    ],
  },
  // fallback route
  {
    path: "/:catchAll(.*)",
    redirect: "/error?code=404",
  },
];

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
});

export default router;
