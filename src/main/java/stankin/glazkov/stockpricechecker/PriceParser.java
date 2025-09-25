package stankin.glazkov.stockpricechecker;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;


@Component
public class PriceParser {
    static BiMap<String, String> valuesOfMoney1 = HashBiMap.create();
    static BiMap<String, String> valuesOfMoney2 = HashBiMap.create();

    static void getRubToOthers() throws IOException {
        Path path = Path.of("src/main/parsejsons/", "cbrf.json");
        try {
            var document = Jsoup.connect("https://www.cbr.ru/currency_base/daily/").get();
            Elements rows = document.select("table tr");
            for (int i = 1; i < rows.size(); i++) {
                Element row = rows.get(i);
                Elements cols = row.select("td");
                String numCode = cols.get(0).text();
                String charCode = cols.get(1).text();
                String howMuch = cols.get(2).text();
                String name = cols.get(3).text();
                String price = cols.get(4).text();

                valuesOfMoney1.put(charCode.toLowerCase(), price + " RUB");
                valuesOfMoney2.put(charCode.toLowerCase(), name.toLowerCase());

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    static String getStockPrices() {
        return "12";
    }


}
