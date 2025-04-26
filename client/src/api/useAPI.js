import axios from "axios";

const BASE_URL = ''

export async function fetchProduct() {
    try{
        const response = await axios.get(`${BASE_URL}/โปดาก`)
        return response.data
    }catch(err){
        console.error("ไอเหี้ย ดึง products ล้มเหลว",err)
        throw err
    }
}

export async function fetchProductById(id) {
    try{
        const response = await axios.get(`${BASE_URL}/โปดาก/${id}`)
        return response.data
    }catch(err){
        console.error("ไอเหี้ย ดึง product 1 ชิ้นล้อมเหลว",err)
        throw err
    }
}