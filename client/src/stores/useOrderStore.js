import { defineStore } from "pinia";
import { getOrdersWithId, getOrdersWithStatus, getBuyerNewOrdersCount } from "@/api/orderAPI";

export const useOrderStore = defineStore("orderStore", {
  state: () => ({
    order: null,
    activePage: 1,
    orders: [],
    loading: false,
    allPages: 0,
    newOrdersCount: 0
  }),

  getters: {
    getActivePage: (state) => state.activePage,
  },

  actions: {
    setActivePage(page) {
      this.activePage = page;
      sessionStorage.setItem("activePage", page);
    },

    async fetchOrderById(orderId) {
      this.loading = true;
      this._orderId = orderId; 
      try {
        const res = await getOrdersWithId(
          orderId,
        );
        
        this.order = res?.data
      } catch (err) {
        this.order = null
        console.error("Error fetching orders:", err);
      } finally {
        this.loading = false;
      }
    },

   async fetchOrders(userId, status, page = this.activePage) {
  this.loading = true;
  this._userId = userId; 
  this._status = status;
  this.orders = []; 
  try {
    const res = await getOrdersWithStatus(
      userId,
      page - 1, 
      5,
      "id,desc",
      status
    );
    this.allPages = res?.data?.totalPages;
    this.orders = (res?.data?.content || []).slice().sort((a, b) => {
      if (a.id != null && b.id != null && a.id !== b.id) {
        return b.id - a.id;
      }
      return new Date(b.orderDate) - new Date(a.orderDate);
    });
  } catch (err) {
    console.error("Error fetching orders:", err);
    this.orders = [];
  } finally {
    this.loading = false;
  }
},

    async refreshBadge(userId) {
      try {
        this.newOrdersCount = await getBuyerNewOrdersCount(userId);
      } catch (err) {
        console.error("Error fetching buyer new orders count:", err);
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
  },
});
