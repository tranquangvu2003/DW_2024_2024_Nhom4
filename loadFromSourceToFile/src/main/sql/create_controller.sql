CREATE DATABASE IF NOT EXISTS db_controller;
USE db_controller;

ALTER DATABASE db_controller CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS db_configs (
	id INT AUTO_INCREMENT PRIMARY KEY,
    db_name VARCHAR(100),
    url VARCHAR(255),
    username VARCHAR(100),
    password VARCHAR(100),
    driver_class_name VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS configs (
	id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    source_path VARCHAR(255),
    backup_path VARCHAR(255),
    staging_config INT,
    datawarehouse_config INT,
    staging_table VARCHAR(50),
    datawarehouse_table VARCHAR(50),
    period Long,
    version VARCHAR(50),
    is_active TINYINT(1) UNSIGNED DEFAULT '0' COMMENT '0: inactive, 1: active',
    insert_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (staging_config) REFERENCES db_configs(id) ON DELETE SET NULL ON UPDATE CASCADE,
    FOREIGN KEY (datawarehouse_config) REFERENCES db_configs(id) ON DELETE SET NULL ON UPDATE CASCADE
);

CREATE TABLE IF NOT EXISTS logs (
	id INT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    config_id INT UNSIGNED,
    status VARCHAR(100) COMMENT 'loading_to_staging, RE, load_to_staging_failed, loading_to_warehouse, load_to_warehouse_completed, load_to_warehouse_failed',
    message TEXT,
    begin_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    level VARCHAR(100) COMMENT 'info, warn, error, debug',
    
	FOREIGN KEY (config_id) REFERENCES configs(id) ON DELETE SET NULL ON UPDATE CASCADE
);

INSERT INTO db_configs (db_name, url, username, password, driver_class_name)
VALUES
('db_staging', 'jdbc:mysql://localhost:3306/db_staging', 'root', '123', 'com.mysql.cj.jdbc.Driver'),
('db_datawarehouse', 'jdbc:mysql://localhost:3306/db_datawarehouse', 'root', '123', 'com.mysql.cj.jdbc.Driver'),
('db_controller', 'jdbc:mysql://localhost:3306/db_staging', 'root', '', 'com.mysql.cj.jdbc.Driver');


INSERT INTO configs (source_path, backup_path, staging_config, datawarehouse_config, staging_table, datawarehouse_table, period, version, is_active)
VALUES
(null, null, 1, 2, 'staging_products', 'dim_manufacturers', 60, 1, 1),
(null, null, 1, 2, 'staging_products', 'dim_products', 60, 1, 1),
('D:\Downloads\demoProject\loadFromSourceToFile\src\main\data', '"C:\Users\HP\Desktop\backup"', 1, 2, 'staging_products', 'dim_manufacturers', 60, 1, 1);
INSERT INTO logs (config_id, status, message, begin_date, update_date, level)
VALUES (1, 'RE', 'Đã load thành công 13 dữ liệu manufacturers vào db_staging', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'info');

INSERT INTO logs (config_id, status, message, begin_date, update_date, level)
VALUES (2, 'RE', 'Đã load thành công 400 dữ liệu products vào db_staging', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'info');