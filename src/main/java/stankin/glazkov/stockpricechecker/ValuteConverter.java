package stankin.glazkov.stockpricechecker;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import stankin.glazkov.stockpricechecker.ExchangeRateResponse;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;
import java.util.Map;

public class ValuteConverter {

    private static String BASE_URL = "https://v6.exchangerate-api.com/v6/c8fc2ba66883c9f2ea8e0568/latest/RUB";
    private OkHttpClient client = new OkHttpClient();
    private ObjectMapper objectMapper = new ObjectMapper();

}