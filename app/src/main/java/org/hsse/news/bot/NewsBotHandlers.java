package org.hsse.news.bot;

import lombok.extern.slf4j.Slf4j;
import org.hsse.news.database.article.ArticlesService;
import org.hsse.news.database.article.UserArticlesService;
import org.hsse.news.database.article.models.Article;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.topic.TopicService;
import org.hsse.news.database.topic.models.TopicDto;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.user.UserService;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
import org.hsse.news.database.user.models.UserDto;
import org.hsse.news.database.website.WebsiteService;
import org.hsse.news.database.website.models.WebsiteDto;
import org.hsse.news.database.website.models.WebsiteId;
import org.hsse.news.dto.ArticleDto;
import org.hsse.news.dto.RequestUserArticleDto;
import org.hsse.news.dto.ResponseUserArticleDto;
import org.hsse.news.util.Grade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class NewsBotHandlers {
    private final static String START_COMMAND = "/start";
    private final static String MENU_COMMAND = "/menu";

    private final static String WEBSITES_MENU_COMMAND = "/websites";
    private final static String ARTICLES_MENU_COMMAND = "/articles";

    private final static String LIST_SUBBED_WEBSITES_COMMAND = "/subs";
    private final static String LIST_NOT_SUBBED_WEBSITES_COMMAND = "/browse-websites";
    private final static String VIEW_WEBSITE_COMMAND = "/view-website";
    private final static String SUB_WEBSITE_COMMAND = "/sub";
    private final static String UNSUB_WEBSITE_COMMAND = "/unsub";
    private final static String SUB_CUSTOM_WEBSITE_COMMAND = "/sub-custom";
    private final static String TOPICS_MENU_COMMAND = "/topics";
    private final static String VIEW_UNWATCHED_ARTICLES_COMMAND = "/view-unwatched-articles";
    private final static String VIEW_WATCHED_ARTICLES_COMMAND = "/view-watched-articles";
    private final static String LIST_SUBBED_TOPICS_COMMAND = "/unblocked-topics";
    private final static String LIST_NOT_SUBBED_TOPICS_COMMAND = "/blocked-topics";
    private final static String VIEW_TOPIC_COMMAND = "/view-topic";
    private final static String SUB_TOPIC_COMMAND = "/unblock-topic";
    private final static String UNSUB_TOPIC_COMMAND = "/block-topic";
    private final static String SUB_CUSTOM_TOPIC_COMMAND = "/add-custom-topic";

    private final static String LIKE_COMMAND = "/like";
    private final static String UNLIKE_COMMAND = "/unlike";
    private final static String DISLIKE_COMMAND = "/dislike";
    private final static String UNDISLIKE_COMMAND = "/undislike";

    private final static String BACK_TEXT = "Назад";
    private static final String INCREASE_VIEW_WATCHED_ARTICLES_COMMAND = "/increase-view-watched-articles";
    private static final String DECREASE_VIEW_WATCHED_ARTICLES_COMMAND = "/decrease-view-watched-articles";

    private final StubDataProvider dataProvider;

    private final ConcurrentHashMap<ChatId, UserState> tempUserStates = new ConcurrentHashMap<>();

    private enum ArticleOpinion {
        LIKED,
        NEUTRAL,
        DISLIKED
    }

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
    public Message menu(final ChatId chatId) {
        log.debug("Main menu method called");

        tempUserStates.put(chatId, new UserState());
        return Message.builder().text("Меню").keyboard(mainMenuKeyboard()).build();
    }

    @BotMapping("/test-send")
    public void testSend(final TelegramBot bot, final ChatId chatId) {
        bot.sendMessage(chatId, Message.builder().text("test").build());
    }

    private InlineKeyboardMarkup websiteMenuKeyboard() {
        return new InlineKeyboardMarkup(List.of(
                List.of(InlineKeyboardButton.builder()
                        .text("Подписан")
                        .callbackData(LIST_SUBBED_WEBSITES_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text("Не подписан")
                        .callbackData(LIST_NOT_SUBBED_WEBSITES_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text("Добавить...")
                        .callbackData(SUB_CUSTOM_WEBSITE_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text(BACK_TEXT)
                        .callbackData("/menu").build())));
    }

    @BotMapping(WEBSITES_MENU_COMMAND)
    public Message websitesMenu() {
        return Message.builder().text("Источники").keyboard(websiteMenuKeyboard()).build();
    }

    private Message websiteListMessage(final String text, final List<WebsiteDto> websites) {
        final List<InlineKeyboardButton> buttons = new ArrayList<>(
                websites.stream().map(
                        website -> InlineKeyboardButton.builder()
                                .text(website.description())
                                .callbackData(VIEW_WEBSITE_COMMAND + " " + website.id().value())
                                .build()).toList());
        buttons.add(InlineKeyboardButton.builder()
                .text(BACK_TEXT)
                .callbackData(WEBSITES_MENU_COMMAND).build());
        return Message.builder().text(text).verticalKeyboard(buttons).build();
    }

    @BotMapping(LIST_SUBBED_WEBSITES_COMMAND)
    public Message listSubbedWebsites() {
        return websiteListMessage("Подписки:", dataProvider.getSubbedWebsites());
    }

    @BotMapping(LIST_NOT_SUBBED_WEBSITES_COMMAND)
    public Message listNotSubbedWebsites() {
        return websiteListMessage("Вы не подписаны на:", dataProvider.getUnsubbedWebsites());
    }

    private Message viewWebsiteMessage(final WebsiteId id) {
        final WebsiteDto website = dataProvider.findWebsite(id).orElseThrow();
        final String subCommand =
                (dataProvider.isSubbed(id) ? UNSUB_WEBSITE_COMMAND : SUB_WEBSITE_COMMAND)
                        + " " + id;

        return Message.builder().text(website.description() + "\n" + website.url())
                .verticalKeyboard(List.of(
                        InlineKeyboardButton.builder()
                                .text(dataProvider.isSubbed(id) ? "Отписаться" : "Подписаться")
                                .callbackData(subCommand + " " + id).build(),
                        InlineKeyboardButton.builder()
                                .text(BACK_TEXT)
                                .callbackData(dataProvider.isSubbed(id)
                                        ? LIST_SUBBED_WEBSITES_COMMAND
                                        : LIST_NOT_SUBBED_WEBSITES_COMMAND)
                                .build())).build();
    }

    @BotMapping(VIEW_WEBSITE_COMMAND)
    public Message viewWebsite(final WebsiteId id) {
        return viewWebsiteMessage(id);
    }

    @BotMapping(SUB_WEBSITE_COMMAND)
    public Message subWebsite(final WebsiteId id) {
        return viewWebsiteMessage(id);
    }

    @BotMapping(UNSUB_WEBSITE_COMMAND)
    public Message unsubWebsite(final WebsiteId id) {
        return viewWebsiteMessage(id);
    }

    @BotMapping(SUB_CUSTOM_WEBSITE_COMMAND)
    public Message subCustomWebsite() {
        return Message.builder().text("Введите URI:").singleButton(
                InlineKeyboardButton.builder()
                        .text("Отмена")
                        .callbackData(TOPICS_MENU_COMMAND).build()
        ).onNextMessage(text -> Message.builder()
                .text("Источник " + text + " добавлен")
                .keyboard(websiteMenuKeyboard()).build()
        ).build();
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
