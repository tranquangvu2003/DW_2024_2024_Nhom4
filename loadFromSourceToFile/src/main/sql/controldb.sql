create database if not exists control;
use control;
create table if not exists data_file_configs
(
    id          int           not null auto_increment,
    name        varchar(1000) not null,
    description varchar(1000),
    source_path varchar(1000),
    location    varchar(1000),
    format      varchar(1000),
    destination varchar(1000),
    columns     varchar(1000),
    created_at  timestamp default current_timestamp,
    updated_at  timestamp default current_timestamp,
    created_by  varchar(1000),
    updated_by  varchar(1000)
    );
create table if not exists data_files
(
    id           int           not null auto_increment,
    df_config_id int           not null,
    name         varchar(1000) not null,
    row_count    bigint,
    status       varchar(1000),
    note         varchar(1000),
    created_at   timestamp default current_timestamp,
    updated_at   timestamp default current_timestamp,
    created_by   varchar(1000),
    updated_by   varchar(1000)
    );
create table if not exists logs(
                                   id int not null auto_increment,
                                   event varchar(1000),
    status varchar(1000),
    note varchar(1000),
    created_at timestamp default current_timestamp
    );

 Đây là database để tạo và lưu barng log