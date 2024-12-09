package dao;

import database.ConnectToDatabase;
import entities.configs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class configsDao {

    public static configs loadConfig(int id) {
        String sql = "SELECT * FROM configs WHERE id = ?";
        configs config = new configs();

        try (Connection conn = ConnectToDatabase.getConnect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                config.setId(resultSet.getInt("id"));
                config.setFileName(resultSet.getString("file_name"));
                config.setSourcePath(resultSet.getString("source_path"));
                config.setFileLocation(resultSet.getString("file_location"));
                config.setBackupPath(resultSet.getString("backup_path"));
                config.setWarehouseProcedure(resultSet.getString("warehouse_procedure"));
                config.setVersion(resultSet.getString("version"));
                config.setIsActive(resultSet.getByte("is_active"));
                config.setInsertDate(resultSet.getTimestamp("insert_date"));
                config.setUpdateDate(resultSet.getTimestamp("update_date"));

            } else {
                System.out.println("No config found with id: " + id);
            }
        } catch (SQLException e) {
            System.out.println("Error loading config: " + e.getMessage());
            e.printStackTrace();
        }

        return config;
    }

    public static configs loadLatestConfig() {
        String sql = "SELECT * FROM configs ORDER BY update_date DESC LIMIT 1";
        configs config = new configs();

        try (Connection conn = ConnectToDatabase.getConnect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                config.setId(resultSet.getInt("id"));
                config.setFileName(resultSet.getString("file_name"));
                config.setSourcePath(resultSet.getString("source_path"));
                config.setFileLocation(resultSet.getString("file_location"));
                config.setBackupPath(resultSet.getString("backup_path"));
                config.setWarehouseProcedure(resultSet.getString("warehouse_procedure"));
                config.setVersion(resultSet.getString("version"));
                config.setIsActive(resultSet.getByte("is_active"));
                config.setInsertDate(resultSet.getTimestamp("insert_date"));
                config.setUpdateDate(resultSet.getTimestamp("update_date"));

            } else {
                System.out.println("No config found.");
            }
        } catch (SQLException e) {
            System.out.println("Error loading latest config: " + e.getMessage());
            e.printStackTrace();
        }

        return config;
    }


    public static void main(String[] args) {
        System.out.println(loadConfig(2));
    }
}
