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

        try {
            switch (data) {
                case "help" -> sendHelp(chatId);
                case "allCurrenciesPage1" -> sendCurrenciesPage1(chatId);
                case "allCurrenciesPage2" -> sendCurrenciesPage2(chatId);
                default -> sendPrice(chatId, data);
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    private void sendHelp(String chatId) {
        String res = """
                Чтобы пользоваться ботом введите название валюты или её буквенный код (EUR, USD и т.д.).
                Также вы можете ввести название компании, цену акции которой вы хотите узнать.
                
                Данные о курсах валют берутся с сайта ЦБ РФ
                Данные о курсах акций берутся с сайта Московской биржи.
                
                Также вы можете конвертировать одну валюту в другую, написав сообщение в формате:
                {количество валюты} {символьный код валюты} в {символьный код валюты}.
                Пример: 100 USD в RUB
                """;
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
                .text("Главное меню 🌍")
                .chatId(chatId)
                .build();

        var helpButton = InlineKeyboardButton.builder()
                .text("Помощь \uD83C\uDF93")
                .callbackData("help")
                .build();

        var coursesButton = InlineKeyboardButton.builder()
                .text("Все курсы 💹")
                .callbackData("allCurrenciesPage1")
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(helpButton),
                new InlineKeyboardRow(coursesButton)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);
        message.setReplyMarkup(markup);
        telegramClient.execute(message);
    }

    private void sendCurrenciesPage1(String chatId) throws TelegramApiException {
        SendMessage message = SendMessage.builder()
                .text("Курсы валют (часть 1):")
                .chatId(chatId)
                .build();

        var usd = InlineKeyboardButton.builder()
                .text("Доллар США")
                .callbackData("usd")
                .build();
        var eur = InlineKeyboardButton.builder()
                .text("Евро")
                .callbackData("eur")
                .build();
        var aud = InlineKeyboardButton.builder()
                .text("Австралийский доллар")
                .callbackData("aud")
                .build();
        var azn = InlineKeyboardButton.builder()
                .text("Азербайджанский манат")
                .callbackData("azn")
                .build();
        var dzd = InlineKeyboardButton.builder()
                .text("Алжирский динар")
                .callbackData("dzd")
                .build();
        var thb = InlineKeyboardButton.builder()
                .text("Бат")
                .callbackData("thd")
                .build();
        var byn = InlineKeyboardButton.builder()
                .text("Белорусский рубль")
                .callbackData("byn")
                .build();
        var brl = InlineKeyboardButton.builder()
                .text("Бразильский реал")
                .callbackData("brl")
                .build();
        var krw = InlineKeyboardButton.builder()
                .text("Южнокорейская вона")
                .callbackData("krw")
                .build();
        var next = InlineKeyboardButton.builder()
                .text("Дальше ➡️")
                .callbackData("allCurrenciesPage2")
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(usd),
                new InlineKeyboardRow(eur),
                new InlineKeyboardRow(aud),
                new InlineKeyboardRow(azn),
                new InlineKeyboardRow(dzd),
                new InlineKeyboardRow(thb),
                new InlineKeyboardRow(byn),
                new InlineKeyboardRow(brl),
                new InlineKeyboardRow(krw),
                new InlineKeyboardRow(next)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);
        message.setReplyMarkup(markup);
        telegramClient.execute(message);
    }

    private void sendCurrenciesPage2(String chatId) throws TelegramApiException {
        SendMessage message = SendMessage.builder()
                .text("Курсы валют (часть 2):")
                .chatId(chatId)
                .build();

        var uah = InlineKeyboardButton.builder()
                .text("Гривна")
                .callbackData("uah")
                .build();
        var aed = InlineKeyboardButton.builder()
                .text("Дирхам ОАЭ")
                .callbackData("aed")
                .build();
        var vnd = InlineKeyboardButton.builder()
                .text("Вьетнамский донг")
                .callbackData("vnd")
                .build();
        var egp = InlineKeyboardButton.builder()
                .text("Египетский фунт")
                .callbackData("egp")
                .build();
        var pln = InlineKeyboardButton.builder()
                .text("Польский злотый")
                .callbackData("pln")
                .build();
        var jpy = InlineKeyboardButton.builder()
                .text("Японская йена")
                .callbackData("jpy")
                .build();
        var inr = InlineKeyboardButton.builder()
                .text("Индийская рупия")
                .callbackData("inr")
                .build();
        var gbp = InlineKeyboardButton.builder()
                .text("Фунт стерлингов")
                .callbackData("gbp")
                .build();
        var chf = InlineKeyboardButton.builder()
                .text("Швейцарский франк")
                .callbackData("chf")
                .build();
        var back = InlineKeyboardButton.builder()
                .text("⬅️ Назад")
                .callbackData("allCurrenciesPage1")
                .build();

        List<InlineKeyboardRow> keyboardRows = List.of(
                new InlineKeyboardRow(uah),
                new InlineKeyboardRow(aed),
                new InlineKeyboardRow(vnd),
                new InlineKeyboardRow(egp),
                new InlineKeyboardRow(pln),
                new InlineKeyboardRow(jpy),
                new InlineKeyboardRow(inr),
                new InlineKeyboardRow(gbp),
                new InlineKeyboardRow(chf),
                new InlineKeyboardRow(back)
        );

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup(keyboardRows);
        message.setReplyMarkup(markup);
        telegramClient.execute(message);
    }

    private void sendPrice(String chatId, String data) {
        for (int i = 0; i < PriceParser.listOfValues.size(); i++) {
            if (PriceParser.listOfValues.get(i).getCharCode().equalsIgnoreCase(data)) {
                Currency cur = PriceParser.listOfValues.get(i);
                SendMessage message = SendMessage.builder()
                        .text(cur.getHowMuch() + " " + cur.getCharCode().toUpperCase() + " (" + cur.getFullname() + ") = " + cur.getPrice() + " RUB")
                        .chatId(chatId)
                        .build();
                try {
                    telegramClient.execute(message);
                } catch (TelegramApiException e) {
                    throw new RuntimeException(e);
                }
                break;
            }
        }
    }
}