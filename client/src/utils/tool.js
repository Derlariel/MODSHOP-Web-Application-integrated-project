const handleFetch = async (url, options, errorMessages) => {
  try {
    const res = await fetch(url, options);
    if(res.status === 204) return null
    if (!res.ok) {
      throw { message: errorMessages, status: res.status };
    }
    return await res.json();
  } catch (err) {
    throw { message: errorMessages, status: err.status || 500 };
  }
};

const getProducts = async (url) => {
  const res = await fetch (url);
  if (!res.ok) throw new Error (res.status);
  if (res.status === 204) return null;

  return await res.json ();
};


const getProductsPage = async (url, params = {}) => {
  const query = new URLSearchParams (params).toString ();
  const fullUrl = query ? `${url}?${query}` : url;

  const res = await fetch (fullUrl);
  if (!res.ok) throw new Error (res.status);
  if (res.status === 204) return null;

  return await res.json ();
};


const getProductById = async (url, id) => {
  return handleFetch(
    `${url}/${id}`,
    { method: "GET" },
    "Error getting product by ID"
  );
};

const updateProductById = async (url, id, data) => {
  return handleFetch(
    `${url}/${id}`,
    {
      method: "PUT",
      headers: { "content-Type": "application/json" },
      body: JSON.stringify(data),
    },
    "Error updating product by ID"
  );
};

async function addProduct(url, data) {
  const response = await fetch(url, {
    method: 'POST',
    body: data,
   
  });

  if (!response.ok) throw new Error('Failed to add product');
  return await response.json();
}

const deleteProductById = async (url, id) => {
  return handleFetch(
    `${url}/${id}`,
    { method: "DELETE"},
    "Error deleting product by ID"
  )
}
export {
  addProduct,getProductById,getProducts, getProductsPage,updateProductById,deleteProductById
};
