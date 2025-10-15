import { defineStore } from "pinia";
import { getSellerOrders, getSellerNewOrdersCount } from "@/api/orderAPI";

export const useSellerOrdersStore = defineStore("sellerOrdersStore", {
  state: () => ({
    tab: "new", // default to 'new' tab for sellers
    page: 1,
    size: 10,
    sort: "id,desc",
    loading: false,
    orders: [],
    totalPages: 0,
    totalElements: 0,
    newOrdersCount: 0,
  }),
  actions: {
    setTab(tab) {
      this.tab = tab;
      this.page = 1;
    },
    setPage(page) {
      this.page = page;
    },
    async refreshBadge(sellerId) {
      try {
        this.newOrdersCount = await getSellerNewOrdersCount(sellerId);
      } catch (e) {
        this.newOrdersCount = 0;
      }
    },

    incrementBadge() {
      this.newOrdersCount++;
    },

    decrementBadge() {
      if (this.newOrdersCount > 0) {
        this.newOrdersCount--;
      }
    },

    async fetchOrders(sellerId) {
      this.loading = true;
      try {
        const res = await getSellerOrders(sellerId, {
          tab: this.tab,
          page: this.page - 1,
          size: this.size,
          sort: this.sort,
        });
        console.log("res",res)
        this.orders = res.data?.content || [];
        console.log(this?.orders)
        this.totalPages = res.data?.totalPages || 0;
        this.totalElements = res.data?.totalElements || 0;
      } catch (e) {
        console.error("Failed to fetch seller orders", e);
        this.orders = [];
        this.totalPages = 0;
        this.totalElements = 0;
      } finally {
        this.loading = false;
      }
    },
  },
});
