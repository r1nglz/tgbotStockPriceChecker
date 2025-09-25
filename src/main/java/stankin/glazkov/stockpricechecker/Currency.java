package stankin.glazkov.stockpricechecker;

import lombok.Getter;
import lombok.Setter;
import me.xdrop.fuzzywuzzy.FuzzySearch;

import java.util.List;
@Getter
@Setter
public class Currency {
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
        Currency bestMatch = null;
        int bestScore = -1;

        for (Currency c : currencies) {
            if (query.length() == 3 && c.charCode.equalsIgnoreCase(query)) {
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
            if (fuzzyScore > bestScore) {
                bestScore = fuzzyScore;
                bestMatch = c;
            }

            if (bestScore >= 70) {
                return bestMatch;
            }
        }

        return null;
    }

    public static int getMaxFuzzyScore(String[] names, String query) {
        int max = 0;
        for (String name : names) {

            int score = FuzzySearch.ratio(name.toLowerCase(), query);
            if (score > max) {
                max = score;
            }

        }
        return max;
    }
}



