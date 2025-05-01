CREATE DATABASE mobile_shop CHARACTER SET utf8mb4;
USE mobile_shop;

show variables like 'time_zone';
SET GLOBAL time_zone = '+00:00';
SET time_zone = '+00:00';

CREATE TABLE brand (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(30) NOT NULL,
    website_url VARCHAR(40),
    is_active BOOLEAN DEFAULT TRUE,
    country_of_origin VARCHAR(80),
    created_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

DELIMITER //
CREATE TRIGGER trim_brand_name BEFORE INSERT ON brand
FOR EACH ROW
BEGIN
    SET NEW.name = TRIM(NEW.name);
    SET NEW.website_url = TRIM(NEW.website_url);
    SET NEW.country_of_origin = TRIM(NEW.country_of_origin);
END//
DELIMITER ;

DELIMITER //
CREATE TRIGGER trim_brand_name_update BEFORE UPDATE ON brand
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
    screen_size_inch DECIMAL(4,1),
    storage_gb INT,
	color VARCHAR(30),
    quantity INT NOT NULL DEFAULT 1,
    created_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
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

INSERT INTO brand (id, name, website_url, is_active, country_of_origin, created_on, updated_on) VALUES
(1, 'Samsung', 'https://www.samsung.com', TRUE, 'South Korea', '1938-03-01 00:00:00', '2024-01-01 12:00:00'),
(2, 'Apple', 'https://www.apple.com', TRUE, 'United States', '1976-04-01 00:00:00', '2024-01-01 12:00:00'),
(3, 'Xiaomi', 'https://www.mi.com', TRUE, 'China', '2010-04-06 00:00:00', '2024-01-01 12:00:00'),
(4, 'Huawei', 'https://www.huawei.com', TRUE, 'China', '1987-09-15 00:00:00', '2024-01-01 12:00:00'),
(5, 'OnePlus', 'https://www.oneplus.com', TRUE, 'China', '2013-12-16 00:00:00', '2024-01-01 12:00:00'),
(6, 'Sony', 'https://www.sony.com', TRUE, 'Japan', '1946-05-07 00:00:00', '2024-01-01 12:00:00'),
(7, 'LG', 'https://www.lg.com', TRUE, 'South Korea', '1947-10-01 00:00:00', '2024-01-01 12:00:00'),
(8, 'Nokia', 'https://www.nokia.com', FALSE, 'Finland', '1865-05-12 00:00:00', '2022-01-01 12:00:00'),
(9, 'Motorola', 'https://www.motorola.com', FALSE, 'United States', '1928-09-25 00:00:00', '2022-01-01 12:00:00'),
(10, 'OPPO', 'https://www.oppo.com', TRUE, 'China', '2004-10-10 00:00:00', '2024-01-01 12:00:00'),
(11, 'Vivo', 'https://www.vivo.com', TRUE, 'China', '2009-11-01 00:00:00', '2024-01-01 12:00:00'),
(12, 'ASUS', 'https://www.asus.com', TRUE, 'Taiwan', '1989-04-02 00:00:00', '2024-01-01 12:00:00'),
(13, 'Google', 'https://store.google.com', TRUE, 'United States', '1998-09-04 00:00:00', '2024-01-01 12:00:00'),
(14, 'Realme', 'https://www.realme.com', TRUE, 'China', '2018-05-04 00:00:00', '2024-01-01 12:00:00'),
(15, 'BlackBerry', 'https://www.blackberry.com', TRUE, 'Canada', '1984-03-07 00:00:00', '2024-01-01 12:00:00'),
(16, 'HTC', 'https://www.htc.com', TRUE, 'Taiwan', '1997-05-15 00:00:00', '2024-01-01 12:00:00'),
(17, 'ZTE', 'https://www.zte.com', TRUE, 'China', '1985-03-01 00:00:00', '2024-01-01 12:00:00'),
(18, 'Lenovo', 'https://www.lenovo.com', TRUE, 'China', '1984-11-01 00:00:00', '2024-01-01 12:00:00'),
(19, 'Honor', 'https://www.hihonor.com', TRUE, 'China', '2013-12-16 00:00:00', '2024-01-01 12:00:00'),
(20, 'Nothing', 'https://nothing.tech', TRUE, 'United Kingdom', '2020-01-01 00:00:00', '2024-01-01 12:00:00');

INSERT INTO sale_item (id, brand_id, model, description, quantity, price, screen_size_inch, ram_gb, storage_gb, color, created_on, updated_on, rate) VALUES
(1, 2, 'iPhone 14 Pro Max', 'ไอโฟนเรือธงรุ่นล่าสุด มาพร้อม Dynamic Island จอใหญ่สุดในตระกูล กล้องระดับโปร', 5, 42900, 6.7, 6, 512, 'Space Black', '2022-09-16 10:00:00', '2024-01-01 12:00:00', 4.7),
(2, 2, 'iPhone 14', 'ไอโฟนรุ่นใหม่ล่าสุด รองรับ 5G เร็วแรง ถ่ายภาพสวยทุกสภาพแสง', 8, 29700, 6.1, 6, 256, 'Midnight', '2022-09-16 10:00:00', '2024-01-01 12:00:00', 4.3),
(3, 2, 'iPhone 13 Pro', 'ไอโฟนรุ่นโปร จอ ProMotion 120Hz กล้องระดับมืออาชีพ', 3, 33000, 6.1, 6, 256, 'Sierra Blue', '2021-09-24 10:00:00', '2023-12-01 12:00:00', 4.0),
(4, 2, 'iPhone SE 2022', 'iPhone SE 2022 มาพร้อมชิป A15 Bionic รองรับ 5G ดีไซน์คลาสสิก ราคาประหยัด', 15, 14190, 4.7, 4, 64, 'Starlight', '2022-03-18 10:00:00', '2024-01-01 12:00:00', 3.8),
(5, 2, 'iPhone 14 Plus', 'iPhone 14 Plus 128GB สี Starlight เครื่องศูนย์ไทย โมเดล TH แบต 100% มีกล่อง ครบประกันศูนย์ถึง พ.ย. 68 ส่งฟรี', 7, 29700, 6.7, 6, 256, 'Blue', '2022-10-07 10:00:00', '2024-01-01 12:00:00', 4.2),
(6, 1, 'Galaxy S23 Ultra', 'Samsung Galaxy S23 Ultra 512GB สีดำนิล สภาพนางฟ้า 99% ไร้รอย แถมเคสแท้ แบตอึดสุดๆ รองรับปากกา S-Pen', 1, 32900, 6.8, 12, 512, 'Phantom Black', '2023-02-17 10:00:00', '2024-01-01 12:00:00', 4.6),
(7, 2, 'iPhone 16 Pro Max', 'ไอโฟนรุ่นท็อปปี 2024 มาพร้อมชิป A18 Pro กล้อง 48MP x3 และจอ 6.9 นิ้ว Super Retina XDR', 10, 139900, 6.9, 8, 512, 'Desert Titanium', '2024-09-22 10:00:00', '2025-04-30 12:00:00', 4.9),
(8, 2, 'iPhone 16 Pro', 'รุ่นโปรขนาดกะทัดรัด จอ 6.1 นิ้ว กล้อง 5x Zoom ชิป A18 Pro รองรับ iOS 18', 12, 119900, 6.1, 8, 256, 'Graphite', '2024-09-22 10:00:00', '2025-04-30 12:00:00', 4.8),
(9, 1, 'Galaxy S25 Ultra', 'เรือธงจากซัมซุง กล้อง 200MP ชิป Snapdragon 8 Gen Elite จอ AMOLED 6.9 นิ้ว', 8, 129900, 6.9, 12, 512, 'Phantom Black', '2025-02-10 10:00:00', '2025-04-30 12:00:00', 4.7),
(10, 1, 'Galaxy S25', 'รุ่นมาตรฐานของ S25 ซีรีส์ จอ 6.1 นิ้ว กล้อง 50MP ชิป Snapdragon 8 Gen Elite', 15, 89900, 6.1, 8, 256, 'Phantom Silver', '2025-02-10 10:00:00', '2025-04-30 12:00:00', 4.4),
(11, 13, 'Pixel 9 Pro', 'มือถือจากกูเกิล กล้อง 50MP x3 ชิป Tensor G4 จอ OLED 6.7 นิ้ว รองรับ Android 15', 10, 89900, 6.7, 12, 512, 'Obsidian', '2024-10-15 10:00:00', '2025-04-30 12:00:00', 4.5),
(12, 13, 'Pixel 9a', 'มือถือราคาประหยัดจากกูเกิล ชิป Tensor G4 กล้อง 48MP แบต 5100mAh รองรับ Android 15', 20, 19900, 6.3, 8, 128, 'Charcoal', '2025-01-10 10:00:00', '2025-04-30 12:00:00', 4.0),
(13, 5, 'OnePlus 13', 'มือถือเรือธงจาก OnePlus ชิป Snapdragon 8 Gen 3 กล้อง 50MP x3 จอ AMOLED 6.8 นิ้ว', 10, 79900, 6.8, 12, 512, 'Emerald Green', '2025-01-20 10:00:00', '2025-04-30 12:00:00', 4.3),
(14, 9, 'Razr Ultra 2025', 'มือถือพับได้จาก Motorola จอใน 7 นิ้ว จอนอก 4 นิ้ว ชิป Snapdragon 8 Gen Elite กล้อง 50MP x3', 5, 45900, 7.0, 12, 512, 'Midnight Blue', '2025-05-15 10:00:00', '2025-04-30 12:00:00', 4.1),
(15, 9, 'Razr Plus 2025', 'มือถือพับได้รุ่นกลางจาก Motorola จอใน 6.9 นิ้ว จอนอก 3.6 นิ้ว ชิป Snapdragon 8 Gen Elite กล้อง 50MP x2', 7, 35900, 6.9, 8, 256, 'Blush Gold', '2025-05-15 10:00:00', '2025-04-30 12:00:00', 4.0),
(16, 9, 'Razr 2025', 'มือถือพับได้รุ่นเริ่มต้นจาก Motorola จอใน 6.7 นิ้ว จอนอก 3.2 นิ้ว ชิป Snapdragon 8 Gen Elite กล้อง 50MP', 10, 25900, 6.7, 8, 128, 'Sage Green', '2025-05-15 10:00:00', '2025-04-30 12:00:00', 3.9),
(17, 20, 'Nothing Phone 3a Pro', 'มือถือดีไซน์ล้ำจาก Nothing จอ AMOLED 6.7 นิ้ว ชิป Snapdragon 8 Gen 3 กล้อง 50MP x2', 12, 28900, 6.7, 12, 256, 'Transparent', '2025-03-01 10:00:00', '2025-04-30 12:00:00', 4.2),
(18, 20, 'Nothing Phone 3a', 'มือถือดีไซน์ล้ำจาก Nothing จอ AMOLED 6.5 นิ้ว ชิป Snapdragon 7 Gen 3 กล้อง 50MP', 15, 19900, 6.5, 8, 128, 'White', '2025-03-01 10:00:00', '2025-04-30 12:00:00', 4.0);
