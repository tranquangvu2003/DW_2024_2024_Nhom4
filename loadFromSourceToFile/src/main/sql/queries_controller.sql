USE db_controller;

SELECT c.*, l.id log_id, db_staging.*, db_warehouse.* FROM configs c
JOIN logs l ON c.id = l.config_id
JOIN db_configs db_staging ON c.staging_config = db_staging.id
JOIN db_configs db_warehouse ON c.datawarehouse_config = db_warehouse.id
WHERE l.status LIKE 'RE'
AND c.datawarehouse_table LIKE 'dim_manufacturers'
AND c.is_active = 1;