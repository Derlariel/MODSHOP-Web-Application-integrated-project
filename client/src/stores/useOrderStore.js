import { defineStore } from "pinia";
import { getOrders } from "@/api/orderAPI";

export const useOrderStore = defineStore("orderStore", {
  state: () => ({
    orders: [],
    loading: false,
  }),

  actions: {
    async fetchOrders(userId) {
      this.loading = true;
      try {
        const res = await getOrders(userId, 0, 100, "id,desc");
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
