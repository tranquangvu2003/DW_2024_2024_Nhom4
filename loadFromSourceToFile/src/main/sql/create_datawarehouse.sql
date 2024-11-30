CREATE DATABASE IF NOT EXISTS db_datawarehouse;
USE db_datawarehouse;

ALTER DATABASE db_datawarehouse CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Table: dim_manufacturers
CREATE TABLE IF NOT EXISTS dim_manufacturers (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    natural_key INT UNSIGNED NOT NULL,
    manufacturer_name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci,
    is_active TINYINT(1) UNSIGNED DEFAULT '0' COMMENT '0: inactive, 1: active',
    delete_date INT,
    insert_date INT,
    update_date INT,
    
    FOREIGN KEY (delete_date) REFERENCES dim_dates(date_sk) ON DELETE SET NULL,
    FOREIGN KEY (insert_date) REFERENCES dim_dates(date_sk) ON DELETE SET NULL,
    FOREIGN KEY (update_date) REFERENCES dim_dates(date_sk) ON DELETE SET NULL
);

-- Table: dim_products
CREATE TABLE IF NOT EXISTS dim_products (
    id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    natural_key INT UNSIGNED NOT NULL,
    sku_no VARCHAR(32) NOT NULL,
    product_name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    product_description VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    image_url VARCHAR(255),
    specifications JSON DEFAULT NULL,
    price DECIMAL(10, 2) NOT NULL,
    original_price DECIMAL(10, 2) NOT NULL,
    stock INT UNSIGNED DEFAULT '0',
    manufacturer_id INT UNSIGNED,
    is_active TINYINT(1) UNSIGNED DEFAULT '0' COMMENT '0: inactive, 1: active',
    delete_date INT,
    insert_date INT,
    update_date INT,
    expired_date DATE DEFAULT '9999-12-31',
    INDEX idx_sku_no (sku_no),
    
    FOREIGN KEY (manufacturer_id) REFERENCES dim_manufacturers(id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (delete_date) REFERENCES dim_dates(date_sk) ON DELETE SET NULL,
    FOREIGN KEY (insert_date) REFERENCES dim_dates(date_sk) ON DELETE SET NULL,
    FOREIGN KEY (update_date) REFERENCES dim_dates(date_sk) ON DELETE SET NULL
) COMMENT = 'SKU';