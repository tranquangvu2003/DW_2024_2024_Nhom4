package handleExceiption;

import Email.EmailService;
import database.ConnectToDatabase;

import static database.ConnectToDatabase.getEmail;

public class exception {

    public static boolean connectConfigException(Exception e){
        EmailService emailService = new EmailService();
        System.err.println("Không thể kết nối đến config");

        String subject = "KHÔNG LẤY CÁC DỮ LIỆU TRONG CONFIG";
        emailService.send(getEmail(),subject,"Lỗi khi kết nối đến config: "+ e.getMessage().toString());
        return false;
    }
}
