import axios from "axios"

const BASE_URL = import.meta.env.VITE_BASE_URL

export async function fetchProduct() {
  try {
    const response = await axios.get(`${BASE_URL}/sale-items`)
    return response.data
  } catch (err) {
    console.error("❌ Failed to fetch products:", err)
    throw err
  }
}

export async function fetchProductById(id) {
  try {
    const response = await axios.get(`${BASE_URL}/sale-items/${id}`)
    return response.data
  } catch (err) {
    console.error(`❌ Failed to fetch product with ID: ${id}`, err)
    throw err
  }
}
