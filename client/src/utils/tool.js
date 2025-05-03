const handleFetch = async (url, options, errorMessages) => {
  try {
    const res = await fetch(url, options);
    if (!res.ok) {
      throw { message: errorMessages, status: res.status };
    }
    return await res.json();
  } catch (err) {
    throw { message: errorMessages, status: err.status || 500 };
  }
};

const getProducts = async (url) => {
  return handleFetch(url, { method: "GET" }, "Error getting products");
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


const addProduct = async (url,product) => {
  return handleFetch(
    url,
    {
      method:"POST",
      headers: { "content-Type": "application/json" },
      body: JSON.stringify(product),
    },
    "Error adding product"
  )
}

const deleteProductById = async (url, id) => {
  return handleFetch(
    `${url}/${id}`,
    { method: "DELETE"},
    "Error deleting product by ID"
  )
}
export {
  addProduct,getProductById,getProducts,updateProductById,deleteProductById
};
