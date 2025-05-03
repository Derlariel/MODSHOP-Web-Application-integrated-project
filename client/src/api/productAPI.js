const BASE_URL = import.meta.env.VITE_BASE_URL;

async function handleFetch(url, errorMessage) {
  try {
    const res = await fetch(url);
    if (!res.ok) {
      throw new Error(`${errorMessage} (Status: ${res.status})`);
    }
    return await res.json();
  } catch (err) {
    console.error(`❌ ${errorMessage}:`, err);
    throw err;
  }
}

export async function fetchProduct() {
  return await handleFetch(`${BASE_URL}/v1/sale-items`, "Failed to fetch products");
}

export async function fetchProductById(id) {
  return await handleFetch(`${BASE_URL}/v1/sale-items/${id}`, `Failed to fetch product with ID: ${id}`);
}
