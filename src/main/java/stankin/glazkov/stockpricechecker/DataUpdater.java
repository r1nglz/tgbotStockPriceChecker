package stankin.glazkov.stockpricechecker;

import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@EnableScheduling
public class DataUpdater {

    @Scheduled(fixedRate = 300000)
    public void refreshData() {
        try {
            PriceParser.getRubToOthers();
            PriceParser.getStockPrices();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
