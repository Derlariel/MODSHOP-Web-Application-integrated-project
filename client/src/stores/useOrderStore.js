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
        const res = await getOrders(userId);
        console.log("orders response:", res);
        this.orders = (res.content || []).sort(
          (a, b) => new Date(b.orderDate) - new Date(a.orderDate)
        );
      } catch (err) {
        console.error("Error fetching orders:", err);
      } finally {
        this.loading = false;
      }
    },
  },
});
