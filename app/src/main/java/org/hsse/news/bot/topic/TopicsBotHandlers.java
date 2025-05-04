package org.hsse.news.bot.topic;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hsse.news.api.schemas.shared.TopicInfo;
import org.hsse.news.bot.BotMapping;
import org.hsse.news.bot.ChatId;
import org.hsse.news.bot.Message;
import org.hsse.news.bot.TelegramBot;
import org.hsse.news.database.topic.exceptions.QuantityLimitExceededTopicsPerUserException;
import org.hsse.news.database.topic.exceptions.TopicAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class TopicsBotHandlers {
    private final static String MENU_COMMAND = "/menu";
    private final static String TOPICS_MENU_COMMAND = "/topics";
    private final static String LIST_SUBBED_TOPICS_COMMAND = "/unblocked-topics";
    private final static String LIST_NOT_SUBBED_TOPICS_COMMAND = "/blocked-topics";
    private final static String VIEW_TOPIC_COMMAND = "/view-topic";
    private final static String SUB_TOPIC_COMMAND = "/unblock-topic";
    private final static String UNSUB_TOPIC_COMMAND = "/block-topic";
    private final static String SUB_CUSTOM_TOPIC_COMMAND = "/add-custom-topic";
    private final static String DELETE_CUSTOM_TOPIC = "/delete-custom-topic";

    private final static String BACK_TEXT = "Назад";

    private final TopicsDataProvider topicsDataProvider;
    private TelegramBot bot;

    public TopicsBotHandlers(final TopicsDataProvider topicsDataProvider) {
        this.topicsDataProvider = topicsDataProvider;
    }

    @Autowired
    @Lazy
    public void setBot(final TelegramBot bot) {
        this.bot = bot;
    }

    public void sendMessage(final ChatId chatId, final String text) {
        bot.sendMessage(chatId, Message.builder().text(text).build());
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
                        .text("Добавить свою тему")
                        .callbackData(SUB_CUSTOM_TOPIC_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text(BACK_TEXT)
                        .callbackData(MENU_COMMAND).build())));
    }

    @BotMapping(TOPICS_MENU_COMMAND)
    public Message topicsMenu() {
        return Message.builder().text("Темы").keyboard(topicsMenuKeyboard()).build();
    }

    private Message buildTopicsListMenu(final String text, final List<TopicInfo> topics) {
        final List<InlineKeyboardButton> buttons = new ArrayList<>(
                topics.stream().map(
                        topic -> InlineKeyboardButton.builder()
                                .text(topic.description())
                                .callbackData(VIEW_TOPIC_COMMAND + " " + topic.topicID())
                                .build()).toList());
        buttons.add(InlineKeyboardButton.builder()
                .text(BACK_TEXT)
                .callbackData(TOPICS_MENU_COMMAND).build());
        return Message.builder().text(text).verticalKeyboard(buttons).build();
    }

    @SneakyThrows
    private Message viewTopicMessage(final Long topicId, final ChatId chatId) {
        final TopicInfo topic = topicsDataProvider.findTopic(topicId).orElseThrow();
        final String subCommand = (topicsDataProvider.isSubbedTopic(chatId.value(), topicId) 
                ? UNSUB_TOPIC_COMMAND 
                : SUB_TOPIC_COMMAND) + " " + topicId;

        if (!topicsDataProvider.isCustomCreatedTopicByUser(chatId.value(), topicId)) {
            return Message.builder().text(topic.description())
                    .verticalKeyboard(List.of(
                            InlineKeyboardButton.builder()
                                    .text(topicsDataProvider.isSubbedTopic(chatId.value(), topicId) ? "Отписаться" : "Подписаться")
                                    .callbackData(subCommand).build(),
                            InlineKeyboardButton.builder()
                                    .text(BACK_TEXT)
                                    .callbackData(topicsDataProvider.isSubbedTopic(chatId.value(), topicId)
                                            ? LIST_SUBBED_TOPICS_COMMAND
                                            : LIST_NOT_SUBBED_TOPICS_COMMAND)
                                    .build())).build();
        }

        return Message.builder().text(topic.description())
                .verticalKeyboard(List.of(
                        InlineKeyboardButton.builder()
                                .text(topicsDataProvider.isSubbedTopic(chatId.value(), topicId) ? "Отписаться" : "Подписаться")
                                .callbackData(subCommand).build(),
                        InlineKeyboardButton.builder()
                                .text("Удалить свою тему")
                                .callbackData(DELETE_CUSTOM_TOPIC + " " + topicId).build(),
                        InlineKeyboardButton.builder()
                                .text(BACK_TEXT)
                                .callbackData(topicsDataProvider.isSubbedTopic(chatId.value(), topicId)
                                        ? LIST_SUBBED_TOPICS_COMMAND
                                        : LIST_NOT_SUBBED_TOPICS_COMMAND)
                                .build())).build();
    }

    private Message subOrUnsubTopic(final Long topicId, final ChatId chatId) {
        if (topicsDataProvider.isSubbedTopic(chatId.value(), topicId)) {
            topicsDataProvider.unSubTopic(chatId.value(), topicId);
            return viewUserSubTopics(chatId);
        } else {
            if (topicsDataProvider.subTopic(chatId.value(), topicId)) {
                return viewUserUnsubTopics(chatId);
            }
            sendMessage(chatId, "Превышен лимит подписок на темы");
            return viewUserUnsubTopics(chatId);
        }
    }

    private Message createCustomTopic(final String text) {
        final List<String> args = Arrays.stream(text.split(" ")).toList();
        final long chatId = Long.parseLong(args.get(1));
        final String description = args.get(0);
        
        try {
            topicsDataProvider.createCustomTopic(chatId, description);
            return Message.builder()
                    .text("Тема '" + description + "' добавлена")
                    .keyboard(topicsMenuKeyboard())
                    .build();
        } catch (TopicAlreadyExistsException | QuantityLimitExceededTopicsPerUserException e) {
            log.error("Error creating topic: {}", e.getMessage());
            sendMessage(new ChatId(chatId), e.getMessage());
            return Message.builder()
                    .text("Не удалось добавить тему")
                    .keyboard(topicsMenuKeyboard())
                    .build();
        }
    }

    @BotMapping(LIST_SUBBED_TOPICS_COMMAND)
    public Message viewUserSubTopics(final ChatId chatId) {
        return buildTopicsListMenu("Ваши подписки:", 
                topicsDataProvider.getSubbedTopics(chatId.value()));
    }

    @BotMapping(LIST_NOT_SUBBED_TOPICS_COMMAND)
    public Message viewUserUnsubTopics(final ChatId chatId) {
        return buildTopicsListMenu("Доступные темы:", 
                topicsDataProvider.getUnsubbedTopics(chatId.value()));
    }

    @BotMapping(VIEW_TOPIC_COMMAND)
    public Message viewTopic(final String arg, final ChatId chatId) {
        final Long topicId = Long.parseLong(arg);
        return viewTopicMessage(topicId, chatId);
    }

    @BotMapping(SUB_TOPIC_COMMAND)
    public Message subTopic(final String arg, final ChatId chatId) {
        final Long topicId = Long.parseLong(arg);
        return subOrUnsubTopic(topicId, chatId);
    }

    @BotMapping(UNSUB_TOPIC_COMMAND)
    public Message unsubTopic(final String arg, final ChatId chatId) {
        final Long topicId = Long.parseLong(arg);
        return subOrUnsubTopic(topicId, chatId);
    }

    @BotMapping(SUB_CUSTOM_TOPIC_COMMAND)
    public Message subCustomTopic(final ChatId chatId) {
        return Message.builder()
                .text("Введите название темы:")
                .singleButton(InlineKeyboardButton.builder()
                        .text("Отмена")
                        .callbackData(TOPICS_MENU_COMMAND)
                        .build())
                .onNextMessage(text -> createCustomTopic(text + " " + chatId.value()))
                .build();
    }

    @BotMapping(DELETE_CUSTOM_TOPIC)
    public Message deleteCustomTopic(final String arg, final ChatId chatId) {
        final Long topicId = Long.parseLong(arg);
        topicsDataProvider.deleteCustomTopic(chatId.value(), topicId);
        return viewUserSubTopics(chatId);
    }
}