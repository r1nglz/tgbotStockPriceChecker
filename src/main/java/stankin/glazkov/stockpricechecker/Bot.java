package stankin.glazkov.stockpricechecker;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

@Component
public class Bot implements SpringLongPollingBot {
    String token = System.getenv("TELEGRAM_TOKEN");
    private final UpdateConsumer updateConsumer;

    public Bot(UpdateConsumer updateConsumer) {
        this.updateConsumer = updateConsumer;
    }


    @Override
    public String getBotToken() {
        return token;
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updateConsumer;
    }
}
