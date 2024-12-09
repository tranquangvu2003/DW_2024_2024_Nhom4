package crawl;

import Email.EmailService;
import dao.processDao;
import entities.configs;
import entities.logs;
import entities.process;
import helper.JsonToUrls;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static dao.configsDao.loadLatestConfig;
import static dao.logsDao.saveLog;
import static dao.processDao.insertProcess;
import static dao.processDao.updateProcessStatus;
import static database.ConnectToDatabase.getEmail;
import static handleExceiption.exception.*;
import static helper.JsonToUrls.parseTikiApiUrls;

public class ProductCrawl {

// 1. Khởi tạo các biến lưu trữ dữ liệu config


    // Biến lưu các giá trị từ bảng config
    public static configs configs;

    // Biến lấy ra thời gian hiện tại
    public static Timestamp datetime = new Timestamp(System.currentTimeMillis());

    // Biến lấy ra id của process vừa chèn
    public static int idProcess;

    // Biến cấu hình gửi mail
    public static EmailService emailService;

    // Khởi tạo biến phân tích dữ liệu url từ configs
    public static List<String> listUrl;

    // Hàm main
    public static void main(String[] args) {

        try {
// 2. Load các giá trị từ bảng configs.
            // Load các config chứa thông tin crawl (config mới nhất được thêm vào)
            configs = loadLatestConfig();
        } catch (Exception e) {
 //3. Thực hiện thông báo gửi mail nếu không connect được config.
            connectConfigException(e);
            // Dừng chương trình
            System.exit(1);
        }

        // Khởi tạo process thông báo quá trình crawl bắt đầu.
        System.out.println("Bắt đầu crawl");
        idProcess = insertProcess(new process(configs.getId(), "crawl", processDao.STATUS_READY, datetime, datetime));
        emailService = new EmailService();


// 4. Duyệt qua các liên kết
        try {
            // Phân tích JSON từ `source_path` để lấy các URL Tiki
            listUrl = parseTikiApiUrls(configs.getSourcePath()); // listUrl chứa mảng các URL Tiki

            System.out.println("listUrl: "+listUrl);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // Thông báo quá trình crawl đang chạy.
        updateProcessStatus(idProcess,processDao.STATUS_RUNNING);
// 5. Lặp qua từng liên kết và lấy dữ liệu sản phẩm
        List<String[]> productTiki = new ArrayList<>();
        for (String tikiApiUrl : listUrl) {
            try {
                // Lấy dữ liệu từ mỗi URL Tiki
                productTiki.addAll(crawlDataTiki(tikiApiUrl));
            } catch (IOException e) {
                linkTikiException(e);
            }
        }

        boolean saveDataToCSVTiki = false;
        boolean saveDataToCSVBackupTiki = false ;


// 6. Kiểm tra dữ liệu có bị trống hay không.
        if (productTiki.isEmpty()) {
            System.out.println("Không tìm thấy dữ liệu");
// 7. Ghi log thông báo dữ liệu bị trống.
            saveLog(new logs(3,  "Không tìm thấy dữ liệu của sản phẩm của Tiki", new Timestamp(System.currentTimeMillis()),  "infor"));
        }else{
// 8. Lưu dữ liệu vào file csv.
            saveDataToCSVTiki = saveDataToCSV(productTiki, configs.getFileLocation(),"dataLaptop_");  // Lưu dữ liệu vào CSV
            saveDataToCSVBackupTiki = saveDataToCSV(productTiki, configs.getBackupPath(),"dataLaptop_");
        }



        if (!saveDataToCSVTiki) {
 // 12. Gửi email thông báo lỗi crawl thất bại.
            emailService.send(getEmail(),"CRAWL DỮ LIỆU NGÀY "+ datetime + " THẤT BẠI","Dữ liệu ngày "+datetime+" đã được crawl thất bại!.");
 // 13. Cập nhật thông báo process crawl thất bại.
            updateProcessStatus(idProcess,processDao.STATUS_FAILED);
 // 14. Ghi log thông báo crawl thất bại.
            saveLog(new logs(3, "Đã xảy ra lỗi khi lưu dữ liệu của Tiki", new Timestamp(System.currentTimeMillis()), "error"));
        }

        if (!saveDataToCSVBackupTiki) {
            saveLog(new logs(3, "Đã xảy ra lỗi khi lưu file backup dữ liệu của Tiki", new Timestamp(System.currentTimeMillis()), "error"));
        }


        if(saveDataToCSVTiki){
 // 9. Gửi email thông báo kết quả crawl thành công.
            emailService.send(getEmail(),
                    "CRAWL DỮ LIỆU NGÀY " + datetime + " THÀNH CÔNG",
                    "Dữ liệu ngày " + datetime + ": tiki được crawl " + ((saveDataToCSVTiki) ? "thành công" : "thất bại") );

 // 10. Cập nhật thông báo process crawl thành công cho quá trình tiếp theo.
            updateProcessStatus(idProcess,processDao.STATUS_SUCCESS);
            idProcess = insertProcess(new process(1, "stagging", processDao.STATUS_READY, datetime, datetime));
 // 11. Ghi log thông báo crawl thành công.
            saveLog(new logs(3, "Crawl dữ liệu thành công", new Timestamp(System.currentTimeMillis()),"info"));
        }
    }

    /*
    == --------Các phương thức hỗ trợ-----------------------------------------------==
     */
    // *Phương thức hỗ trợ Crawl dữ liệu từ Tiki
    public static List<String[]> crawlDataTiki(String apiUrl) throws IOException {
        List<String[]> productListTiki = new ArrayList<>();
        for (int page = 1; page <= 3; page++) {
            URL url = new URL(apiUrl + "&page=" + page);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept-Language", "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    StringBuilder response = new StringBuilder();
                    String inputLine;
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray data = jsonResponse.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject item = data.getJSONObject(i);
                        String id = String.valueOf(item.get("id"));
                        String sku = item.optString("sku", "N/A");
                        String productUrl = "https://tiki.vn/" + item.optString("url_path", "N/A");
                        String name = item.optString("name", "N/A").replaceAll("[,\\n\\r]", " ").trim();
                        String originalPrice = item.optString("original_price", "N/A");
                        String discount = item.optString("discount", "N/A");
                        String discount_rate = item.optString("discount_rate", "N/A");
                        String price = item.optString("price", "N/A");
                        String list_price = item.optString("price", "N/A");
                        String averageRating = item.optString("rating_average", "N/A");
                        String reviewCount = item.optString("review_count", "N/A");
                        String brandName = item.optString("brand_name", "Unknown");
                        String all_time_quantity_sold = item.optString("all_time_quantity_sold", "");
                        String sellerName = item.optString("seller_name", "N/A");
                        String origin = item.optString("origin", "N/A");
                        String primaryCategoryName = item.optString("primary_category_name", "N/A");
                        String short_description = getDescriptionTiki(productUrl).replaceAll("[,\\n\\r]", " ").trim();
                        String categoryL1Name = "N/A";
                        String brand_id = item.optString("brand_id", "");
                        String url_key = item.optString("url_key", "");
                        String url_path = item.optString("url_path", "");
                        String inventory_status = item.optInt("availability", 0) == 1 ? "availability" : "";
                        String thumbnail_urlTiki = item.optString("thumbnail_url","");
                        JSONArray badgesNew = item.optJSONArray("badges_new");
                        String stock_item_qty = "";
                        String stock_item_max_sale_qty = "";
                        String variations = "[]";
                        String options = String.valueOf(extractOptions(name)).replaceAll("[,\\n\\r]", " ").trim();;

                        if (badgesNew != null) {
                            for (int j = 0; j < badgesNew.length(); j++) {
                                JSONObject badge = badgesNew.getJSONObject(j);
                                stock_item_qty = badge.optString("icon_width", "N/A");
                                stock_item_max_sale_qty = badge.optString("icon_height", "N/A");
                                if (!stock_item_qty.equals("N/A") || !stock_item_max_sale_qty.equals("N/A")) {
                                    break;
                                }
                            }
                        }

                        JSONObject visibleInfo = item.optJSONObject("visible_impression_info");
                        if (visibleInfo != null) {
                            JSONObject amplitude = visibleInfo.optJSONObject("amplitude");
                            if (amplitude != null) {
                                categoryL1Name = amplitude.optString("category_l1_name", "N/A");
                            }
                        }

//                        String specifications = String.format("%s; %s ;%s ;%s", sellerName, primaryCategoryName, categoryL1Name, origin).replaceAll("[\\n\\r]", " ").trim();
                        String specifications = new JSONObject()
                                .put("sellerName", sellerName)
                                .put("primaryCategoryName", primaryCategoryName)
                                .put("categoryL1Name", categoryL1Name)
                                .put("origin", origin)
                                .toString().replace(",", "; "); // Thay thế dấu phẩy bằng dấu chấm phẩy


                        String date = LocalDate.now().toString(); // Lấy ngày hiện tại

                        productListTiki.add(new String[]{id, sku, name, short_description, price, list_price, originalPrice, discount, discount_rate, all_time_quantity_sold, averageRating, reviewCount, inventory_status, stock_item_qty, stock_item_max_sale_qty, brand_id, brandName, url_key, url_path, thumbnail_urlTiki, options, specifications, variations, date});
                    }
                }
            } else {
                System.out.println("Failed to retrieve data from Tiki: " + responseCode);
            }
        }
        return productListTiki;
    }

    // *Phương thức lưu file csv
    public static boolean saveDataToCSV(List<String[]> data, String directoryPath,String name) {
        // Kiểm tra nếu dữ liệu trống, không ghi vào CSV
        if (data.isEmpty()) {
            System.out.println("Dữ liệu trống, không ghi file CSV.");
            return false;
        }

        // Lấy ngày hiện tại với định dạng ngày tháng năm (ddMMyyyy)
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd");
        String currentDate = dtf.format(LocalDate.now()); // Lấy ngày hiện tại

        // Tạo đường dẫn cho file với tên file theo định dạng yêu cầu
        String filePath = directoryPath + File.separator + name + currentDate + ".csv";

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8")) {
            // Thêm BOM để Excel nhận diện UTF-8
            writer.write('\uFEFF');

            // Ghi header vào file CSV chỉ khi có dữ liệu
            String[] header = {"id", "sku", "product_name", "short_description", "price", "list_price",
                    "origin_price", "discount", "discount_rate", "all_time_quantity_sold",
                    "rating_average", "review_count", "inventory_status", "stock_item_qty",
                    "stock_item_max_sale_qty", "brand_id", "brand_name", "url_key", "url_path", "thumbnail_url", "options",
                    "specification", "variations", "date"};
            writer.write(String.join(",", header));
            writer.write("\n");

            // Ghi dữ liệu vào file CSV
            for (String[] rowData : data) {
                writer.write(String.join(",", rowData));
                writer.write("\n");
            }

            System.out.println("Dữ liệu đã được lưu vào: " + filePath);
        } catch (IOException e) {
            saveDataToCSVException(e);
            return false;
        }
        return true;
    }

    // *Phương thức lấy ra mô tả từ trang tiki
    public static String getDescriptionTiki(String url) {
        try {
            // Kết nối và lấy nội dung HTML từ URL
            Document document = Jsoup.connect(url).get();

            // Tìm thẻ <meta name="description"> chứa mô tả sản phẩm
            Element metaDescription = document.selectFirst("meta[name=description]");

            if (metaDescription != null) {
                return metaDescription.attr("content");
            } else {
                return "Không tìm thấy mô tả sản phẩm.";
            }

        } catch (Exception e) {
            getShortDescriptionTikiException(e);
            return "Lỗi xảy ra: " + e.getMessage();
        }
    }

    // *Hàm tách thông tin cấu hình từ tên sản phẩm
    private static JSONObject extractOptions(String name) {
        HashSet<String> configurations = new HashSet<>(); // Đảm bảo các cấu hình không bị trùng lặp
        String[] parts = name.split("[,\\|]");
        for (String part : parts) {
            if (part.toLowerCase().matches(".*(i[357]).*") || part.toLowerCase().contains("ram") || part.toLowerCase().contains("ssd") || part.toLowerCase().contains("fhd")) {
                configurations.add(part.trim().replace(",", ";")); // Thay thế dấu "," bằng ";"
            }
        }

        JSONObject result = new JSONObject();
        if (configurations.isEmpty()) {
            return result; // Trả về {} nếu không có dữ liệu
        } else {
            result.put("Cấu Hình", configurations); // Đưa dữ liệu vào JSON
            return result;
        }
    }
}
