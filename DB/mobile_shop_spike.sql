CREATE DATABASE mobile_shop_spike 
CHARACTER SET utf8mb4;

USE mobile_shop_spike;

CREATE TABLE products (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(60) NOT NULL,
    description VARCHAR(400),
    stock INT(10) NOT NULL,
    price INT(10) NOT NULL,
    brand VARCHAR(50) NOT NULL,
    createdOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updatedOn DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

INSERT INTO products (name, description,stock, price, brand) VALUES
('iPhone 16 Pro Max', 'iPhone 16 Pro Max ดีไซน์ไทเทเนียมที่ทนทานตัวควบคุมกล้อง Dolby Vision ระดับ 4K ที120 fps ชิป A18 Pro และสร้างมาเพื่อ Apple Intelligence.',
	2, 41700, 'Apple'),
('iPhone 16 Pro', 'iPhone 16 Pro พร้อมหน้าจอ Super Retina XDR 6.1 นิ้ว ชิป A18 Pro กล้องระบบ 3 ตัวพร้อม Night Mode', 5, 39900, 'Apple'),
('iPhone 16 Plus', 'iPhone 16 Plus หน้าจอขนาดใหญ่ 6.7 นิ้ว พร้อมชิป A16 Bionic กล้องคู่ระบบใหม่ แบตเตอรี่ใช้งานได้นานขึ้น', 8, 34900, 'Apple'),
('iPhone 16', 'iPhone 16 พื้นฐาน หน้าจอ 6.1 นิ้ว ชิป A16 Bionic กล้องคู่ ระบบปฏิบัติการ iOS ล่าสุด', 10, 29900, 'Apple');
