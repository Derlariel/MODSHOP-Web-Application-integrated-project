const BASE_URL = import.meta.env.VITE_BASE_URL;

import { request } from "../middleware/interception.js"

export async function createOrder(orders) {
  return await request("/v2/orders", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(orders),
  });
}

export async function getOrders(userId, page = 0, size = 10, sort = "orderDate,desc") {
  const query = `?page=${page}&size=${size}&sort=${sort}`;
  return await request(`/v2/users/${userId}/orders${query}`, {
    method: "GET",
  });
}


export async function getOrdersWithId(orderId) {
  return await request(`/v2/orders/${orderId}`, {
    method: "GET",
  });
}


export async function getOrdersWithStatus(userId, page = 0, size = 10, sort = "orderDate,desc", status) {
  const query = `?page=${page}&size=${size}&sort=${sort}`;
  return await request(`/v2/users/${userId}/orders/status/${status}${query}`, {
    method: "GET",
  });
}

export async function getSellerOrders(
  sellerId,
  { tab = "all", page = 0, size = 10, sort = "orderDate,desc" } = {}
) {
  const query = `?tab=${encodeURIComponent(tab)}&page=${page}&size=${size}&sort=${encodeURIComponent(
    sort
  )}`;
  return await request(`/v2/sellers/${sellerId}/orders${query}`, {
    method: "GET",
  });
}

export async function getSellerNewOrdersCount(sellerId) {
  const res = await getSellerOrders(sellerId, {
    tab: "new",
    page: 0,
    size: 1,
    sort: "id,desc",
  });
  return res?.data?.totalElements ?? 0;
}

export async function getBuyerNewOrdersCount(userId) {
  const res = await getOrdersWithStatus(userId, 0, 1, "id,desc", "NEW");
  return res?.data?.totalElements ?? 0;
}

export async function getSellerNamesForUser(userId) {
  return await request(`/v2/users/${userId}/sellers/names`, {
    method: "GET",
  });
}

export async function getBuyerNamesForSeller(sellerId) {
  return await request(`/v2/sellers/${sellerId}/buyers/names`, {
    method: "GET",
  });
}
