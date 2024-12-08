package test;

import database.ConnectToDatabase;
import entities.configs;
import entities.db_configs;
import entities.logs;
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
import java.util.List;

public class ProductCrawl {

// 1. Khởi tạo các biến lưu trữ dữ liệu config

    // Biến lưu các giá trị từ bảng config
    public static configs configs;

    //Biến lưu giá trị từ bảng db_config
    private static db_configs db_configs;


    // Hàm main
    public static void main(String[] args) {
// 2. Load các giá trị từ bảng configs (có id là 3)

        if (!loadConfig(3) || !loaddbConfig(3)) {
            System.err.println("Lỗi: không thể load các giá trị từ config");
            return;
        }

        System.out.println("configs: " + configs);
        System.out.println("config_db: " + db_configs);


// 3. Duyệt qua các liên kết
        String tikiApiUrl = "https://tiki.vn/api/v2/products?limit=40&include=advertisement,brand,specifications,price,review&aggregations=2&trackity_id=b99a8719-716f-b1cf-6233-523360a75090&brand=17825,17826&q=laptop";
        List<String> tgddUrls = List.of(
                  "https://www.thegioididong.com/laptop-acer?itm_source=trang-nganh-hang&itm_medium=quicklink"
                , "https://www.thegioididong.com/laptop-hp-compaq?itm_source=trang-nganh-hang&itm_medium=quicklink"
                , "https://www.thegioididong.com/laptop-dell?itm_source=trang-nganh-hang&itm_medium=quicklink");

        List<String[]> allProducts = new ArrayList<>();
// 4. Lặp qua từng liên kết lấy dữ liệu các sản phẩm
//        for (String url : tgddUrls) {
//            allProducts.addAll(crawlDataTGDD(url));
//        }

        try {
            allProducts.addAll(crawlDataTiki(tikiApiUrl));
        } catch (IOException e) {
            //ghi log nếu gặp lỗi lấy dữ liệu
            logException(new logs(3, "Error", "Lỗi khi crawl dữ liệu từ Tiki", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Error"));
            System.out.println("Lỗi khi crawl dữ liệu từ Tiki: " + e.getMessage());
        }
        boolean saveDataToCSV = false;
        boolean saveDataToCSVBackup = false;

// 5. Kiểm tra dữ liệu có bị trống hay không
        if (allProducts.isEmpty()) {
            System.out.println("Không tìm thấy dữ liệu");
            saveLog(new logs(3, "Error", "Không tìm thấy dữ liệu của sản phẩm", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "INFOR"));
        } else {
// 6. Lưu file csv
            saveDataToCSV = saveDataToCSV(allProducts, configs.getSourcePath());  // Lưu dữ liệu vào CSV
            saveDataToCSVBackup = saveDataToCSV(allProducts, configs.getBackupPath());
        }

// 7. Ghi log khi xuất file
        if (!saveDataToCSV) {
            saveLog(new logs(3, "Error", "Đã xảy ra lỗi khi lưu dữ liệu", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Error"));
        } else if (!saveDataToCSVBackup) {
            saveLog(new logs(3, "Error", "Đã xảy ra lỗi khi lưu file backup dữ liệu", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "Error"));
        } else {
            saveLog(new logs(3, "RE", "Lưu dữ liệu thành công", new Timestamp(System.currentTimeMillis()), new Timestamp(System.currentTimeMillis()), "INFOR"));
        }
    }
    /*
    == --------Các phương thức hỗ trợ-----------------------------------------------==
     */

    // Lưu dữ liệu vào file CSV với đường dẫn thư mục được chỉ định
//    public static boolean saveDataToCSV(List<String[]> data, String directoryPath) {
//        // Lấy thời gian hiện tại (bao gồm giờ, phút, giây)
//        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
//        String currentTime = dtf.format(LocalDateTime.now());
//        String filePath = directoryPath + File.separator + currentTime + ".csv"; // Tạo đường dẫn đầy đủ cho file
//
//        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8")) {
//            // Thêm BOM để Excel nhận diện UTF-8
//            writer.write('\uFEFF');
//
//            String[] header = {"id", "sku", "product_name", "link-href",  "origin_price", "discount_percent", "sale_price", "img-src", "rating", "review_count", "brand_name", "specification", "date"};
//
//            // Ghi header vào file CSV
//            writer.write(String.join(",", header));
//            writer.write("\n");
//
//            // Ghi dữ liệu vào file CSV
//            for (String[] rowData : data) {
//                writer.write(String.join(",", rowData));
//                writer.write("\n");
//            }
//
//            System.out.println("Dữ liệu đã được lưu vào: " + filePath);
//        } catch (IOException e) {
//            System.err.println("Lỗi khi lưu dữ liệu vào CSV: " + e.getMessage());
//            return false;
//        }
//        return true;
//    }

    public static boolean saveDataToCSV(List<String[]> data, String directoryPath) {
        // Kiểm tra nếu dữ liệu trống, không ghi vào CSV
        if (data.isEmpty()) {
            System.out.println("Dữ liệu trống, không ghi file CSV.");
            return false;
        }

        // Lấy thời gian hiện tại (bao gồm giờ, phút, giây)
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String currentTime = dtf.format(LocalDateTime.now());
        String filePath = directoryPath + File.separator + currentTime + ".csv"; // Tạo đường dẫn đầy đủ cho file

        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(filePath), "UTF-8")) {
            // Thêm BOM để Excel nhận diện UTF-8
            writer.write('\uFEFF');

            String[] header = {"id", "sku", "product_name","short_description",  "price", "list_price", "origin_price", "discount", "discount_rate", "all_time_quantity_sold", "rating_average", "review_count", "inventory_status", "stock_item_qty", "stock_item_max_sale_qty", "brand_id", "brand_name", "url_key", "url_path", "specification", "date"};

            // Ghi header vào file CSV chỉ khi có dữ liệu
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


    public static boolean loadConfig(int id) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = ConnectToDatabase.getConnect();
            String sql = "SELECT * FROM configs WHERE id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Khởi tạo biến configs nếu nó đang null
                if (configs == null) {
                    configs = new configs();
                }

                // Gán các giá trị từ cơ sở dữ liệu cho các thuộc tính của configs
                configs.setId(resultSet.getInt("id"));
                configs.setSourcePath(resultSet.getString("source_path"));
                configs.setBackupPath(resultSet.getString("backup_path"));
                configs.setStagingConfig(resultSet.getInt("staging_config"));
                configs.setDatawarehouseConfig(resultSet.getInt("datawarehouse_config"));
                configs.setStagingTable(resultSet.getString("staging_table"));
                configs.setDatawarehouseTable(resultSet.getString("datawarehouse_table"));
                configs.setPeriod(resultSet.getString("period"));
                configs.setVersion(resultSet.getString("version"));
                configs.setIsActive(resultSet.getByte("is_active"));
                configs.setInsertDate(resultSet.getTimestamp("insert_date"));
                configs.setUpdateDate(resultSet.getTimestamp("update_date"));

                System.out.println("Config đã được load và gán vào biến configs!");
                return true; // Tải thành công
            } else {
                System.out.println("Không tìm thấy config với ID = " + id);
                return false; // Không tìm thấy config
            }

        } catch (SQLException e) {
            System.out.println("Lỗi khi load config: " + e.getMessage());
            e.printStackTrace();
            return false; // Lỗi khi tải config
        } finally {
            ConnectToDatabase.closeResources(connection, preparedStatement, resultSet);
        }
    }

    public static boolean loaddbConfig(int id) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            connection = ConnectToDatabase.getConnect();
            String sql = "SELECT * FROM db_configs WHERE id = ?";
            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Khởi tạo biến db_configs nếu nó đang null
                if (db_configs == null) {
                    db_configs = new db_configs();
                }

                // Gán các giá trị từ cơ sở dữ liệu cho các thuộc tính của db_configs
                db_configs.setId(resultSet.getInt("id"));
                db_configs.setDbName(resultSet.getString("db_name"));
                db_configs.setUrl(resultSet.getString("url"));
                db_configs.setUsername(resultSet.getString("username"));
                db_configs.setPassword(resultSet.getString("password"));
                db_configs.setDriverClassName(resultSet.getString("driver_class_name"));

                System.out.println("Config đã được load và gán vào biến db_configs!");
                return true; // Tải thành công
            } else {
                System.out.println("Không tìm thấy config với ID = " + id);
                return false; // Không tìm thấy config
            }

        } catch (SQLException e) {
            System.out.println("Lỗi khi load config: " + e.getMessage());
            e.printStackTrace();
            return false; // Lỗi khi tải config
        } finally {
            ConnectToDatabase.closeResources(connection, preparedStatement, resultSet);
        }
    }

//     Phương thức hỗ trợ Crawl dữ liệu từ Thế Giới Di Động
    public static List<String[]> crawlDataTGDD(String url) {
        List<String[]> productList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).get();
            Elements items = doc.select("li.item");
            for (Element item : items) {
                String productId = item.attr("data-id");
                String sku = item.attr("data-productcode");
                String detailUrl = "https://www.thegioididong.com" + item.select("a[href]").attr("href");
                String name = item.select("a").attr("data-name");
                String oldPrice = item.select("p.price-old").text();
                String discountPercent = item.select("span.percent").text();
                if (discountPercent.isEmpty()) {
                    discountPercent = "0%"; // Nếu không có giảm giá, mặc định là 0%
                } else {
                    discountPercent = discountPercent.replaceAll("[^\\d]", "");
                    if (!discountPercent.isEmpty()) {
                        discountPercent = Math.abs(Integer.parseInt(discountPercent)) + "%"; // Đảm bảo là số dương và có dấu %
                    }
                }
                String currentPrice = item.select("strong.price").text();
                String imgUrl = item.select("img[data-src]").attr("data-src");
                String rating = item.select("div.vote-txt b").text();
                rating = rating.isEmpty() ? "0" : rating;  // Nếu rating rỗng thì gán 0

                String numReviews = item.select("div.vote-txt").text().replaceAll("[^\\d]", "");
                numReviews = numReviews.isEmpty() ? "0" : numReviews;  // Nếu rating rỗng thì gán 0
                String brand = item.select("a").attr("data-brand");
                String configDetails = item.select("p").text().replace(",", ";");

                String date = LocalDate.now().toString(); // Lấy ngày hiện tại

                productList.add(new String[]{productId, sku,name , detailUrl,  oldPrice, discountPercent, currentPrice, imgUrl, rating, numReviews, brand, configDetails, date});
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi crawl dữ liệu từ TGDD: " + e.getMessage());
        }
        return productList;
    }

    // Phương thức hỗ trợ Crawl dữ liệu từ Tiki
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
                        if (discount_rate.equals("N/A") || discount_rate.isEmpty()) {
                            discount_rate = "0%"; // Nếu không có giảm giá, mặc định là 0%
                        } else {
                            discount_rate = Math.abs(Integer.parseInt(discount_rate)) + "%"; // Đảm bảo là số dương và có dấu %
                        }
                        String price = item.optString("price", "N/A");
                        String list_price = item.optString("price", "N/A");
                        String averageRating = item.optString("rating_average", "N/A");
                        String reviewCount = item.optString("review_count", "N/A");
                        String brandName = item.optString("brand_name", "Unknown");
                        String all_time_quantity_sold = item.optString("all_time_quantity_sold", "");
                        String sellerName = item.optString("seller_name", "N/A");
                        String origin = item.optString("origin", "N/A");
                        String primaryCategoryName = item.optString("primary_category_name", "N/A");
                        String short_description = getDescriptionTiki(productUrl).replaceAll("[,\\n\\r]", " ").trim();;
                        String categoryL1Name = "N/A";
                        String brand_id = item.optString("brand_id","");
                        String url_key = item.optString("url_key","");
                        String url_path = item.optString("url_path","");
                        String inventory_status = item.optInt("availability", 0) == 1 ? "availability" : "";

                        JSONArray badgesNew = item.optJSONArray("badges_new");
                        System.out.println("badgesNew"+badgesNew);
                        String stock_item_qty = "";
                        String stock_item_max_sale_qty = "";

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

                        productList.add(new String[]{id, sku, name, short_description, price, list_price ,originalPrice, discount, discount_rate, all_time_quantity_sold, averageRating, reviewCount, inventory_status, stock_item_qty, stock_item_max_sale_qty, brand_id, brandName,url_key,url_path, specifications, date});
                    }
                }
            } else {
                System.out.println("Failed to retrieve data from Tiki: " + responseCode);
            }
        }
        return productList;
    }

    // Phương thức ghi log
    public static boolean saveLog(logs log) {
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

}

