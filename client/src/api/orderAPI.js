const BASE_URL = import.meta.env.VITE_BASE_URL;

async function request(path, options = {}) {
  const headers = options.headers ? { ...options.headers } : {};

  const requestOptions = {
    ...options,
    headers,
    credentials: "include",
  };

  const res = await fetch(`${BASE_URL}${path}`, requestOptions);
  let data = null;
  try {
    data = await res.json();
  } catch (e) {
  }
  if (!res.ok) {
    const msg = (data && (data.message || data.error)) || `Request failed (${res.status})`;
    throw new Error(msg);
  }
  return data;
}

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


export async function getOrdersWithStatus(userId, page = 0, size = 10, sort = "orderDate,desc", status) {
  const query = `?page=${page}&size=${size}&sort=${sort}`;
  return await request(`/v2/users/${userId}/orders/status/${status}${query}`, {
    method: "GET",
  });
}
