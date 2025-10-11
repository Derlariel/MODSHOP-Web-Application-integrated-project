import { defineStore } from "pinia";
import { getOrdersWithStatus } from "@/api/orderAPI";

export const useOrderStore = defineStore("orderStore", {
  state: () => ({
    activePage: 1,
    orders: [],
    loading: false,
    allPages: 0
  }),

  getters: {
    getActivePage: (state) => state.activePage,
  },

  actions: {
    setActivePage(page) {
      this.activePage = page;
      sessionStorage.setItem("activePage", page);
      this.fetchOrders(this._userId, this._status);
    },

    async fetchOrders(userId, status) {
      this.loading = true;
      this._userId = userId; 
      this._status = status;
      try {
        const res = await getOrdersWithStatus(
          userId,
          this.activePage - 1,
          5,
          "id,desc",
          status
        );
        this.allPages = res.totalPages;
        this.orders = (res.content || []).slice().sort((a, b) => {
          if (a.id != null && b.id != null && a.id !== b.id) {
            return b.id - a.id;
          }
          return new Date(b.orderDate) - new Date(a.orderDate);
        });
      } catch (err) {
        console.error("Error fetching orders:", err);
      } finally {
        this.loading = false;
      }
    },
  },
});
