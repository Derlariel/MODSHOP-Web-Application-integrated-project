CREATE DATABASE mobile_shop CHARACTER SET utf8mb4;
USE mobile_shop;

show variables like 'time_zone';
SET GLOBAL time_zone = '+07:00';
SET time_zone = '+07:00';

CREATE TABLE brand (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    website_url VARCHAR(40),
    is_active TINYINT DEFAULT 1,
    country_of_origin VARCHAR(80),
    created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

DELIMITER //
CREATE TRIGGER trim_brand_on_insert BEFORE INSERT ON brand
FOR EACH ROW
BEGIN
    SET NEW.name = TRIM(NEW.name);
    SET NEW.website_url = TRIM(NEW.website_url);
    SET NEW.country_of_origin = TRIM(NEW.country_of_origin);
END//
DELIMITER ;

DELIMITER //
CREATE TRIGGER trim_brand_on_update BEFORE UPDATE ON brand
FOR EACH ROW
BEGIN
    SET NEW.name = TRIM(NEW.name);
    SET NEW.website_url = TRIM(NEW.website_url);
    SET NEW.country_of_origin = TRIM(NEW.country_of_origin);
END//
DELIMITER ;

CREATE TABLE sale_item (
    id INT AUTO_INCREMENT PRIMARY KEY,
    model VARCHAR(60) NOT NULL,
    brand_id INT NOT NULL,
    description TEXT NOT NULL,
    price INT NOT NULL,
    rate DECIMAL(2,1) NULL,
    ram_gb INT,
    screen_size_inch DECIMAL(5,2),
    storage_gb INT,
	color VARCHAR(30),
    quantity INT NOT NULL DEFAULT 1,
    created_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_rate CHECK (rate >= 0.0 AND rate <= 5.0),
    FOREIGN KEY (brand_id) REFERENCES brand(id)
);

DELIMITER //
CREATE TRIGGER trim_sale_item_on_insert BEFORE INSERT ON sale_item
FOR EACH ROW
BEGIN
    SET NEW.model = TRIM(NEW.model);
    SET NEW.description = TRIM(NEW.description);
    SET NEW.color = TRIM(NEW.color);
END//
DELIMITER ;

DELIMITER //
CREATE TRIGGER trim_sale_item_on_update BEFORE UPDATE ON sale_item
FOR EACH ROW
BEGIN
    SET NEW.model = TRIM(NEW.model);
    SET NEW.description = TRIM(NEW.description);
    SET NEW.color = TRIM(NEW.color);
END//
DELIMITER ;

CREATE INDEX idx_sale_item_created_on ON sale_item(created_on);
CREATE INDEX idx_brand_created_on ON brand(created_on);

INSERT INTO brand (id, name, website_url, is_active, country_of_origin) VALUES
(1, 'Samsung', 'https://www.samsung.com', 1, 'South Korea'),
(2, 'Apple', 'https://www.apple.com', 1, 'United States'),
(3, 'Xiaomi', 'https://www.mi.com', 1, 'China'),
(4, 'Huawei', 'https://www.huawei.com', 1, 'China'),
(5, 'OnePlus', 'https://www.oneplus.com', 1, 'China'),
(6, 'Sony', 'https://www.sony.com', 1, 'Japan'),
(7, 'LG', 'https://www.lg.com', 1, 'South Korea'),
(8, 'Nokia', 'https://www.nokia.com', 0, 'Finland'),
(9, 'Motorola', 'https://www.motorola.com', 0, 'United States'),
(10, 'OPPO', 'https://www.oppo.com', 1, 'China'),
(11, 'Vivo', 'https://www.vivo.com', 1, 'China'),
(12, 'ASUS', 'https://www.asus.com', 1, 'Taiwan'),
(13, 'Google', 'https://store.google.com', 1, 'United States'),
(14, 'Realme', 'https://www.realme.com', 1, 'China'),
(15, 'BlackBerry', 'https://www.blackberry.com', 1, 'Canada'),
(16, 'HTC', 'https://www.htc.com', 1, 'Taiwan'),
(17, 'ZTE', 'https://www.zte.com', 1, 'China'),
(18, 'Lenovo', 'https://www.lenovo.com', 1, 'China'),
(19, 'Honor', 'https://www.hihonor.com', 1, 'China'),
(20, 'Nothing', 'https://nothing.tech', 1, 'United Kingdom');

INSERT INTO sale_item (id, brand_id, model, description, quantity, price, screen_size_inch, ram_gb, storage_gb, color, created_on, updated_on, rate) VALUES
-- Apple products (brand_id = 2)
(1, 2, 'iPhone 14 Pro Max', 'ไอโฟนเรือธงรุ่นล่าสุด มาพร้อม Dynamic Island จอใหญ่สุดในตระกูล กล้องระดับโปร', 5, 42900, 6.7, 6, 512, 'Space Black', '2020-09-15 10:30:00', '2024-01-10 14:25:00', 4.8),
(2, 2, 'iPhone 14', 'ไอโฟนรุ่นใหม่ล่าสุด รองรับ 5G เร็วแรง ถ่ายภาพสวยทุกสภาพแสง', 8, 29700, 6.1, 6, 256, 'Midnight', '2020-09-16 11:15:00', '2024-01-05 09:40:00', 4.5),
(3, 2, 'iPhone 13 Pro', 'ไอโฟนรุ่นโปร จอ ProMotion 120Hz กล้องระดับมืออาชีพ', 3, 33000, 6.1, 6, 256, 'Sierra Blue', '2020-09-20 14:20:00', '2023-12-15 16:30:00', 4.6),
(4, 2, 'iPhone 13', 'Previous gen base model', 10, 23100, 6.1, 4, 128, 'Pink', '2020-09-25 09:45:00', '2023-11-20 11:15:00', 4.2),
(5, 2, 'iPhone 12 Pro Max', '2020 flagship model', 4, 29700, 6.7, 6, 256, 'Pacific Blue', '2020-10-05 13:10:00', '2023-10-05 10:20:00', 4.3),
(6, 2, 'iPhone 12', '2020 base model', 6, 19800, 6.1, 4, 128, 'Purple', '2020-10-10 10:30:00', '2023-09-15 14:45:00', 4.1),
(7, 2, 'iPhone SE 2022', 'Budget-friendly model', 15, 14190, 4.7, 4, 64, 'Starlight', '2020-10-15 08:20:00', '2023-12-10 15:10:00', 3.9),
(8, 2, 'iPhone 14 Plus', 'iPhone 14 Plus 128GB สี Starlight เครื่องศูนย์ไทย โมเดล TH แบต 100% มีกล่องครบ ประกันศูนย์ถึง พ.ย. 68 ส่งฟรี', 7, 29700, 6.7, 6, 256, 'Blue', '2020-10-20 12:40:00', '2024-01-08 13:50:00', 4.4),
(9, 2, 'iPhone 13 mini', 'Compact previous gen', 5, 19800, 5.4, 4, 128, 'Green', '2020-10-25 15:25:00', '2023-11-25 09:30:00', 4.0),
(10, 2, 'iPhone 12 mini', 'Compact 2020 model', 4, 16500, 5.4, 4, 64, 'Red', '2020-10-30 11:50:00', '2023-10-20 14:15:00', 3.8),

-- Samsung products (brand_id = 1)
(16, 1, 'Galaxy S23 Ultra', 'Samsung Galaxy S23 Ultra 512GB สีดำปีศาจ สภาพนางฟ้า 99% ไร้รอย แถมเคสแท้ แบตอึดสุดๆ รองรับปากกา S-Pen อุปกรณ์ครบกล่อง ประกันศูนย์เหลือ 6 เดือน ส่งฟรี', 6, 39600, 6.8, NULL, 512, NULL, '2021-01-10 10:15:00', '2024-01-15 16:20:00', 4.7),
(17, 1, 'Galaxy S23+', 'Premium flagship model', 8, 33000, 6.6, 8, 256, 'Cream', '2021-01-15 09:30:00', '2024-01-12 11:45:00', 4.6),
(18, 1, 'Galaxy Z Fold4', 'สมาร์ทโฟนพับได้สุดล้ำ จอใหญ่เท่าแท็บเล็ต ทำงานได้หลากหลาย', 3, 59400, 7.6, 12, 256, 'Phantom Green', '2021-01-20 14:40:00', '2023-12-18 10:30:00', 4.5),
(19, 1, 'Galaxy Z Flip4', 'Compact foldable', 5, 33000, 6.7, 8, 128, 'Bora Purple', '2021-01-25 11:25:00', '2023-12-10 14:15:00', 4.3),
(20, 1, 'Galaxy A53 5G', 'มือถือ 5G สเปคดี กล้องเทพ แบตอึด คุ้มค่าน่าใช้', 12, 14850, 6.5, 6, 128, 'Awesome Blue', '2021-02-01 10:10:00', '2023-11-20 09:40:00', 4.0),
(21, 1, 'Galaxy A33 5G', 'Budget 5G phone', 15, 11550, 6.4, 6, 128, 'Awesome White', '2021-02-05 13:20:00', '2023-11-15 16:25:00', 3.8),
(22, 1, 'Galaxy S22', 'เรือธงตัวท็อปจาก Samsung พร้อม S Pen ในตัว กล้อง 200MP ซูมไกลสุด 100x', 7, 26400, 6.1, 8, 128, 'Pink Gold', '2021-02-10 09:45:00', '2023-10-25 14:10:00', 4.4),
(23, 1, 'Galaxy M53', 'Mid-range performance', 9, 14850, 6.7, 6, 128, 'Green', '2021-02-15 11:30:00', '2023-11-30 10:20:00', 3.9),
(24, 1, 'Galaxy A73 5G', 'Premium mid-range', 6, 16500, 6.7, 8, 256, 'Gray', '2021-02-20 14:15:00', '2023-12-05 13:45:00', 4.1),
(25, 1, 'Galaxy S21 FE', 'Fan Edition model', 8, 19800, 6.4, 6, 128, 'Olive', '2021-02-25 10:50:00', '2023-10-15 15:30:00', 4.2),

-- Xiaomi products (brand_id = 3)
(31, 3, '13 Pro', 'เรือธงสเปคแรงจาก Xiaomi กล้องไลก้า ชาร์จไว 120W', 8, 33000, 6.73, 12, 256, 'Black', '2021-03-05 11:20:00', '2024-01-08 14:40:00', 4.4),
(32, 3, '13T Pro', 'Xiaomi 13T Pro 12/512GB สี Meadow Green ชิป Dimensity 9200+ เร็วแรง กล้อง Leica ถ่ายรูปสวยขั้นเทพ มีที่ชาร์จ 120W ครบกล่อง จัดส่งฟรีทั่วประเทศ', 6, 23100, NULL, 12, NULL, 'Alpine Blue', '2021-03-10 13:45:00', '2024-01-12 10:15:00', 4.3),
(33, 3, 'POCO F5', 'มือถือสเปคเทพ เน้นเล่นเกม จอ 120Hz ราคาคุ้มค่า', 10, 13200, 6.67, 8, 256, 'Carbon Black', '2021-03-15 10:30:00', '2023-12-20 16:25:00', 4.2),
(34, 3, 'Redmi Note 12 Pro', 'กล้องคมชัด 108MP แบตอึด ชาร์จเร็ว ราคาโดนใจ', 15, 9900, 6.67, 8, 128, 'Sky Blue', '2021-03-20 09:15:00', '2023-11-25 11:40:00', 4.0),
(35, 3, '12T Pro', 'Previous flagship', 5, 21450, 6.67, 8, 256, 'Cosmic Black', '2021-03-25 14:20:00', '2023-10-30 14:50:00', 4.1),
(36, 3, 'POCO X5 Pro', 'Mid-range performer', 12, 9900, 6.67, 8, 128, 'Yellow', '2021-04-01 11:10:00', '2023-12-15 09:30:00', 3.9),
(37, 3, 'Redmi 12C', 'Budget friendly', 20, 5940, 6.71, 4, 64, 'Ocean Blue', '2021-04-05 08:45:00', '2023-11-10 15:20:00', 3.7),
(38, 3, '12 Lite', 'Slim mid-range', 8, 13200, 6.55, 8, 128, 'Lite Pink', '2021-04-10 10:25:00', '2023-10-20 10:45:00', 3.8),
(39, 3, 'POCO M5', 'Budget gaming', 14, 7590, 6.58, 6, 128, 'Power Black', '2021-04-15 13:30:00', '2023-11-15 14:15:00', 3.9),
(40, 3, 'Redmi Note 11', 'Previous gen mid-range', 10, 8250, 6.43, 6, 128, 'Star Blue', '2021-04-20 09:50:00', '2023-10-10 11:30:00', 3.8),

-- Huawei products (brand_id = 4)
(46, 4, 'P60 Pro', 'กล้องเรือธงระดับเทพ เซ็นเซอร์ใหญ่พิเศษ ถ่ายภาพกลางคืนสวยเยี่ยม', 5, 36300, 6.67, 12, 256, 'Rococo Pearl', '2021-05-05 14:15:00', '2024-01-05 15:45:00', 4.5),
(47, 4, 'Mate 50 Pro', 'เรือธงตระกูล Mate จอ OLED คมชัด ดีไซน์พรีเมียม', 4, 42900, 6.74, 8, 256, 'Silver Black', '2021-05-10 11:40:00', '2023-12-20 10:30:00', 4.4),
(48, 4, 'nova 11 Pro', 'สมาร์ทโฟนดีไซน์สวย กล้องหน้าคู่ เน้นเซลฟี่ ชาร์จไว', 8, 19800, 6.78, 8, 256, 'Green', '2021-05-15 10:20:00', '2023-12-15 14:25:00', 4.1),
(49, 4, 'P50 Pro', 'Previous flagship', 6, 29700, 6.6, 8, 256, 'Cocoa Gold', '2021-05-20 09:30:00', '2023-11-25 16:10:00', 4.2),
(50, 4, 'nova 10', 'Stylish mid-range', 10, 16500, 6.67, 8, 128, 'Starry Silver', '2021-05-25 13:45:00', '2023-11-30 11:20:00', 4.0),
(51, 4, 'Mate X3', 'Premium foldable', 3, 66000, 7.85, 12, 512, 'Feather Gold', '2021-06-01 15:10:00', '2024-01-10 13:30:00', 4.6),
(52, 4, 'nova 9', 'Previous mid-range', 12, 13200, 6.57, 8, 128, 'Starry Blue', '2021-06-05 10:50:00', '2023-10-25 09:45:00', 3.9),
(53, 4, 'P50 Pocket', 'Foldable fashion', 4, 46200, 6.9, 8, 256, 'Premium Gold', '2021-06-10 14:30:00', '2023-12-05 15:15:00', 4.3),
(54, 4, 'nova Y70', 'Budget friendly', 15, 9900, 6.75, 4, 128, 'Crystal Blue', '2021-06-15 08:40:00', '2023-11-20 10:50:00', 3.7),
(55, 4, 'Mate 40 Pro', 'Classic flagship', 5, 26400, 6.76, 8, 256, 'Mystic Silver', '2021-06-20 11:15:00', '2023-10-15 14:40:00', 4.4),

-- ASUS products (brand_id = 12)
(61, 12, 'ROG Phone 7', 'สมาร์ทโฟนเกมมิ่งสเปคโหด จอ 165Hz เสียงสเตอริโอคู่ แบตอึด', 4, 33000, 6.78, 16, 512, 'Phantom Black', '2021-07-05 10:45:00', '2024-01-08 16:30:00', 4.7),
(62, 12, 'ROG Phone 6D', 'เกมมิ่งโฟนพลังแรง CPU Dimensity ระบายความร้อนเยี่ยม', 5, 29700, 6.78, 16, 256, 'Space Gray', '2021-07-10 13:20:00', '2023-12-18 11:45:00', 4.6),
(63, 12, 'Zenfone 9', 'มือถือกะทัดรัด สเปคแรง กล้องกันสั่น ใช้ง่ายมือเดียว', 8, 23100, 5.9, 8, 128, 'Midnight Black', '2021-07-15 09:30:00', '2023-11-28 14:20:00', 4.3),
(64, 12, 'ROG Phone 6', 'Previous gaming flagship', 6, 29700, 6.78, 12, 256, 'Storm White', '2021-07-20 11:15:00', '2023-12-10 10:30:00', 4.5),
(65, 12, 'Zenfone 8', 'Previous compact flagship', 7, 19800, 5.9, 8, 128, 'Obsidian Black', '2021-07-25 14:40:00', '2023-10-25 15:10:00', 4.2),
(66, 12, 'ROG Phone 5s', 'Gaming performance', 5, 26400, 6.78, 12, 256, 'Phantom Black', '2021-08-01 10:20:00', '2023-11-15 13:45:00', 4.4),
(67, 12, 'Zenfone 8 Flip', 'Flip camera flagship', 4, 26400, 6.67, 8, 256, 'Galactic Black', '2021-08-05 13:30:00', '2023-10-30 09:50:00', 4.1),
(68, 12, 'ROG Phone 5', 'Classic gaming phone', 6, 23100, 6.78, 12, 256, 'Storm White', '2021-08-10 09:45:00', '2023-10-20 14:15:00', 4.3),
(69, 12, 'Zenfone 7', 'Flip camera classic', 5, 19800, 6.67, 8, 128, 'Aurora Black', '2021-08-15 11:50:00', '2023-09-25 10:40:00', 4.0),
(70, 12, 'ROG Phone 3', 'Legacy gaming phone', 3, 16500, 6.59, 12, 256, 'Black Glare', '2021-08-20 14:25:00', '2023-09-15 16:20:00', 3.9),

-- OPPO products (brand_id = 10)
(76, 10, 'Find X6 Pro', 'กล้องเทพระดับมืออาชีพ ชิป Snapdragon 8 Gen 2 ชาร์จไว 100W', 5, 33000, 6.82, 12, 256, 'Cosmos Black', '2021-09-05 10:30:00', '2024-01-05 14:50:00', 4.5),
(77, 10, 'Reno9 Pro+', 'OPPO Reno9 Pro+ 5G 256GB สี Glossy Purple สวยสะดุดตา ใช้งานลื่นสุดๆ แบต 4700 mAh รองรับชาร์จไว ครบกล่อง + ใบเสร็จศูนย์ ส่งฟรี Flash Express', 8, 23100, 6.7, 12, 256, 'Eternal Gold', '2021-09-10 13:15:00', '2023-12-20 11:30:00', 4.3),
(78, 10, 'Find N2 Flip', 'สมาร์ทโฟนพับได้สุดหรู จอนอกใหญ่พิเศษ กล้องคู่คมชัด', 4, 33000, 6.8, 8, 256, 'Astral Black', '2021-09-15 14:40:00', '2024-01-08 15:25:00', 4.4),
(79, 10, 'Reno8 Pro', 'ดีไซน์บางเบา กล้องคมชัด ชาร์จเร็วสุด ระบบเสียงดี', 10, 19800, 6.7, 8, 256, 'Glazed Green', '2021-09-20 09:20:00', '2023-11-25 10:15:00', 4.2),
(80, 10, 'Find X5 Pro', 'Previous flagship', 6, 29700, 6.7, 12, 256, 'Ceramic White', '2021-09-25 11:45:00', '2023-12-15 14:30:00', 4.3),
(81, 10, 'A78', 'Mid-range performer', 15, 9900, 6.56, 8, 128, 'Glowing Black', '2021-09-30 08:30:00', '2023-11-20 09:40:00', 3.9),
(82, 10, 'Reno7', 'Style focused mid-range', 12, 13200, 6.43, 8, 128, 'Startrails Blue', '2021-10-05 10:10:00', '2023-10-25 11:20:00', 4.0),
(83, 10, 'Find X5 Lite', 'Previous gen lite', 8, 14850, 6.43, 8, 128, 'Starry Black', '2021-10-10 13:25:00', '2023-11-15 15:10:00', 3.8),
(84, 10, 'A77', 'Budget friendly', 20, 8250, 6.56, 6, 128, 'Ocean Blue', '2021-10-15 09:50:00', '2023-10-30 10:45:00', 3.7),
(85, 10, 'Reno6 Pro', 'Classic premium', 7, 16500, 6.55, 12, 256, 'Arctic Blue', '2021-10-20 14:15:00', '2023-10-20 13:30:00', 4.1);