package crawl;

import Email.EmailService;
import dao.processDao;
import entities.configs;
import entities.logs;
import entities.process;
import org.apache.tools.ant.taskdefs.SendEmail;
import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import static dao.configsDao.loadConfig;
import static dao.logsDao.logException;
import static dao.logsDao.saveLog;
import static dao.processDao.insertProcess;
import static dao.processDao.updateProcessStatus;
import static database.ConnectToDatabase.getEmail;
import static handleExceiption.exception.connectConfigException;

public class ProductCrawl {

// 1. Khởi tạo các biến lưu trữ dữ liệu config

    // Biến lưu các giá trị từ bảng config của thế giới di động
    public static configs configsTgdd;

    // Biến lưu các giá trị từ bảng config của tiki
    public static configs configsTiki;

    // Biến lấy ra thời gian hiện tại
    public static Timestamp datetime = new Timestamp(System.currentTimeMillis());

    // Biến lấy ra id của process vừa chèn
    public static int idProcess;

    // Biến cấu hình gửi mail
    public static EmailService emailService;

    // Hàm main
    public static void main(String[] args) {

        // Khởi tạo process thông báo quá trình crawl bắt đầu.
        idProcess = insertProcess(new process(3, "crawl", processDao.STATUS_READY, datetime, datetime));


        try {
            System.out.println("Bắt đầu crawl");

// 2. Load các giá trị từ bảng configs.
            // Load các config chứa thông tin crawl Tiki (id là 2)
            configsTiki = loadConfig(2);
            // Load các config chứa thông tin crawl TGDD (id là 3)
            configsTgdd = loadConfig(3);

            System.out.println("configsTgdd: " + configsTgdd);
            System.out.println("configsTiki: " + configsTiki);

        } catch (Exception e) {
 //2.1 Thực hiện thông báo gửi mail nếu không connect được config.
            connectConfigException(e);
            e.printStackTrace();
            // Dừng chương trình
            System.exit(1);
        }

        // Thông báo quá trình crawl đang chạy.
        updateProcessStatus(idProcess,processDao.STATUS_RUNNING);
// 3. Duyệt qua các liên kết
        String tikiApiUrl = "https://tiki.vn/api/v2/products?limit=40&include=advertisement,brand,specifications,price,review&aggregations=2&trackity_id=b99a8719-716f-b1cf-6233-523360a75090&brand=17825,17826&q=laptop";
        List<String> tgddUrls = List.of(
                "https://www.thegioididong.com/laptop-acer?itm_source=trang-nganh-hang&itm_medium=quicklink"
                , "https://www.thegioididong.com/laptop-hp-compaq?itm_source=trang-nganh-hang&itm_medium=quicklink"
                , "https://www.thegioididong.com/laptop-dell?itm_source=trang-nganh-hang&itm_medium=quicklink");

        List<String[]> productTiki = new ArrayList<>();
        List<String[]> productTgdd = new ArrayList<>();


// 4. Lặp qua từng liên kết lấy dữ liệu các sản phẩm.
        try {
            productTiki.addAll(crawlDataTiki(tikiApiUrl));
        } catch (IOException e) {
            //ghi log nếu gặp exception lỗi lấy dữ liệu từ liên kết tiki.
            logException(new logs(3, "Lỗi khi crawl dữ liệu từ Tiki",  new Timestamp(System.currentTimeMillis()), "Error"));
            System.out.println("Lỗi khi crawl dữ liệu từ Tiki: " + e.getMessage());
        }
        for (String url : tgddUrls) {
            productTgdd.addAll(crawlDataTGDD(url));
        }

        boolean saveDataToCSVTiki = false;
        boolean saveDataToCSVBackupTiki = false ;

        boolean saveDataToCSVTgdd = false;
        boolean saveDataToCSVBackupTgdd = false;

// 5. Kiểm tra dữ liệu có bị trống hay không.
        if (productTiki.isEmpty()) {
            System.out.println("Không tìm thấy dữ liệu");
 // 5.1 Ghi log thông báo dữ liệu bị trống.
            saveLog(new logs(3,  "Không tìm thấy dữ liệu của sản phẩm của Tiki", new Timestamp(System.currentTimeMillis()),  "INFOR"));
        }else{
// 6. Lưu dữ liệu vào file csv.
            saveDataToCSVTiki = saveDataToCSV(productTiki, configsTiki.getFileLocation(),"thegioididong_");  // Lưu dữ liệu vào CSV
            saveDataToCSVBackupTiki = saveDataToCSV(productTiki, configsTiki.getBackupPath(),"thegioididong_");
        }

        if (productTgdd.isEmpty()) {
            System.out.println("Không tìm thấy dữ liệu");
            saveLog(new logs(3,  "Không tìm thấy dữ liệu của sản phẩm của Thegioididong", new Timestamp(System.currentTimeMillis()),  "INFOR"));
        }else{
            saveDataToCSVTgdd = saveDataToCSV(productTgdd, configsTgdd.getFileLocation(),"tiki_");  // Lưu dữ liệu vào CSV
            saveDataToCSVBackupTgdd = saveDataToCSV(productTgdd, configsTgdd.getBackupPath(),"tiki_");
        }





        if (!saveDataToCSVTiki) {

 // 7.2 Gửi email thông báo lỗi crawl thất bại.
            emailService.send(getEmail(),"CRAWL DỮ LIỆU NGÀY "+ datetime + " THẤT BẠI","Dữ liệu ngày "+datetime+" đã được crawl thất bại!.");
 // 8.2 Cập nhật thông báo process crawl thất bại.
            updateProcessStatus(idProcess,processDao.STATUS_FAILED);
 // 9.2 Ghi log thông báo crawl thất bại.
            saveLog(new logs(3, "Đã xảy ra lỗi khi lưu dữ liệu của Tiki", new Timestamp(System.currentTimeMillis()), "error"));
        } else if (!saveDataToCSVBackupTiki) {
            saveLog(new logs(3, "Đã xảy ra lỗi khi lưu file backup dữ liệu của Tiki", new Timestamp(System.currentTimeMillis()), "error"));
        } else if (!saveDataToCSVTgdd) {
 // 7.2 Gửi email thông báo lỗi thất bại.
            emailService.send(getEmail(),"CRAWL DỮ LIỆU NGÀY "+ datetime + " THẤT BẠI","Dữ liệu ngày "+datetime+" đã được crawl thất bại!.");
 // 8.2 Cập nhật thông báo process crawl thất bại.
            updateProcessStatus(idProcess,processDao.STATUS_FAILED);
 // 9.2 Ghi log thông báo crawl thất bại.
            saveLog(new logs(3, "Đã xảy ra lỗi khi lưu file backup dữ liệu", new Timestamp(System.currentTimeMillis()), "error"));
        } else if (!saveDataToCSVBackupTgdd) {
            saveLog(new logs(3, "Đã xảy ra lỗi khi lưu file backup dữ liệu", new Timestamp(System.currentTimeMillis()), "error"));
        } else {
 // 7.1 Gửi email thông báo kết quả crawl thành công.
            emailService.send(getEmail(),"CRAWL DỮ LIỆU NGÀY "+ datetime + " THÀNH CÔNG","Dữ liệu ngày "+datetime+" đã được crawl thành công!.");

 // 8.1 Cập nhật thông báo process crawl thành công cho quá trình tiếp theo.
            updateProcessStatus(idProcess,processDao.STATUS_SUCCESS);
            idProcess = insertProcess(new process(1, "stagging", processDao.STATUS_READY, datetime, datetime));
 // 9.1 Ghi log thông báo crawl thành công.
            saveLog(new logs(3, "Lưu dữ liệu thành công", new Timestamp(System.currentTimeMillis()),"info"));
        }
    }
    /*
    == --------Các phương thức hỗ trợ-----------------------------------------------==
     */

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
            System.err.println("Lỗi khi lưu dữ liệu vào CSV: " + e.getMessage());
            return false;
        }
        return true;
    }

    //     Phương thức hỗ trợ Crawl dữ liệu từ Thế Giới Di Động
    public static List<String[]> crawlDataTGDD(String url) {
        List<String[]> productList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).get();

            Element categoryPage = doc.selectFirst("#categoryPage");
            String brand_id = "";
            if (categoryPage != null) {
                brand_id = categoryPage.attr("data-id");
            } else {
                System.out.println("Thẻ categoryPage không tồn tại trên trang.");
            }


            Elements items = doc.select("li.item");
            for (Element item : items) {
                String productId = item.attr("data-id");
                String sku = item.attr("data-productcode");
                String detailUrl = "https://www.thegioididong.com" + item.select("a[href]").attr("href");
                String name = item.select("a").attr("data-name");
                String list_price = item.select("p.price-old").text().replaceAll("[^\\d]", "");
                String original_price = list_price;
                String discount_rate = item.select("span.percent").text();
                String short_description = getShortDescriptionTGDD(detailUrl);
                if (discount_rate.isEmpty()) {
                    discount_rate = "0"; // Nếu không có giảm giá, mặc định là 0%
                } else {
                    discount_rate = discount_rate.replaceAll("[^\\d]", "");
                    if (!discount_rate.isEmpty()) {
                        discount_rate = Math.abs(Integer.parseInt(discount_rate)) + ""; // Đảm bảo là số dương và có dấu %
                    }
                }
                String price = item.select("strong.price").text().replaceAll("[^\\d]", "");

                String discount = "";
                try {
                    int listPrice = list_price.isEmpty() ? 0 : Integer.parseInt(list_price);
                    int productPrice = price.isEmpty() ? 0 : Integer.parseInt(price);
                    discount = String.valueOf(listPrice - productPrice);
                } catch (NumberFormatException e) {
                    System.out.println("Lỗi khi chuyển đổi giá thành số nguyên: " + e.getMessage());
                }


                String all_time_quantity_sold = item.select("all_time_quantity_sold").text();
                String rating = item.select("div.vote-txt b").text();
                rating = rating.isEmpty() ? "0" : rating;  // Nếu rating rỗng thì gán 0

                String numReviews = item.select("div.vote-txt").text().replaceAll("[^\\d]", "");
                numReviews = numReviews.isEmpty() ? "0" : numReviews;  // Nếu rating rỗng thì gán 0
                String brand_name = item.select("a").attr("data-brand");

                String inventory_status = "availability";
                String stock_item_qty = item.attr("data-subgroup");
                String stock_item_max_sale_qty = item.attr("data-maingroup");

//                String brand_id = item.select("a").attr("data-brand");
                String href = item.select("a").attr("href");
                String url_key = href.substring(href.lastIndexOf("/") + 1); // Lấy phần cuối của URL
                String url_path = href; // Đường dẫn đầy đủ, ví dụ: /laptop/acer-aspire..
                String thumb_url = item.select("img.lazyloaded").attr("data-src");

                // Lấy thông tin từ thẻ utility (màn hình, CPU, card, pin, khối lượng)
                JSONObject specifications = new JSONObject();
                Elements utilityItems = item.select("div.utility p");
                for (Element utilityItem : utilityItems) {
                    String text = utilityItem.text();

                    if (text.contains("Màn hình")) {
                        specifications.put("screen", text);
                    } else if (text.contains("CPU")) {
                        specifications.put("cpu", text);
                    } else if (text.contains("Card")) {
                        specifications.put("card", text);
                    } else if (text.contains("Pin")) {
                        specifications.put("battery", text);
                    } else if (text.contains("Khối lượng")) {
                        specifications.put("weight", text);
                    }
                }

                String specificationsStr = specifications.toString().replace(",", ";");
                String date = LocalDate.now().toString(); // Lấy ngày hiện tại
                String variations = "[]";

                // Lấy option từ HTML
                Elements optionElements = item.select(".item-compare span");
                JSONObject options = new JSONObject();
                for (Element optionElement : optionElements) {
                    String optionText = optionElement.text();
                    options.append("Cấu hình", optionText);
                }

                String optionStr = options.length() > 0 ? options.toString().replaceAll("[,\\n\\r]", " ").trim() : "{}";

                productList.add(new String[]{productId, sku, name, short_description, price, list_price, original_price, discount, discount_rate, all_time_quantity_sold, rating, numReviews, inventory_status, stock_item_qty, stock_item_max_sale_qty, brand_id, brand_name, url_key, url_path,thumb_url,optionStr, specificationsStr, variations, date});
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi crawl dữ liệu từ TGDD: " + e.getMessage());
        }
        return productList;
    }

//     Phương thức hỗ trợ Crawl dữ liệu từ Tiki
    public static List<String[]> crawlDataTiki(String apiUrl) throws IOException {
        List<String[]> productList = new ArrayList<>();
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
                        String thumbnail_url = item.optString("thumbnail_url", "");
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

                        productList.add(new String[]{id, sku, name, short_description, price, list_price, originalPrice, discount, discount_rate, all_time_quantity_sold, averageRating, reviewCount, inventory_status, stock_item_qty, stock_item_max_sale_qty, brand_id, brandName, url_key, url_path, thumbnail_url, options, specifications, variations, date});
                    }
                }
            } else {
                System.out.println("Failed to retrieve data from Tiki: " + responseCode);
            }
        }
        return productList;
    }

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
            return "Lỗi xảy ra: " + e.getMessage();
        }
    }

    public static String getShortDescriptionTGDD(String detailUrl) {
        String shortDescription = "N/A"; // Giá trị mặc định nếu không tìm được
        try {
            Document doc = Jsoup.connect(detailUrl).get();

            // Lấy mô tả ngắn từ các thẻ HTML của trang chi tiết
            // Ví dụ: Kiểm tra thẻ HTML nào chứa mô tả ngắn
            Element descriptionElement = doc.select("div.text-detail h3").first(); // Thay ".article" bằng selector chính xác
            if (descriptionElement != null) {
                shortDescription = descriptionElement.text().replace(",", ";").trim();
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi lấy mô tả từ " + detailUrl + ": " + e.getMessage());
        }
        return shortDescription;
    }

    // Hàm tách thông tin cấu hình từ tên sản phẩm
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
