package org.hsse.news.bot;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;


@Component
public class NewsBotHandlers {
    private final static String START_COMMAND = "/start";
    private final static String MENU_COMMAND = "/menu";
    private final static String WEBSITES_MENU_COMMAND = "/websites";
    private static final String ARTICLES_MENU_COMMAND = "/articles";
    private final static String TOPICS_MENU_COMMAND = "/topics";
    private final static String LIST_SUBBED_TOPICS_COMMAND = "/unblocked-topics";
    private final static String LIST_NOT_SUBBED_TOPICS_COMMAND = "/blocked-topics";
    private final static String VIEW_TOPIC_COMMAND = "/view-topic";
    private final static String SUB_TOPIC_COMMAND = "/unblock-topic";
    private final static String UNSUB_TOPIC_COMMAND = "/block-topic";
    private final static String SUB_CUSTOM_TOPIC_COMMAND = "/add-custom-topic";
    private final static String START_INFO = "/start-info";
    private final String startMessage =  "Привет! 👋\n\n" +
            "Этот бот поможет тебе быть в курсе свежих новостей из любимых источников! 📰✨\n\n" +
            "Как это работает?\n" +
            "1. Добавляй источники — выбирай сайты и темы, которые тебе интересны. \uD83D\uDD0D\n" +
            "2. Получай подборку статей — бот будет присылать актуальные новости в удобном формате. \uD83D\uDCE2\n" +
            "3. Настраивай под себя — меняй список источников в любой момент. ⚙️\n\n" +
            "Больше не нужно переключаться между сайтами — все важное в одном месте! \uD83D\uDCA1\n\n" +
            "Начни сейчас — добавь первый источник! \uD83D\uDD17";

    private final static String BACK_TEXT = "Назад";

    private final StubDataProvider dataProvider;

    public NewsBotHandlers(final StubDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    private static InlineKeyboardMarkup mainMenuKeyboard() {
        return new InlineKeyboardMarkup(List.of(
                List.of(InlineKeyboardButton.builder()
                        .text("Статьи")
                        .callbackData(ARTICLES_MENU_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text("Источники")
                        .callbackData(WEBSITES_MENU_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text("Темы")
                        .callbackData(TOPICS_MENU_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text("Информация")
                        .callbackData(START_INFO).build())
        ));
    }

    @BotMapping(START_COMMAND)
    public Message start(final ChatId chatId) {
        dataProvider.registerUser(chatId.value());
        return Message.builder()
                .text(startMessage)
                .keyboard(mainMenuKeyboard()).build();
    }

    @BotMapping(MENU_COMMAND)
    public Message menu() {
        return Message.builder().text("Меню").keyboard(mainMenuKeyboard()).build();
    }

    @BotMapping(START_INFO)
    public Message sendStartInfo() {
        return Message.builder()
                .text(startMessage)
                .keyboard(mainMenuKeyboard()).build();
    }
}