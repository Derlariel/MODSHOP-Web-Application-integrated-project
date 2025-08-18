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
  let options;
  console.log("CALLED updateProductById >>>", url, id);

  if (data instanceof FormData) {
    // 📌 ถ้าเป็น FormData → ห้ามใส่ Content-Type เดี๋ยว browser ใส่ boundary ให้เอง
    options = {
      method: "PUT",
      body: data,
    };
  } else {
    // 📌 ถ้าเป็น JSON object ปกติ
    options = {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(data),
    };
  }

  return handleFetch(
    `${url}/${id}`,
    options,
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
