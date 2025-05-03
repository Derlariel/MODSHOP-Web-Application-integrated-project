import axios from 'axios';

const getProducts = async url => {
  try {
    const response = await axios.get (url);
    return response.data;
  } catch (err) {
    throw new Error ('Error getting items: ' + err.message);
  }
};

const getProductById = async (url, productId) => {
  try {
    const response = await axios.get (`${url}/${productId}`);
    return response.data;
  } catch (err) {
    throw new Error ('Error getting by item: ' + err.message);
  }
};

const updateProductById = async (url, productId, data) => {
  try {
    const response = await axios.put (`${url}/${productId}`, data);
    return response.data;
  } catch (err) {
    throw new Error ('Error add item' + err.message);
  }
};

const addProduct = async (url, product) => {
  try {
    const response = await axios.post (`${url}`, product);
    return response.data;
  } catch (err) {
    throw new Error ('Error add item' + err.message);
  }
};

const deleteProductById = async (url, productId) => {
     try {
    const response = await axios.delete (`${url}/${productId}`);
    return response.data;
  } catch (err) {
    throw new Error ('Error add item' + err.message);
  }
}



export { addProduct, getProductById, getProducts, updateProductById, deleteProductById }