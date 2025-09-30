const handleFetch = async (url, options, errorMessages) => {
  try {

    const requestOptions = {
      ...options,
      credentials: 'include', 
    };
    
    const res = await fetch(url, requestOptions);
    if (res.status === 204) return null;
    if (!res.ok) {
      throw { message: errorMessages, status: res.status };
    }
    return await res.json();
  } catch (err) {
    throw { message: errorMessages, status: err.status || 500 };
  }
};

const getProducts = async (url) => {
  const res = await fetch(url, {
    credentials: 'include', 
  });
  if (!res.ok) throw new Error(res.status);
  if (res.status === 204) return null;

  return await res.json();
};

const getProductsPage = async (url, params = {}) => {
  const cleanParams = {};

  Object.keys(params).forEach((key) => {
    const value = params[key];
    if (value !== null && value !== undefined && value !== "") {
      if (Array.isArray(value)) {
        if (value.length > 0) {
          cleanParams[key] = value;
        }
      } else {
        cleanParams[key] = value;
      }
    }
  });

  const query = new URLSearchParams(cleanParams).toString();
  const fullUrl = query ? `${url}?${query}` : url;

  const res = await fetch(fullUrl, {
    credentials: 'include',
  });
  if (!res.ok) throw new Error(res.status);
  if (res.status === 204) return null;

  return await res.json();
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
    options = {
      method: "PUT",
      body: data,
      credentials: 'include',
    };
  } else {
    options = {
      method: "PUT",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(data),
      credentials: 'include',
    };
  }

  return handleFetch(`${url}/${id}`, options, "Error updating product by ID");
};

async function addProduct(url, data) {
  const response = await fetch(url, {
    method: "POST",
    body: data,
    credentials: 'include',
  });

  if (!response.ok) throw new Error("Failed to add product");
  return await response.json();
}
const deleteProductById = async (url, id) => {
  return handleFetch(
    `${url}/${id}`,
    { method: "DELETE" },
    "Error deleting product by ID"
  );
};
export {
  addProduct,
  getProductById,
  getProducts,
  getProductsPage,
  updateProductById,
  deleteProductById,
};
