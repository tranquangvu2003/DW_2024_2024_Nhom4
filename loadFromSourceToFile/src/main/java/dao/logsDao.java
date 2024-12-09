package dao;

import database.ConnectToDatabase;
import entities.logs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

public class logsDao {

    public static boolean saveLog(logs log) {
        String sql = "INSERT INTO logs (process_id, message, insert_date, level) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectToDatabase.getConnect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setObject(1, log.getProcessId());
            preparedStatement.setString(2, log.getMessage());
            preparedStatement.setTimestamp(3, log.getInsertDate());
            preparedStatement.setString(4, log.getLevel());

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
        String sql = "INSERT INTO logs (process_id, message, insert_date, level) VALUES (?, ?, ?, ?)";

        try (Connection conn = ConnectToDatabase.getConnect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            preparedStatement.setObject(1, log.getProcessId());
            preparedStatement.setString(2, log.getMessage());
            preparedStatement.setTimestamp(3, log.getInsertDate());
            preparedStatement.setString(4, log.getLevel());

            preparedStatement.executeUpdate();
            System.out.println("Log ghi thành công!");
            return true;

        } catch (SQLException e) {
            System.err.println("Lỗi khi ghi log: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
