package helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.List;

public class JsonToUrls {

    public static List<String> parseTikiApiUrls(String json) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(json);

        ArrayList<String> tikiApiUrls = new ArrayList<>();
        for (JsonNode urlNode : rootNode.get("tikiApiUrls")) {
            tikiApiUrls.add(urlNode.asText());
        }

        return tikiApiUrls;
    }

    public static void main(String[] args) {
        try {
            String json = "{\n" +
                    "  \"tikiApiUrls\": [\n" +
                    "    \"https://tiki.vn/api/v2/products?limit=40&include=advertisement,brand,specifications,price,review&aggregations=2&trackity_id=b99a8719-716f-b1cf-6233-523360a75090&brand=17825,17826&q=laptop\",\n" +
                    "    \"https://tiki.vn/api/personalish/v1/blocks/listings?limit=40&include=advertisement&aggregations=2&version=home-persionalized&trackity_id=b99a8719-716f-b1cf-6233-523360a75090&category=5584&page=1&urlKey=laptop-gaming\"\n" +
                    "  ]\n" +
                    "}";

            List<String> tikiApiUrls = parseTikiApiUrls(json);

            System.out.println("Tiki API URLs: " + tikiApiUrls);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
