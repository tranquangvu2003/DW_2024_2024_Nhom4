package dao;

import database.ConnectToDatabase;
import entities.configs;
import entities.db_configs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class configsDao {

    public static boolean loadConfig(int id) {
        String sql = "SELECT * FROM configs WHERE id = ?";
        configs config = null;

        try (Connection conn = ConnectToDatabase.getConnect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                config = new configs();
                config.setId(resultSet.getInt("id"));
                config.setSourcePath(resultSet.getString("source_path"));
                config.setBackupPath(resultSet.getString("backup_path"));
                config.setStagingConfig(resultSet.getInt("staging_config"));
                config.setDatawarehouseConfig(resultSet.getInt("datawarehouse_config"));
                config.setStagingTable(resultSet.getString("staging_table"));
                config.setDatawarehouseTable(resultSet.getString("datawarehouse_table"));
                config.setPeriod(resultSet.getString("period"));
                config.setVersion(resultSet.getString("version"));
                config.setIsActive(resultSet.getByte("is_active"));
                config.setInsertDate(resultSet.getTimestamp("insert_date"));
                config.setUpdateDate(resultSet.getTimestamp("update_date"));
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi load config: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static db_configs loadDbConfig(int id) {
        String sql = "SELECT * FROM db_configs WHERE id = ?";
        db_configs dbConfig = null;

        try (Connection conn = ConnectToDatabase.getConnect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                dbConfig = new db_configs();
                dbConfig.setId(resultSet.getInt("id"));
                dbConfig.setDbName(resultSet.getString("db_name"));
                dbConfig.setUrl(resultSet.getString("url"));
                dbConfig.setUsername(resultSet.getString("username"));
                dbConfig.setPassword(resultSet.getString("password"));
                dbConfig.setDriverClassName(resultSet.getString("driver_class_name"));
            }
        } catch (SQLException e) {
            System.out.println("Lỗi khi load db config: " + e.getMessage());
            e.printStackTrace();
        }
        return dbConfig;
    }
}
