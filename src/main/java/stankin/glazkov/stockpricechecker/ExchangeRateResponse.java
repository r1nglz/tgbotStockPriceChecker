package stankin.glazkov.stockpricechecker;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Map;
@JsonIgnoreProperties(ignoreUnknown = true)
@Setter
@Getter
public class ExchangeRateResponse {
    private String result;
    @JsonProperty("conversion_rates")
    private Map<String, Double> conversionRates;


}