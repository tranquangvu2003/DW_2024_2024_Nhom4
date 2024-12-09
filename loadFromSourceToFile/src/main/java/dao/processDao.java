package dao;

import database.ConnectToDatabase;
import entities.process;

import java.sql.*;

public class processDao {

    public static final String STATUS_READY = "READY";
    public static final String STATUS_RUNNING = "RUNNING";
    public static final String STATUS_FAILED = "FAILED";
    public static final String STATUS_SUCCESS = "SUCCESS";

    public static int insertProcess(process proc) {
        String sql = "INSERT INTO process (config_id, process_at, status, begin_date, update_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = ConnectToDatabase.getConnect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            // Gán giá trị cho các tham số
            if (proc.getConfigId() != null) {
                preparedStatement.setObject(1, proc.getConfigId());
            } else {
                preparedStatement.setNull(1, java.sql.Types.INTEGER);
            }
            preparedStatement.setString(2, proc.getProcessAt() != null ? proc.getProcessAt() : "");
            preparedStatement.setString(3, proc.getStatus() != null ? proc.getStatus() : "PENDING"); // Mặc định là "PENDING"
            preparedStatement.setTimestamp(4, proc.getBeginDate() != null ? proc.getBeginDate() : new Timestamp(System.currentTimeMillis())); // Nếu null, dùng thời gian hiện tại
            preparedStatement.setTimestamp(5, proc.getUpdateDate() != null ? proc.getUpdateDate() : new Timestamp(System.currentTimeMillis()));

            // Thực hiện câu lệnh
            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                // Lấy id của dòng vừa chèn
                try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        int id = generatedKeys.getInt(1);
                        return id;
                    } else {
                        System.err.println("Không thể lấy id của process vừa chèn.");
                        return -1; // hoặc xử lý lỗi khác tùy theo yêu cầu của bạn
                    }
                }
            } else {
                System.err.println("Không có process nào được chèn. Vui lòng kiểm tra lại dữ liệu.");
                return -1; // hoặc xử lý lỗi khác tùy theo yêu cầu của bạn
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi chèn process: " + e.getMessage());
            e.printStackTrace();
            return -1; // hoặc xử lý lỗi khác tùy theo yêu cầu của bạn
        }
    }


    public static boolean updateProcessStatus(int processId, String newStatus) {
        String sql = "UPDATE process SET status = ? WHERE id = ?";

        try (Connection conn = ConnectToDatabase.getConnect();
             PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

            // Gán giá trị cho các tham số
            preparedStatement.setString(1, newStatus); // Trạng thái mới
            preparedStatement.setInt(2, processId);    // ID của process cần cập nhật

            // Thực hiện câu lệnh
            int rowsUpdated = preparedStatement.executeUpdate();
            if (rowsUpdated > 0) {
                return true;
            } else {
                System.err.println("Không có process nào được cập nhật. Vui lòng kiểm tra ID.");
                return false;
            }

        } catch (SQLException e) {
            System.err.println("Lỗi khi cập nhật trạng thái process: " + e.getMessage());
            e.printStackTrace();
            return false;
        } catch (Exception ex) {
            System.err.println("Lỗi không xác định khi cập nhật trạng thái process: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }


}
