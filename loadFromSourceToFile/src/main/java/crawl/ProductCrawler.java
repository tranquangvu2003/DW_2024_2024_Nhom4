package crawl;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ProductCrawler {

    // Crawl dữ liệu từ Thế Giới Di Động
    public static List<String[]> crawlDataTGDD(String url) {
        List<String[]> productList = new ArrayList<>();
        try {
            // Sử dụng Jsoup để lấy HTML của trang
            Document doc = Jsoup.connect(url).get();

            // Phân tích HTML để lấy thông tin sản phẩm
            Elements items = doc.select("li.item");
            for (Element item : items) {
                String productId = item.attr("data-id");
                String productCode = item.attr("data-productcode");
                String detailUrl = "https://www.thegioididong.com" + item.select("a[href]").attr("href");
                String name = item.select("a").attr("data-name");
                String oldPrice = item.select("p.price-old").text();
                String discountPercent = item.select("span.percent").text();
                String currentPrice = item.select("strong.price").text();
                String imgUrl = item.select("img[data-src]").attr("data-src");
                String rating = item.select("div.vote-txt b").text();
                String numReviews = item.select("div.vote-txt").text().replaceAll("[^\\d]", "");
                String brand = item.select("a").attr("data-brand");
                String configDetails = item.select("p").text();
                configDetails = configDetails.replace(",", ";"); // Thay dấu phẩy bằng dấu chấm phẩy

                productList.add(new String[]{
                        productId, productCode, detailUrl, name, oldPrice, discountPercent, currentPrice, imgUrl, rating, numReviews, brand, configDetails});
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi crawl dữ liệu từ TGDD: " + e.getMessage());
        }
        return productList;
    }

    // Crawl dữ liệu từ Tiki
    public static List<String[]> crawlDataTiki(String apiUrl) throws IOException {
        List<String[]> productList = new ArrayList<>();
        for (int page = 1; page <= 3; page++) { // Crawl 3 trang
            URL url = new URL(apiUrl + "&page=" + page);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0");
            conn.setRequestProperty("Accept-Language", "vi-VN,vi;q=0.9,en-US;q=0.8,en;q=0.7");
            conn.setRequestProperty("Accept", "application/json");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }

                    // Parse JSON response
                    JSONObject jsonResponse = new JSONObject(response.toString());
                    JSONArray data = jsonResponse.getJSONArray("data");
                    for (int i = 0; i < data.length(); i++) {
                        JSONObject item = data.getJSONObject(i);
                        String id = String.valueOf(item.get("id"));
                        String sku = item.optString("sku", "N/A");
                        String productUrl = "https://tiki.vn/" + item.optString("url_path", "N/A");
                        String name = item.optString("name", "N/A").replaceAll("[,\\n\\r]", " ") // Xóa ký tự không mong muốn
                                .trim();
                        String originalPrice = item.optString("original_price", "N/A");
                        String discountPercent = item.optString("discount_rate", "N/A");
                        String salePrice = item.optString("price", "N/A");
                        String imgUrl = item.optString("thumbnail_url", "N/A");
                        String averageRating = item.optString("rating_average", "N/A");
                        String reviewCount = item.optString("review_count", "N/A");
                        String brandName = item.optString("brand_name", "Unknown");
                        String sellerName = item.optString("seller_name", "N/A");
                        String origin = item.optString("origin", "N/A");
                        String primaryCategoryName = item.optString("primary_category_name", "N/A");

                        // Lấy `category_l1_name` từ `visible_impression_info.amplitude`
                        String categoryL1Name = "N/A"; // Giá trị mặc định
                        JSONObject visibleInfo = item.optJSONObject("visible_impression_info");
                        if (visibleInfo != null) {
                            JSONObject amplitude = visibleInfo.optJSONObject("amplitude");
                            if (amplitude != null) {
                                categoryL1Name = amplitude.optString("category_l1_name", "N/A");
                            }
                        }

                        // Gộp các thông tin lại thành specifications
                        String specifications = String.format("%s; %s ;%s ;%s", sellerName, primaryCategoryName, categoryL1Name,origin)
                                .replaceAll("[\\n\\r]", " ") // Loại bỏ ký tự xuống dòng
                                .trim(); // Xóa khoảng trắng thừa


                        productList.add(new String[]{id, sku, productUrl, name, originalPrice, discountPercent, salePrice, imgUrl, averageRating, reviewCount, brandName, specifications});
                    }
                }
            } else {
                System.out.println("Failed to retrieve data from Tiki: " + responseCode);
            }
        }
        return productList;
    }

    // Lưu dữ liệu vào CSV
    public static void saveDataToCSV(List<String[]> data, String filePath) {
        try (FileWriter writer = new FileWriter(filePath)) {
            String[] header = {"id", "sku", "link-href", "name", "origin_price", "discount_percent", "discount_percent", "img-src", "rating", "review_count", "brand_name", "specification"};
            writer.append(String.join(",", header)).append("\n");
            for (String[] row : data) {
                writer.append(String.join(",", row)).append("\n");
            }
            System.out.println("Dữ liệu đã được lưu vào: " + filePath);
        } catch (IOException e) {
            System.out.println("Lỗi khi lưu dữ liệu vào CSV: " + e.getMessage());
        }
    }

    // Hàm main
    public static void main(String[] args) {
        // Crawl từ Thế Giới Di Động
        List<String> tgddUrls = List.of(
                "https://www.thegioididong.com/laptop-acer?itm_source=trang-nganh-hang&itm_medium=quicklink",
                "https://www.thegioididong.com/laptop-hp-compaq?itm_source=trang-nganh-hang&itm_medium=quicklink",
                "https://www.thegioididong.com/laptop-dell?itm_source=trang-nganh-hang&itm_medium=quicklink"
        );

        List<String[]> allProductsTGDD = new ArrayList<>();
        for (String url : tgddUrls) {
            System.out.println("Đang crawl dữ liệu từ TGDD: " + url);
            allProductsTGDD.addAll(crawlDataTGDD(url));
        }
        saveDataToCSV(allProductsTGDD, "LAPTOP_TGDD.csv");

        // Crawl từ Tiki
        String tikiApiUrl = "https://tiki.vn/api/v2/products?limit=40&include=advertisement,brand,specifications,price,review&aggregations=2&trackity_id=b99a8719-716f-b1cf-6233-523360a75090&brand=17825,17826&q=laptop";
        try {
            List<String[]> allProductsTiki = crawlDataTiki(tikiApiUrl);
            saveDataToCSV(allProductsTiki, "LAPTOP_TIKI.csv");
        } catch (IOException e) {
            System.out.println("Lỗi khi crawl dữ liệu từ Tiki: " + e.getMessage());
        }
    }
}
