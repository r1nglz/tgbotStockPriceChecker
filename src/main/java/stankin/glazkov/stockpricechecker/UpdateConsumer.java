package stankin.glazkov.stockpricechecker;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.client.okhttp.OkHttpTelegramClient;
import org.telegram.telegrambots.longpolling.util.LongPollingSingleThreadUpdateConsumer;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;

import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.generics.TelegramClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class UpdateConsumer implements LongPollingSingleThreadUpdateConsumer {

    private final TelegramClient telegramClient;

    private void registerBotCommands() {
        List<BotCommand> commandList = new ArrayList<>();
        commandList.add(new BotCommand("/start", "Запуск бота"));

        try {
            telegramClient.execute(
                    new SetMyCommands(commandList, new BotCommandScopeDefault(), null)
            );
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    public UpdateConsumer() {
        String token = System.getenv("TELEGRAM_TOKEN");
        this.telegramClient = new OkHttpTelegramClient(token);
        registerBotCommands();
    }

    @Override
    public void consume(Update update) {
        long chatId = 0;
        if (update.hasMessage()) {
            String messageText = update.getMessage().getText();
            chatId = update.getMessage().getChatId();
            if (messageText.equals("/start")) {
                try {
                    sendMainMenu(chatId);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
            } else {


                Currency cur = Currency.findCurrency(PriceParser.listOfValues, messageText);
                StockCurrency curr = StockCurrency.FindStockCurrency(PriceParser.listOfStocks, messageText);
                if (cur != null) {
                    SendMessage message = SendMessage.builder()
                            .text(cur.getHowMuch() + " " + cur.getCharCode().toUpperCase() + " (" + cur.getFullname() + ") = " + cur.getPrice() + " RUB")
                            .chatId(chatId)
                            .build();
                    try {
                        telegramClient.execute(message);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                } else if (curr != null) {
                    SendMessage message = SendMessage.builder()
                            .text(curr.getLOTSIZE() + " Акция " + curr.getSHORTNAME() + " (" + curr.getSECNAME() + ") = " + curr.getPREVPRICE() + " RUB")
                            .chatId(chatId)
                            .build();
                    try {
                        telegramClient.execute(message);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }


                } else if (messageText.matches("[1-9][0-9]* [a-zA-Z]{3} [В|в] [a-zA-Z]{3}")) {
                    String[] parts = messageText.split(" ");
                    double amount = Double.parseDouble(parts[0]);
                    String from = parts[1].toUpperCase();
                    String to = parts[3].toUpperCase();

                    try {
                        double result = ValuteConverter.convertCurrency(from, to, amount);
                        SendMessage message = SendMessage.builder()
                                .chatId(chatId)
                                .text(amount + " " + from + " = " + result + " " + to)
                                .build();
                        telegramClient.execute(message);
                    } catch (Exception e) {
                        SendMessage message = SendMessage.builder()
                                .chatId(chatId)
                                .text("Ошибка при конвертации: " + e.getMessage())
                                .build();
                        try {
                            telegramClient.execute(message);
                        } catch (TelegramApiException ex) {
                            throw new RuntimeException(ex);
                        }
                    }

                } else {
                    SendMessage message = SendMessage.builder()
                            .text("Я не понимаю вас")
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

    private void handleCallbackQuery(@NotNull CallbackQuery callbackQuery) throws IOException {
        var data = callbackQuery.getData();
        String chatId = callbackQuery.getMessage().getChatId().toString();
        var user = callbackQuery.getFrom();
        switch (data) {
            case "help" -> sendHelp(chatId);
            default -> {
                SendMessage message = SendMessage.builder()
                        .chatId(chatId)
                        .text("Неизвестная команда")
                        .build();
                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void sendHelp(String chatId) {
        String res = "Чтобы пользоваться ботом введите название валюты или её буквенный код (EUR, USD и т.д.).\n" +
                "Также вы можете ввести название компании, цену акции которой вы хотите узнать.\n\nДанные о курсах валют берутся с сайта ЦБ РФ\nДанные о курсах акций берутся с сайта московской Биржи" +
                "\n\nТакже вы можете конвертировать одну валюту в другую, написав сообщение в формате {количество валюты} {символьный код валюты из которой хотите перевести} В {символьный код валюты в которую хотите перевести}. К примеру: 100 USD в RUB";
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
