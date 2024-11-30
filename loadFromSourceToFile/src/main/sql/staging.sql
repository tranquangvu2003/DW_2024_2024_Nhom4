SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
Use db_controller;
DROP TABLE IF EXISTS `temp_staging`;

-- Tạo bảng tạm lưu trữ dữ liệu từ file lên 
CREATE TABLE `temp_staging` (
  `id` TEXT,
  `sku` TEXT,
  `product_name` TEXT,
  `short_description` TEXT,
  `price` TEXT,
  `list_price` TEXT,
  `original_price` TEXT,
  `discount` TEXT,
  `discount_rate` TEXT,
  `all_time_quantity_sold` TEXT,
  `rating_average` TEXT,
  `review_count` TEXT,
  `inventory_status` TEXT,
  `stock_item_qty` TEXT,
  `stock_item_max_sale_qty` TEXT,
  `brand_id` TEXT,
  `brand_name` TEXT,
  `url_key` TEXT,
  `url_path` TEXT,
  `thumbnail_url` TEXT,
  `options` TEXT,
  `specifications` TEXT,
  `variations` TEXT
);

SET FOREIGN_KEY_CHECKS = 1;
ALTER TABLE temp_staging ADD INDEX(id(255)); 

-- load data từ file lên bảng tạm
LOAD DATA INFILE '/var/lib/mysql-files/crawled_data_laptop.csv'
INTO TABLE temp_staging
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'

IGNORE 1 LINES;

-- Xử lý dữ liệu trong bảng tạm
UPDATE temp_staging
SET 
    id = COALESCE(id, '0'),
    sku = COALESCE(TRIM(sku), 'N/A'),
    product_name = COALESCE(TRIM(product_name), 'N/A'),
    short_description = COALESCE(TRIM(short_description), 'N/A'),
    price = COALESCE(NULLIF(TRIM(price), ''), '0.00'),
    list_price = COALESCE(NULLIF(TRIM(list_price), ''), '0.00'),
    original_price = COALESCE(NULLIF(TRIM(original_price), ''), '0.00'),
    discount = COALESCE(NULLIF(TRIM(discount), ''), '0.00'),
    discount_rate = COALESCE(NULLIF(TRIM(discount_rate), ''), '0.00'),
    all_time_quantity_sold = COALESCE(NULLIF(TRIM(all_time_quantity_sold), ''), '0'),
    rating_average = COALESCE(NULLIF(TRIM(rating_average), ''), '0.00'),
    review_count = COALESCE(NULLIF(TRIM(review_count), ''), '0'),
    inventory_status = COALESCE(TRIM(inventory_status), 'N/A'),
    stock_item_qty = COALESCE(NULLIF(TRIM(stock_item_qty), ''), '0'),
    stock_item_max_sale_qty = COALESCE(NULLIF(TRIM(stock_item_max_sale_qty), ''), '0'),
    brand_id = COALESCE(NULLIF(TRIM(brand_id), ''), '0'),
    brand_name = COALESCE(TRIM(brand_name), 'N/A'),
    url_key = COALESCE(TRIM(url_key), 'N/A'),
    url_path = COALESCE(TRIM(url_path), 'N/A'),
    thumbnail_url = COALESCE(TRIM(thumbnail_url), 'N/A'),
    options = COALESCE(CASE 
        WHEN JSON_VALID(REPLACE(TRIM(options), "'", '"')) THEN REPLACE(TRIM(options), "'", '"')
        ELSE '"N/A"'
    END, '"N/A"'),
    specifications = COALESCE(CASE 
        WHEN JSON_VALID(REPLACE(TRIM(specifications), "'", '"')) THEN REPLACE(TRIM(specifications), "'", '"')
        ELSE '"N/A"'
    END, '"N/A"'),
    variations = COALESCE(CASE 
        WHEN JSON_VALID(REPLACE(TRIM(variations), "'", '"')) THEN REPLACE(TRIM(variations), "'", '"')
        ELSE '"N/A"'
    END, '"N/A"')
    where id IS not null;


Use db_staging;
DROP TABLE IF EXISTS staging_products;
-- Tạo bảng staging_products
CREATE TABLE staging_products (
   natural_key VARCHAR(255),
    sku VARCHAR(255),
    product_name VARCHAR(255),
    short_description TEXT,
    price DECIMAL(10, 2),
    list_price DECIMAL(10, 2),
    original_price DECIMAL(10, 2),
    discount DECIMAL(10, 2),
    discount_rate DECIMAL(5, 2),
    all_time_quantity_sold DOUBLE,
    rating_average DECIMAL(3, 2),
    review_count INT,
    inventory_status VARCHAR(50),
    stock_item_qty INT,
    stock_item_max_sale_qty INT,
    brand_id INT,
    brand_name VARCHAR(255),
    url_key VARCHAR(255),
    url_path VARCHAR(255),
    thumbnail_url VARCHAR(255),
    options JSON,
    specifications JSON, 
    variations JSON
);


-- Chuyển dữ liệu từ bảng tạm sang bảng staging
INSERT INTO staging_products (
    natural_key,
    sku,
    product_name,
    short_description,
    price,
    list_price,
    original_price,
    discount,
    discount_rate,
    all_time_quantity_sold,
    rating_average,
    review_count,
    inventory_status,
    stock_item_qty,
    stock_item_max_sale_qty,
    brand_id,
    brand_name,
    url_key,
    url_path,
    thumbnail_url,
    options,
    specifications,
    variations
)
SELECT
    id AS natural_key,
    sku,
    product_name,
    short_description,
    CAST(price AS DECIMAL(10, 2)) AS price,
    CAST(list_price AS DECIMAL(10, 2)) AS list_price,
    CAST(original_price AS DECIMAL(10, 2)) AS original_price,
    CAST(discount AS DECIMAL(10, 2)) AS discount,
    CAST(discount_rate AS DECIMAL(5, 2)) AS discount_rate,
    CAST(all_time_quantity_sold AS DOUBLE) AS all_time_quantity_sold,
    CAST(rating_average AS DECIMAL(3, 2)) AS rating_average,
    CAST(review_count AS SIGNED) AS review_count,
    inventory_status,
    CAST(stock_item_qty AS SIGNED) AS stock_item_qty,
    CAST(stock_item_max_sale_qty AS SIGNED) AS stock_item_max_sale_qty,
    CAST(brand_id AS SIGNED) AS brand_id,
    brand_name,
    LEFT(url_key, 255) AS url_key,
    LEFT(url_path, 255) AS url_path,
    thumbnail_url,
    options,
    specifications,
    variations
FROM db_controller.temp_staging;

DROP TABLE IF EXISTS db_controller.temp_staging;