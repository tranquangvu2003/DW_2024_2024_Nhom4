DELIMITER //

DROP PROCEDURE IF EXISTS CreateStagingProducts;
CREATE PROCEDURE CreateStagingProducts()
BEGIN
    DROP TABLE IF EXISTS db_staging.staging_products;

    CREATE TABLE db_staging.staging_products (
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
END //

DELIMITER ;

DROP PROCEDURE IF EXISTS MoveDataToStagingProducts;
DELIMITER //

CREATE PROCEDURE MoveDataToStagingProducts()
BEGIN
    INSERT INTO db_staging.staging_products (
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
        url_key,
        url_path,
        thumbnail_url,
        options,
        specifications,
        variations
    FROM db_controller.temp_staging;
END //

DELIMITER ;

 DROP TABLE IF EXISTS db_controller.temp_staging;
    CREATE TABLE db_controller.temp_staging (
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

    ALTER TABLE db_controller.temp_staging ADD INDEX(id(255));

CALL LoadDataIntoTempStaging('2024-12-06');

LOAD DATA LOCAL INFILE 'D:/dataLaptop_20241206.csv' INTO TABLE temp_staging FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"' LINES TERMINATED BY '\n' IGNORE 1 ROWS ;

CALL CleanTempStagingData();

CALL CreateStagingProducts();
CALL MoveDataToStagingProducts();

DELETE FROM db_controller.temp_staging;

-- 1. process 1: READY
-- 2. process 1: Cập nhật RUNNING, Insert Log (process = 1)
-- 3. process 1: Cập nhật SUCCESS / FAILED, Insert Log (process = 1)
-- 4. INSERT process 2 at <chu trình sau>: READY, config = config của process 1, Insert Log (process = 2)
-- 5. Quay lại 1
