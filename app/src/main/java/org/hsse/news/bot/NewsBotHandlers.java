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

    @BotMapping(START_COMMAND)
    public Message start(final ChatId chatId) {
        return Message.builder()
                .text("Привет! Добавь источники и ты сможешь смотреть ленту новостей в этом боте!")
                .keyboard(mainMenuKeyboard()).build();
    }

    @BotMapping(MENU_COMMAND)
    public Message menu() {
        return Message.builder()
                .text("Главное меню")
                .keyboard(mainMenuKeyboard()).build();
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
                        .callbackData(TOPICS_MENU_COMMAND).build())
        ));
    }

    @BotMapping("/test-send")
    public void testSend(final TelegramBot bot, final ChatId chatId) {
        bot.sendMessage(chatId, Message.builder().text("test").build());
    }
}