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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {
    private final TelegramClient telegramClient;

    public UpdateConsumer() {
        String token = System.getenv("TELEGRAM_TOKEN");
        this.telegramClient = new OkHttpTelegramClient(token);
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
            } else {
                try {
                    PriceParser.getRubToOthers();
                    PriceParser.getStockPrices();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                Currency cur = Currency.findCurrency(PriceParser.listOfValues, messageText);
                StockCurrency curr = StockCurrency.FindStockCurrency(PriceParser.listOfStocks, messageText);
                if (cur != null) {
                    SendMessage message = SendMessage.builder()
                            .text(cur.howMuch + " " + cur.charCode.toUpperCase() + " (" + cur.fullname + ") = " + cur.price + " RUB")
                            .chatId(chatId)
                            .build();
                    try {
                        telegramClient.execute(message);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                } else if (curr != null) {
                    SendMessage message = SendMessage.builder()
                            .text(curr.LOTSIZE + " Акция " + curr.SHORTNAME + " (" + curr.SECNAME + ") = " + curr.PREVPRICE + " RUB")
                            .chatId(chatId)
                            .build();
                    try {
                        telegramClient.execute(message);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }


                }
                else{
                    SendMessage message = SendMessage.builder()
                            .text("Неизвестная команда")
                            .chatId(chatId)
                            .build();
                    try {
                        telegramClient.execute(message);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }

            }


        } else if (update.hasCallbackQuery()) {
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
        String res = "Чтобы пользоваться ботом введите название валюты или её буквенный код (EUR, USD и т.д.).\n\n" +
                "Также вы можете ввести название компании, цену акции которой вы хотите узнать.\n\n\nДанные о курсах валют берутся с сайта ЦБ РФ\nДанные о курсах акций берутся с сайта московской Биржи";
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
