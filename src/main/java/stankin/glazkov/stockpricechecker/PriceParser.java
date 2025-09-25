package stankin.glazkov.stockpricechecker;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.io.IOException;
import java.lang.reflect.Array;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.*;

class Currency {
    String numCode, charCode, howMuch, price, fullname;
    String[] name;

    Currency(String numCode, String charCode, String howMuch, String[] name, String price, String fullname) {
        this.numCode = numCode.toLowerCase();
        this.charCode = charCode.toLowerCase();
        this.howMuch = howMuch.toLowerCase();
        this.name = name;
        this.price = price.toLowerCase();
        this.fullname = fullname;
    }

    public static Currency findCurrency(List<Currency> currencies, String query) {


        for (Currency c : currencies) {
            if (c.charCode.equalsIgnoreCase(query)) {
                return c;
            }


            for (String name : c.name) {
                if (name.toLowerCase().contains(query)) {
                    return c;
                }
            }


            int fuzzyScore = Math.max(
                    FuzzySearch.ratio(c.charCode.toLowerCase(), query),
                    getMaxFuzzyScore(c.name, query)
            );

            if (fuzzyScore >= 65) {
                return c;
            }
        }

        return null;
    }

    private static int getMaxFuzzyScore(String[] names, String query) {
        int max = 0;
        for (String name : names) {

            int score = FuzzySearch.ratio(name.toLowerCase(), query);
            if (score > max) max = score;

        }
        return max;
    }
}


@Component
public class PriceParser {
    static ArrayList<Currency> listOfValues = new ArrayList<>();


    static void getRubToOthers() throws IOException {
        try {
            Document document = Jsoup.connect("https://www.cbr.ru/currency_base/daily/").get();
            Elements rows = document.select("table tr");
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

    static void getStockPrices() {
        try {
            Document document = Jsoup.connect("https://iss.moex.com/iss/engines/stock/markets/shares/securities").get();
            Elements rows = document.select("tbody tr");
            for (int i = 1; i < rows.size(); i++) {
                Element row = rows.get(i);
                Elements cols = row.select("tr");
                String secId = cols.get(0).text();
                System.out.println(secId);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {
        getStockPrices();
    }
}
