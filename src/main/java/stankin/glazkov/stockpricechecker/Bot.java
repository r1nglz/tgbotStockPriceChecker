package stankin.glazkov.stockpricechecker;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.longpolling.interfaces.LongPollingUpdateConsumer;
import org.telegram.telegrambots.longpolling.starter.SpringLongPollingBot;

@Component
public class Bot implements SpringLongPollingBot {
    private final UpdateConsumer updateConsumer;

    public Bot(UpdateConsumer updateConsumer) {
        this.updateConsumer = updateConsumer;
    }

    @Override
    public String getBotToken() {
        return "8426320186:AAESpPcbOKAR7DMjk6rJRPMmZ5fwj0a3vzM";
    }

    @Override
    public LongPollingUpdateConsumer getUpdatesConsumer() {
        return updateConsumer;
    }
}
