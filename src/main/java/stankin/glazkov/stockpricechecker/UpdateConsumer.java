package stankin.glazkov.stockpricechecker;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.util.List;


@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;

    public UpdateConsumer() {
        this.telegramClient = new OkHttpTelegramClient("8426320186:AAESpPcbOKAR7DMjk6rJRPMmZ5fwj0a3vzM");
    }

    @Override
    public void consume(Update update) {
        long chatId = 0;
        if (update.hasMessage()) {
            String messageText = update.getMessage().getText();
            chatId = update.getMessage().getChatId();
            try {
                PriceParser.getRubToOthers();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            if (messageText.equals("/start")) {
                try {
                    sendMainMenu(chatId);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (messageText.equals("доллар")) {
                SendMessage message = SendMessage.builder()
                        .text("1 " + "USD" + " (" + "доллар США" + ") = " + PriceParser.valuesOfMoney1.get("usd"))
                        .chatId(chatId)
                        .build();
                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else if (PriceParser.valuesOfMoney1.containsKey(messageText.toLowerCase())) {
                SendMessage message = SendMessage.builder()
                        .text("1 " + messageText.toUpperCase() + " (" + PriceParser.valuesOfMoney2.get(messageText.toLowerCase()) + ") = " + PriceParser.valuesOfMoney1.get(messageText.toLowerCase()))
                        .chatId(chatId)
                        .build();
                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }

            } else if (PriceParser.valuesOfMoney2.containsValue(messageText.toLowerCase())) {

                String codeValue, nameValue;
                nameValue = messageText.toLowerCase();
                codeValue = PriceParser.valuesOfMoney2.inverse().get(nameValue);
                SendMessage message = SendMessage.builder()
                        .text("1 " + codeValue.toUpperCase() + " (" + nameValue + ") = " + PriceParser.valuesOfMoney1.get(codeValue))
                        .chatId(chatId)
                        .build();
                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            }


    } else if(update.hasCallbackQuery())

    {
        try {
            handleCallbackQuery(update.getCallbackQuery());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}

private void handleCallbackQuery(CallbackQuery callbackQuery) throws IOException {
    var data = callbackQuery.getData();
    String chatId = callbackQuery.getMessage().getChatId().toString();
    var user = callbackQuery.getFrom();
    switch (data) {
        case "help" -> sendHelp(chatId);
        default -> new SendMessage(chatId, "Неизвестная команда");
    }
}
private void sendHelp(String chatId) {
    String res = "Чтобы пользоваться ботом введите название валюты или её буквенный код (EUR, USD и т.д.).\n" +
            "Также вы можете ввести название компании, цену акции которой вы хотите узнать.";
    SendMessage message = SendMessage.builder()
            .text(res)
            .chatId(chatId)
            .build();
    try {
        telegramClient.execute(message);
    } catch (TelegramApiException e) {
        throw new RuntimeException(e);
    }
}


private void sendMainMenu(long chatId) throws TelegramApiException {
    SendMessage message = SendMessage.builder()
            .text("Узнать как пользоваться ботом \uD83D\uDC47")
            .chatId(chatId)
            .build();
    var button1 = InlineKeyboardButton.builder()
            .text("Помощь\uD83C\uDF93")
            .callbackData("help")
            .build();

    List<InlineKeyboardRow> keyboardRows = List.of(
            new InlineKeyboardRow(button1)
    );

    InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);
    message.setReplyMarkup(markup);
    telegramClient.execute(message);
}
}
