package org.hsse.news.bot;

import org.hsse.news.database.topic.models.TopicDto;
import org.hsse.news.database.topic.models.TopicId;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
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
                        .callbackData(TOPICS_MENU_COMMAND).build())
        ));
    }

    @BotMapping(START_COMMAND)
    public Message start() {
        return Message.builder()
                .text("Привет! Добавь источники и ты сможешь смотреть ленту новостей в этом боте! ")
                .keyboard(mainMenuKeyboard()).build();
    }

    @BotMapping(MENU_COMMAND)
    public Message menu() {
        return Message.builder().text("Меню").keyboard(mainMenuKeyboard()).build();
    }

    @BotMapping("/test-send")
    public void testSend(final TelegramBot bot, final ChatId chatId) {
        bot.sendMessage(chatId, Message.builder().text("test").build());
    }

    private InlineKeyboardMarkup topicsMenuKeyboard() {
        return new InlineKeyboardMarkup(List.of(
                List.of(InlineKeyboardButton.builder()
                        .text("Подписан")
                        .callbackData(LIST_SUBBED_TOPICS_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text("Не подписан")
                        .callbackData(LIST_NOT_SUBBED_TOPICS_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text("Добавить...")
                        .callbackData(SUB_CUSTOM_TOPIC_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text(BACK_TEXT)
                        .callbackData("/menu").build())));
    }

    @BotMapping(TOPICS_MENU_COMMAND)
    public Message topicsMenu() {
        return Message.builder().text("Темы").keyboard(topicsMenuKeyboard()).build();
    }

    private Message topicListMessage(final String text, final List<TopicDto> topics) {
        final List<InlineKeyboardButton> buttons = new ArrayList<>(
                topics.stream().map(
                        topic -> InlineKeyboardButton.builder()
                                .text(topic.description())
                                .callbackData(VIEW_TOPIC_COMMAND + " " + topic.id())
                                .build()).toList());
        buttons.add(InlineKeyboardButton.builder()
                .text(BACK_TEXT)
                .callbackData(TOPICS_MENU_COMMAND).build());

        return Message.builder().text(text).verticalKeyboard(buttons).build();
    }

    @BotMapping(LIST_SUBBED_TOPICS_COMMAND)
    public Message listSubbedTopics() {
        return topicListMessage("Подписки:", dataProvider.getSubbedTopics());
    }

    @BotMapping(LIST_NOT_SUBBED_TOPICS_COMMAND)
    public Message listUnsubbedTopics() {
        return topicListMessage("Вы не подписаны на:", dataProvider.getSubbedTopics());
    }

    private Message viewTopicMessage(final TopicId id) {
        final TopicDto topic = dataProvider.findTopic(id).orElseThrow();
        final String subCommandName = dataProvider.isSubbed(id) ? "Отписаться" : "Подписаться";
        final String subCommand = dataProvider.isSubbed(id) ? UNSUB_TOPIC_COMMAND : SUB_TOPIC_COMMAND;

        return Message.builder().text(topic.description()).verticalKeyboard(List.of(
                InlineKeyboardButton.builder()
                        .text(subCommandName)
                        .callbackData(subCommand + " " + id).build(),
                InlineKeyboardButton.builder()
                        .text(BACK_TEXT)
                        .callbackData(dataProvider.isSubbed(id)
                                ? LIST_SUBBED_TOPICS_COMMAND
                                : LIST_NOT_SUBBED_TOPICS_COMMAND)
                        .build())).build();
    }

    @BotMapping(VIEW_TOPIC_COMMAND)
    public Message viewTopic(final TopicId id) {
        return viewTopicMessage(id);
    }

    @BotMapping(SUB_TOPIC_COMMAND)
    public Message subTopic(final TopicId id) {
        return viewTopicMessage(id);
    }

    @BotMapping(UNSUB_TOPIC_COMMAND)
    public Message unsubTopic(final TopicId id) {
        return viewTopicMessage(id);
    }

    @BotMapping(SUB_CUSTOM_TOPIC_COMMAND)
    public Message subCustomTopic() {
        return Message.builder().text("Введите название темы:").singleButton(
                        InlineKeyboardButton.builder()
                                .text("Отмена")
                                .callbackData(WEBSITES_MENU_COMMAND).build())
                .onNextMessage(text -> Message.builder()
                        .text("Тема " + text + " добавлена")
                        .keyboard(topicsMenuKeyboard()).build()).build();
    }

}
