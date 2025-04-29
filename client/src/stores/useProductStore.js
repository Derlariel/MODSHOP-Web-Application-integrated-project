import { defineStore } from "pinia";
import { addProduct, getProductById } from "@/utils/tool.js";
import { updateProductById } from "../utils/tool";

export const useProductStore = defineStore("product", {
  state: () => ({
    products: [
      {
        id: 1,
        name: "iPhone 16 Pro Max",
        description:
          "iPhone 16 Pro Max ดีไซน์ไทเทเนียมที่ทนทาน ตัวควบคุมกล้อง Dolby Vision ระดับ 4K ที่ 120 fps ชิป A18 Pro และสร้างมาเพื่อ Apple Intelligence.",
        stock: 2,
        price: 42900,
        brand: "Apple",
        createdOn: "2024-04-19 01:00:00",
        updatedOn: "2024-04-19 01:00:00",
      },
      {
        id: 2,
        name: "Samsung Galaxy S24 Ultra",
        description:
          "หน้าจอ 6.8 นิ้ว QHD+ 120Hz กล้อง 200MP ซูม 100x ชิป Snapdragon 8 Gen 3 แบต 5000mAh.",
        stock: 5,
        price: 38900,
        brand: "Samsung",
        createdOn: "2024-04-20 10:30:00",
        updatedOn: "2024-04-20 10:30:00",
      },
      {
        id: 3,
        name: "Google Pixel 8 Pro",
        description:
          "ชิป Tensor G3 พร้อมกล้อง AI อัจฉริยะ หน้าจอ OLED 6.7 นิ้ว LTPO 120Hz รองรับ Magic Eraser.",
        stock: 3,
        price: 35900,
        brand: "Google",
        createdOn: "2024-04-21 09:15:00",
        updatedOn: "2024-04-21 09:15:00",
      },
      {
        id: 4,
        name: "Xiaomi 14 Ultra",
        description:
          "กล้อง Leica สุดเทพ ถ่าย RAW ได้ ชิป Snapdragon 8 Gen 3 หน้าจอ AMOLED 120Hz.",
        stock: 10,
        price: 27900,
        brand: "Xiaomi",
        createdOn: "2024-04-22 08:00:00",
        updatedOn: "2024-04-22 08:00:00",
      },
      {
        id: 5,
        name: "OnePlus 12",
        description:
          "หน้าจอ AMOLED 120Hz ชิป Snapdragon 8 Gen 3 ระบบระบายความร้อนใหม่ แบตอึดจัด.",
        stock: 7,
        price: 24900,
        brand: "OnePlus",
        createdOn: "2024-04-22 12:45:00",
        updatedOn: "2024-04-22 12:45:00",
      },
      {
        id: 6,
        name: "ASUS ROG Phone 8",
        description:
          "มือถือเกมเมอร์ตัวพ่อ จอ AMOLED 165Hz ชิป Snapdragon 8 Gen 3 ลำโพงคู่ x-axis.",
        stock: 4,
        price: 31900,
        brand: "ASUS",
        createdOn: "2024-04-23 14:20:00",
        updatedOn: "2024-04-23 14:20:00",
      },
      {
        id: 7,
        name: "Nothing Phone (2)",
        description:
          "ดีไซน์ใสโคตรเท่ ชิป Snapdragon 8+ Gen 1 กล้องคู่ 50MP LED Glyph Interface.",
        stock: 8,
        price: 19900,
        brand: "Nothing",
        createdOn: "2024-04-23 16:00:00",
        updatedOn: "2024-04-23 16:00:00",
      },
      {
        id: 8,
        name: "Sony Xperia 1 V",
        description:
          "จอ 4K OLED 120Hz กล้องถ่ายวิดีโอ Cinematic ระบบเสียง Hi-Res สุดเทพ.",
        stock: 3,
        price: 36900,
        brand: "Sony",
        createdOn: "2024-04-23 17:30:00",
        updatedOn: "2024-04-23 17:30:00",
      },
      {
        id: 9,
        name: "Realme GT5 Pro",
        description:
          "Snapdragon 8 Gen 3 พร้อม RAM 16GB จอ 144Hz กล้อง IMX890 ซูม 120x.",
        stock: 9,
        price: 18900,
        brand: "Realme",
        createdOn: "2024-04-24 09:00:00",
        updatedOn: "2024-04-24 09:00:00",
      },
      {
        id: 10,
        name: "Motorola Edge 50 Ultra",
        description:
          "Snapdragon 8s Gen 3 จอ pOLED 144Hz กล้อง 50MP ซูมไกลสุดขอบจักรวาล.",
        stock: 6,
        price: 22900,
        brand: "Motorola",
        createdOn: "2024-04-24 10:00:00",
        updatedOn: "2024-04-24 10:00:00",
      },
    ],
  }),
  getters: {
    allProducts: (state) => state.products,
    getProductById: (state) => {
      return (id) => state.products.find((p) => p.id === id);
    },
  },

  actions: {
    addProduct(product) {
      const newId = this.products.length
        ? Math.max(...this.products.map((p) => p.id)) + 1
        : 1;
      this.products.push({ ...product, id: newId });
    },
    updateProduct(updatedProduct){
        const index = this.products.findIndex(p => p.id === updatedProduct.id)
        if(index != -1){
            this.products[index] = {...this.products[index],...updatedProduct}
        }
    },
    deleteProduct(id){
        this.products = this.products.filter(p => p.id !== id)
    }
  },
});
