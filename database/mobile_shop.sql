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
    updated_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_name_trimmed CHECK (name = RTRIM(LTRIM(name)))
);

DELIMITER //
CREATE TRIGGER trim_brand_name BEFORE INSERT ON brand
FOR EACH ROW
BEGIN
    SET NEW.name = TRIM(NEW.name);
END//
DELIMITER ;

DELIMITER //
CREATE TRIGGER trim_brand_name_update BEFORE UPDATE ON brand
FOR EACH ROW
BEGIN
    SET NEW.name = TRIM(NEW.name);
END//
DELIMITER ;

CREATE TABLE sale_item (
    id INT AUTO_INCREMENT PRIMARY KEY,
    model VARCHAR(60) NOT NULL,
    brand_id INT NOT NULL,
    description TEXT NOT NULL,
    price INT NOT NULL,
    ram_gb INT,
    screen_size_inch DECIMAL(4,1),
    storage_gb INT,
	color VARCHAR(30),
    quantity INT NOT NULL DEFAULT 1,
    created_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_on DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_model_trimmed CHECK (model = RTRIM(LTRIM(model))),
    CONSTRAINT chk_description_trimmed CHECK (description = RTRIM(LTRIM(description))),
    FOREIGN KEY (brand_id) REFERENCES brand(id)
);


DELIMITER //
CREATE TRIGGER trim_sale_item_on_insert BEFORE INSERT ON sale_item
FOR EACH ROW
BEGIN
    SET NEW.model = RTRIM(LTRIM(NEW.model));
    SET NEW.description = RTRIM(LTRIM(NEW.description));
END//
DELIMITER ;

DELIMITER //
CREATE TRIGGER trim_sale_item_on_update BEFORE UPDATE ON sale_item
FOR EACH ROW
BEGIN
    SET NEW.model = RTRIM(LTRIM(NEW.model));
    SET NEW.description = RTRIM(LTRIM(NEW.description));
END//
DELIMITER ;

CREATE INDEX idx_sale_item_created_on ON sale_item(created_on);
CREATE INDEX idx_brand_created_on ON brand(created_on);

INSERT INTO brand (id, name, website_url, is_active, country_of_origin) VALUES
(1, '     Samsung    ', 'https://www.samsung.com', TRUE, 'South Korea'),
(2, 'Apple    ', 'https://www.apple.com', TRUE, 'United States'),
(3, 'Xiaomi', 'https://www.mi.com', TRUE, 'China'),
(4, 'Huawei', 'https://www.huawei.com', TRUE, 'China'),
(5, 'OnePlus', 'https://www.oneplus.com', TRUE, 'China'),
(6, 'Sony', 'https://www.sony.com', TRUE, 'Japan'),
(7, 'LG', 'https://www.lg.com', TRUE, 'South Korea'),
(8, 'Nokia', 'https://www.nokia.com', FALSE, 'Finland'),
(9, 'Motorola', 'https://www.motorola.com', FALSE, 'United States'),
(10, 'OPPO', 'https://www.oppo.com', TRUE, 'China'),
(11, 'Vivo', 'https://www.vivo.com', TRUE, 'China'),
(12, 'ASUS', 'https://www.asus.com', TRUE, 'Taiwan'),
(13, 'Google', 'https://store.google.com', TRUE, 'United States'),
(14, 'Realme', 'https://www.realme.com', TRUE, 'China'),
(15, 'BlackBerry', 'https://www.blackberry.com', TRUE, 'Canada'),
(16, 'HTC', 'https://www.htc.com', TRUE, 'Taiwan'),
(17, 'ZTE', 'https://www.zte.com', TRUE, 'China'),
(18, 'Lenovo', 'https://www.lenovo.com', TRUE, 'China'),
(19, 'Honor', 'https://www.hihonor.com', TRUE, 'China'),
(20, 'Nothing', 'https://nothing.tech', TRUE, 'United Kingdom');

INSERT INTO sale_item (id, brand_id, model, description, quantity, price, screen_size_inch, ram_gb, storage_gb, color) VALUES
(1, 2, '    iPhone 14 Pro Max    ', '   ไอโฟนเรือธงรุ่นล่าสุด มาพร้อม Dynamic Island จอใหญ่สุดในตระกูล กล้องระดับโปร          ', 5, 42900, 6.7, 6, 512, 'Space Black'),
(2, 2, 'iPhone 14', '     ไอโฟนรุ่นใหม่ล่าสุด รองรับ 5G เร็วแรง ถ่ายภาพสวยทุกสภาพแสง    ', 8, 29700, 6.1, 6, 256, 'Midnight'),
(3, 2, 'iPhone 13 Pro', 'ไอโฟนรุ่นโปร จอ ProMotion 120Hz กล้องระดับมืออาชีพ', 3, 33000, 6.1, 6, 256, 'Sierra Blue'),
(7, 2, 'iPhone SE 2022', 'Budget-friendly model', 15, 14190, 4.7, 4, 64, 'Starlight'),
(8, 2, 'iPhone 14 Plus', 'iPhone 14 Plus 128GB สี Starlight เครื่องศูนย์ไทย โมเดล TH แบต 100% มีกล่อง ครบประกันศูนย์ถึง พ.ย. 68 ส่งฟรี', 7, 29700, 6.7, 6, 256, 'Blue'),
(16, 1, 'Galaxy S23 Ultra', '    Samsung Galaxy S23 Ultra 512GB สีดำนิล สภาพางฟ้า 99% ไร้รอย แถมเคสแท้ แบตอึดสุดๆ รองรับปากกา S-Pen', 1, 32900, 6.8, NULL, 512, NULL);
