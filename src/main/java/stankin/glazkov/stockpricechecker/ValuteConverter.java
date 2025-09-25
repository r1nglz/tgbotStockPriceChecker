package stankin.glazkov.stockpricechecker;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

public class ValuteConverter {

    public static double convertCurrency(String from, String to, double amount) {
        String token = System.getenv("EXCHANGE_API_KEY");

        String url = "https://v6.exchangerate-api.com/v6/" + token + "/latest/" + from;

        OkHttpClient client = new OkHttpClient();
        ObjectMapper mapper = new ObjectMapper();

        try {
            Request request = new Request.Builder().url(url).build();
            Response response = client.newCall(request).execute();
            assert response.body() != null;
            String json = response.body().string();

            ExchangeRateResponse exchangeRateResponse = mapper.readValue(json, ExchangeRateResponse.class);

            if (!"success".equalsIgnoreCase(exchangeRateResponse.getResult())) {
                throw new RuntimeException("Ошибка API: " + exchangeRateResponse.getResult());
            }

            Double rate = exchangeRateResponse.getConversionRates().get(to.toUpperCase());
            if (rate == null) {
                throw new RuntimeException("Конверсия " + from + " -> " + to + " недоступна");
            }

            return amount * rate;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}