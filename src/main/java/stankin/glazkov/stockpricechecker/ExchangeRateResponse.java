package stankin.glazkov.stockpricechecker;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.stereotype.Component;

import java.util.Map;

public class ExchangeRateResponse {
    private String result;
    @JsonProperty("conversion_rates")
    private Map<String, Double> conversionRates;


    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public Map<String, Double> getConversionRates() {
        return conversionRates;
    }

    public void setConversionRates(Map<String, Double> conversionRates) {
        this.conversionRates = conversionRates;
    }
}