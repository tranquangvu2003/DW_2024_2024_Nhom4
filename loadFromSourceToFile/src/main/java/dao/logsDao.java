package dao;

import database.ConnectToDatabase;
import entities.logs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class logsDao {

    public static boolean saveLog(logs log) {
        Connection connection = null;
        String sql = "INSERT INTO logs (status, message, begin_date, update_date, level, config_id) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = ConnectToDatabase.getConnect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setString(1, log.getStatus());
            preparedStatement.setString(2, log.getMessage());
            preparedStatement.setTimestamp(3, log.getBeginDate());
            preparedStatement.setTimestamp(4, log.getUpdateDate());
            preparedStatement.setString(5, log.getLevel());
            preparedStatement.setInt(6, log.getConfigId());

            preparedStatement.executeUpdate();
            System.out.println("Log ghi thành công!");
            return true;

        } catch (SQLException e) {
            System.err.println("Lỗi khi ghi log: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public static boolean logException(logs log) {
        Connection connection = null;
        String sql = "INSERT INTO logs ( status, message, begin_date, update_date, level,config_id) VALUES (?, ?, ?, ?, ?, ?)";

        PreparedStatement preparedStatement = null;
        try {
            connection = ConnectToDatabase.getConnect();
            preparedStatement = connection.prepareStatement(sql);

            preparedStatement.setString(1, log.getStatus());
            preparedStatement.setString(2, log.getMessage());
            preparedStatement.setTimestamp(3, log.getBeginDate());
            preparedStatement.setTimestamp(4, log.getUpdateDate());
            preparedStatement.setString(5, log.getLevel());
            preparedStatement.setInt(6, log.getConfigId());


            preparedStatement.executeUpdate();
            System.out.println("Log ghi thành công!");
        } catch (SQLException e) {
            System.err.println("Lỗi khi ghi log: " + e.getMessage());
            e.printStackTrace();
            return false;
        } finally {
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    System.err.println("Lỗi khi đóng PreparedStatement: " + e.getMessage());
                }
            }
        }
        return true;
    }
}

