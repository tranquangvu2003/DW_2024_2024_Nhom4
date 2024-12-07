import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class DataIdCrawler {
    public static void main(String[] args) {
        String url = "https://www.thegioididong.com/laptop-acer?itm_source=trang-nganh-hang&itm_medium=quicklink";
        try {
            // Gọi phương thức lấy data-id
            crawlDataId(url);
        } catch (IOException e) {
            System.out.println("Error fetching the URL: " + e.getMessage());
        }
    }

    public static void crawlDataId(String url) throws IOException {
        // Tải nội dung trang web
        Document document = Jsoup.connect(url).get();

        // Chọn tất cả các thẻ <li> có chứa thuộc tính data-id
        Elements elements = document.select("li[data-id]");

        // Duyệt qua từng phần tử và in giá trị data-id
        for (Element element : elements) {
            String dataId = element.attr("data-id");
            System.out.println("Found data-id: " + dataId);
        }
    }
}
