import org.json.JSONArray;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ProductCrawl {

    // Crawl dữ liệu từ Thế Giới Di Động
    public static List<String[]> crawlDataTGDD(String url) {
        List<String[]> productList = new ArrayList<>();
        try {
            Document doc = Jsoup.connect(url).get();
            Elements items = doc.select("li.item");
            for (Element item : items) {
                String productId = item.attr("data-id");
                String productCode = item.attr("data-productcode");
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

                productList.add(new String[]{
                        productId, productCode, detailUrl, name, oldPrice, discountPercent, currentPrice, imgUrl, rating, numReviews, brand, configDetails, date
                });
            }
        } catch (Exception e) {
            System.out.println("Lỗi khi crawl dữ liệu từ TGDD: " + e.getMessage());
        }
        return productList;
    }

    // Crawl dữ liệu từ Tiki
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
                        String discountPercent = item.optString("discount_rate", "N/A");
                        if (discountPercent.equals("N/A") || discountPercent.isEmpty()) {
                            discountPercent = "0%"; // Nếu không có giảm giá, mặc định là 0%
                        } else {
                            discountPercent = Math.abs(Integer.parseInt(discountPercent)) + "%"; // Đảm bảo là số dương và có dấu %
                        }
                        String salePrice = item.optString("price", "N/A");
                        String imgUrl = item.optString("thumbnail_url", "N/A");
                        String averageRating = item.optString("rating_average", "N/A");
                        String reviewCount = item.optString("review_count", "N/A");
                        String brandName = item.optString("brand_name", "Unknown");
                        String sellerName = item.optString("seller_name", "N/A");
                        String origin = item.optString("origin", "N/A");
                        String primaryCategoryName = item.optString("primary_category_name", "N/A");

                        String categoryL1Name = "N/A";
                        JSONObject visibleInfo = item.optJSONObject("visible_impression_info");
                        if (visibleInfo != null) {
                            JSONObject amplitude = visibleInfo.optJSONObject("amplitude");
                            if (amplitude != null) {
                                categoryL1Name = amplitude.optString("category_l1_name", "N/A");
                            }
                        }

                        String specifications = String.format("%s; %s ;%s ;%s", sellerName, primaryCategoryName, categoryL1Name, origin).replaceAll("[\\n\\r]", " ").trim();

                        String date = LocalDate.now().toString(); // Lấy ngày hiện tại

                        productList.add(new String[]{id, sku, productUrl, name, originalPrice, discountPercent, salePrice, imgUrl, averageRating, reviewCount, brandName, specifications, date});
                    }
                }
            } else {
                System.out.println("Failed to retrieve data from Tiki: " + responseCode);
            }
        }
        return productList;
    }

    // Lưu dữ liệu vào file CSV
    public static void saveDataToCSV(List<String[]> data) {
        // Lấy thời gian hiện tại (bao gồm giờ, phút, giây)
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
        String currentTime = dtf.format(LocalDateTime.now());
        String filePath = "Products_" + currentTime + ".csv";  // Tên file chứa thời gian hiện tại

        try (FileWriter writer = new FileWriter(filePath)) {
            String[] header = {"id", "sku", "link-href", "name", "origin_price", "discount_percent", "sale_price", "img-src", "rating", "review_count", "brand_name", "specification", "date"};

            // Ghi header vào file CSV
            writer.append(String.join(",", header));
            writer.append("\n");

            // Ghi dữ liệu vào file CSV
            for (String[] rowData : data) {
                writer.append(String.join(",", rowData));
                writer.append("\n");
            }

            System.out.println("Dữ liệu đã được lưu vào: " + filePath);
        } catch (IOException e) {
            System.out.println("Lỗi khi lưu dữ liệu vào CSV: " + e.getMessage());
        }
    }

    // Hàm main
    public static void main(String[] args) {
        List<String> tgddUrls = List.of(
                "https://www.thegioididong.com/laptop-acer?itm_source=trang-nganh-hang&itm_medium=quicklink",
                "https://www.thegioididong.com/laptop-hp-compaq?itm_source=trang-nganh-hang&itm_medium=quicklink",
                "https://www.thegioididong.com/laptop-dell?itm_source=trang-nganh-hang&itm_medium=quicklink"
        );

        List<String[]> allProducts = new ArrayList<>();
        for (String url : tgddUrls) {
            allProducts.addAll(crawlDataTGDD(url));
        }

        String tikiApiUrl = "https://tiki.vn/api/v2/products?limit=40&include=advertisement,brand,specifications,price,review&aggregations=2&trackity_id=b99a8719-716f-b1cf-6233-523360a75090&brand=17825,17826&q=laptop";
        try {
            allProducts.addAll(crawlDataTiki(tikiApiUrl));
        } catch (IOException e) {
            System.out.println("Lỗi khi crawl dữ liệu từ Tiki: " + e.getMessage());
        }

        saveDataToCSV(allProducts);  // Lưu dữ liệu vào CSV
    }
}
