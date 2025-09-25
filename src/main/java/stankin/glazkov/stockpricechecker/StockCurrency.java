package stankin.glazkov.stockpricechecker;

import lombok.Getter;
import lombok.Setter;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.util.List;

@Setter
@Getter
public class StockCurrency {
    private String PREVPRICE;
    private String SECID;
    private String SHORTNAME;
    private String LOTSIZE;
    private String SECNAME;

    StockCurrency(String secid, String prevprice, String shortname, String lotsize, String secname) {
        this.LOTSIZE = lotsize;
        this.PREVPRICE = prevprice;
        this.SECID = secid;
        this.SECNAME = secname;
        this.SHORTNAME = shortname;
    }

    public static StockCurrency FindStockCurrency(List<StockCurrency> currencies, String query) {
        for (StockCurrency s : currencies) {
            if (s.SECID.equalsIgnoreCase(query)) {
                return s;
            }
            if (s.SHORTNAME.equalsIgnoreCase(query)) {
                return s;
            }
            if (s.SECNAME.equalsIgnoreCase(query)) {
                return s;
            }
            int fuzzyScore = Math.max(
                    Math.max(
                            FuzzySearch.ratio(s.SECID.toLowerCase(), query.toLowerCase()),
                            FuzzySearch.ratio(s.SHORTNAME.toLowerCase(), query.toLowerCase())
                    ),
                    FuzzySearch.ratio(s.SECNAME.toLowerCase(), query.toLowerCase())
            );

            if (fuzzyScore >= 67) {
                return s;
            }
        }

        return null;
    }
}




