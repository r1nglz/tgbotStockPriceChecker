package stankin.glazkov.stockpricechecker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.io.IOException;
import java.util.*;



public class PriceParser {

    static ArrayList<Currency> listOfValues = new ArrayList<>();
    static ArrayList<StockCurrency> listOfStocks = new ArrayList<>();

    static void getRubToOthers() {
        try {
            Document document = Jsoup.connect("https://www.cbr.ru/currency_base/daily/").get();
            Elements rows = document.select("table tr");
            listOfValues.clear();
            for (int i = 1; i < rows.size(); i++) {
                Element row = rows.get(i);
                Elements cols = row.select("td");
                Currency cur = new Currency(cols.get(0).text(), cols.get(1).text(), cols.get(2).text(), cols.get(3).text().split(" "), cols.get(4).text(), cols.get(3).text());
                listOfValues.add(cur);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static int findIndex(JsonNode columns, String name) {
        for (int i = 0; i < columns.size(); i++) {
            if (columns.get(i).asText().equals(name)) {
                return i;
            }
        }
        throw new IllegalArgumentException();
    }

    static void getStockPrices() {

        String url = "https://iss.moex.com/iss/engines/stock/markets/shares/securities.json";
        try {
            OkHttpClient client = new OkHttpClient();
            ObjectMapper mapper = new ObjectMapper();
            Request request = new Request.Builder().url(url).build();
            Response response;
            response = client.newCall(request).execute();
            assert response.body() != null;
            JsonNode root = mapper.readTree(response.body().string());
            JsonNode securities = root.path("securities");
            JsonNode columns = securities.get("columns");
            JsonNode data = securities.get("data");
            int indxOfSecId = findIndex(columns, "SECID");
            int indxOfShortName = findIndex(columns, "SHORTNAME");
            int indxOfPrevPrice = findIndex(columns, "PREVPRICE");
            int indxOfLotSize = findIndex(columns, "LOTSIZE");
            int indxOfSecName = findIndex(columns, "SECNAME");
            listOfStocks.clear();
            for (JsonNode row : data) {
                String secId = row.get(indxOfSecId).asText();
                String shortName = row.get(indxOfShortName).asText();
                String prevPrice = row.get(indxOfPrevPrice).asText();
                String lotSize = row.get(indxOfLotSize).asText();
                String secName = row.get(indxOfSecName).asText();
                StockCurrency stock = new StockCurrency(secId, prevPrice, shortName, lotSize, secName);
                listOfStocks.add(stock);
            }


        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}



