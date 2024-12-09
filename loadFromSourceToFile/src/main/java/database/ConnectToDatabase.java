package database;


import java.io.IOException;
import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

// 1.Khởi tạo kết nối đến database
public class ConnectToDatabase {
    private static String db;
    private static String host;
    private static String port;
    private static String nameDB;
    private static String username;
    private static String password;
    public static String email;


    // 2. Lấy các thuộc tính của database trong file config.properties
    static {
        Properties properties = new Properties();

        try (InputStream inputStream = ConnectToDatabase.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream == null) {
                throw new IOException("File db.properties không tồn tại trong thư mục resources.");
            }
            properties.load(inputStream);

            db = properties.getProperty("db");
            host = properties.getProperty("db.host");
            port = properties.getProperty("db.port");
            nameDB = properties.getProperty("db.name");
            username = properties.getProperty("db.username");
            password = properties.getProperty("db.password");
            email = properties.getProperty("email");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getEmail(){
        Properties properties = new Properties();

        try (InputStream inputStream = ConnectToDatabase.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (inputStream == null) {
                throw new IOException("File db.properties không tồn tại trong thư mục resources.");
            }
            properties.load(inputStream);

            email = properties.getProperty("email");

        } catch (IOException e) {
            e.printStackTrace();
        }
        return email;
    }

    //Constructor
    public ConnectToDatabase() {
    }

    //3. kết nối đến database
    public static Connection getConnect() {
        String url = "jdbc:"+db+"://"+host+":"+port+"/"+nameDB;
        System.out.println("url: "+url);
        Connection connection = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(url, username, password);
//            System.out.println("Connect success");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return connection;
    }

    //4. Đóng kết nối
    public static void closeResources(Connection connection, PreparedStatement preparedStatement, ResultSet rs) {
        try {
            if (rs != null) {
                rs.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public static void main(String[] args) throws Exception {
        System.out.println("connect:"+getConnect());
    }

}