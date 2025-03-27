package org.hsse.news.bot;

import org.hsse.news.database.topic.models.Topic;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.website.models.Website;
import org.hsse.news.database.website.models.WebsiteId;
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
    private final static String LIST_SUBBED_WEBSITES_COMMAND = "/subs";
    private final static String LIST_NOT_SUBBED_WEBSITES_COMMAND = "/browse-websites";
    private final static String VIEW_WEBSITE_COMMAND = "/view-website";
    private final static String SUB_WEBSITE_COMMAND = "/sub";
    private final static String UNSUB_WEBSITE_COMMAND = "/unsub";
    private final static String SUB_CUSTOM_WEBSITE_COMMAND = "/sub-custom";
    private final static String TOPICS_MENU_COMMAND = "/topics";
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
    private final static String SEND_TEST_ARTICLE_COMMAND = "/test-feed";

    private final static String BACK_TEXT = "Назад";

    private final StubDataProvider dataProvider;

    private enum ArticleOpinion {
        LIKED,
        NEUTRAL,
        DISLIKED
    }

    public NewsBotHandlers(final StubDataProvider dataProvider) {
        this.dataProvider = dataProvider;
    }

    @BotMapping(START_COMMAND)
    public TelegramBot.Message start() {
        return new TelegramBot.Message(
                "Привет! Добавь источники и ты сможешь смотреть ленту новостей в этом боте! ",
                getMainMenu());
    }

    @BotMapping(MENU_COMMAND)
    public TelegramBot.Message menu() {
        return new TelegramBot.Message("Меню", getMainMenu());
    }

    @BotMapping(SEND_TEST_ARTICLE_COMMAND)
    public void testArticle() {
//        bot.sendArticle((messageId) ->
//                        articleMessage(new ArticleId(UUID.randomUUID()), ArticleOpinion.NEUTRAL, messageId))
    }

    private InlineKeyboardMarkup getMainMenu() {
        return new InlineKeyboardMarkup(List.of(
                List.of(InlineKeyboardButton.builder()
                        .text("Источники")
                        .callbackData(WEBSITES_MENU_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text("Темы")
                        .callbackData(TOPICS_MENU_COMMAND).build())));
    }

    private InlineKeyboardMarkup getWebsitesMenu() {
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

    private InlineKeyboardMarkup buildWebsitesListMenu(final List<Website> websites) {
        final List<List<InlineKeyboardButton>> buttons = new ArrayList<>(
                websites.stream().map(
                        website -> List.of(InlineKeyboardButton.builder()
                                .text(website.description())
                                .callbackData(VIEW_WEBSITE_COMMAND + " " + website.id().value())
                                .build())).toList());
        buttons.add(List.of(InlineKeyboardButton.builder()
                .text(BACK_TEXT)
                .callbackData(WEBSITES_MENU_COMMAND).build()));
        return new InlineKeyboardMarkup(buttons);
    }

    private InlineKeyboardMarkup getTopicsMenu() {
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

    private InlineKeyboardMarkup buildTopicListMenu(final List<Topic> topics) {
        final List<List<InlineKeyboardButton>> buttons = new ArrayList<>(
                topics.stream().map(
                        topic -> List.of(InlineKeyboardButton.builder()
                                .text(topic.description())
                                .callbackData(VIEW_TOPIC_COMMAND + " " + topic.id())
                                .build())).toList());
        buttons.add(List.of(
                InlineKeyboardButton.builder()
                        .text(BACK_TEXT)
                        .callbackData(TOPICS_MENU_COMMAND).build()));
        return new InlineKeyboardMarkup(buttons);
    }

    private TelegramBot.Message viewWebsiteMessage(final WebsiteId id) {
        final Website website = dataProvider.findWebsite(id).orElseThrow();
        final String subCommand =
                (dataProvider.isSubbed(id) ? UNSUB_TOPIC_COMMAND : SUB_TOPIC_COMMAND)
                        + " " + id;

        return new TelegramBot.Message(website.description() + "\n" + website.url(),
                new InlineKeyboardMarkup(List.of(
                        List.of(InlineKeyboardButton.builder()
                                .text(dataProvider.isSubbed(id) ? "Отписаться" : "Подписаться")
                                .callbackData(subCommand + " " + id).build()),
                        List.of(InlineKeyboardButton.builder()
                                .text(BACK_TEXT)
                                .callbackData(dataProvider.isSubbed(id)
                                        ? LIST_SUBBED_WEBSITES_COMMAND
                                        : LIST_NOT_SUBBED_WEBSITES_COMMAND)
                                .build()))));
    }

    @BotMapping(WEBSITES_MENU_COMMAND)
    public TelegramBot.Message websitesMenu() {
        return new TelegramBot.Message("Источники", getWebsitesMenu());
    }

    @BotMapping(LIST_SUBBED_WEBSITES_COMMAND)
    public TelegramBot.Message listSubbedWebsites() {
        return new TelegramBot.Message("Подписки:",
                buildWebsitesListMenu(dataProvider.getSubbedWebsites()));
    }

    @BotMapping(LIST_NOT_SUBBED_WEBSITES_COMMAND)
    public TelegramBot.Message listNotSubbedWebsites() {
        return new TelegramBot.Message("Вы не подписаны на:",
                buildWebsitesListMenu(dataProvider.getUnsubbedWebsites()));
    }

    @BotMapping(VIEW_WEBSITE_COMMAND)
    public TelegramBot.Message viewWebsite(WebsiteId id) {
        return viewWebsiteMessage(id);
    }

    @BotMapping(SUB_WEBSITE_COMMAND)
    public TelegramBot.Message subWebsite(WebsiteId id) {
        return viewWebsiteMessage(id);
    }

    @BotMapping(UNSUB_WEBSITE_COMMAND)
    public TelegramBot.Message unsubWebsite(WebsiteId id) {
        return viewWebsiteMessage(id);
    }

    @BotMapping(SUB_CUSTOM_WEBSITE_COMMAND)
    public TelegramBot.Message subCustomWebsite() {
        return new TelegramBot.Message("Введите URI:",
                new InlineKeyboardMarkup(List.of(List.of(
                        InlineKeyboardButton.builder()
                                .text("Отмена")
                                .callbackData(TOPICS_MENU_COMMAND).build()))),
                (text) ->
                        new TelegramBot.Message("Источник " + text + " добавлен",
                                getWebsitesMenu()));
    }

    private TelegramBot.Message viewTopicMessage(final TopicId id) {
        final Topic topic = dataProvider.findTopic(id).orElseThrow();
        final String subCommandName = dataProvider.isSubbed(id) ? "Отписаться" : "Подписаться";
        final String subCommand = dataProvider.isSubbed(id) ? UNSUB_TOPIC_COMMAND : SUB_TOPIC_COMMAND;

        return new TelegramBot.Message(topic.description(),
                new InlineKeyboardMarkup(List.of(
                        List.of(InlineKeyboardButton.builder()
                                .text(subCommandName)
                                .callbackData(subCommand + " " + id).build()),
                        List.of(InlineKeyboardButton.builder()
                                .text(BACK_TEXT)
                                .callbackData(dataProvider.isSubbed(id)
                                        ? LIST_SUBBED_TOPICS_COMMAND
                                        : LIST_NOT_SUBBED_TOPICS_COMMAND)
                                .build()))));
    }

    @BotMapping(TOPICS_MENU_COMMAND)
    public TelegramBot.Message topicsMenu() {
        return new TelegramBot.Message("Темы", getTopicsMenu());
    }

    @BotMapping(LIST_SUBBED_TOPICS_COMMAND)
    public TelegramBot.Message listSubbedTopics() {
        return new TelegramBot.Message("Подписки:",
                buildTopicListMenu(dataProvider.getSubbedTopics()));
    }

    @BotMapping(LIST_NOT_SUBBED_TOPICS_COMMAND)
    public TelegramBot.Message listUnsubbedTopics() {
        return new TelegramBot.Message("Вы не подписаны на:",
                buildTopicListMenu(dataProvider.getUnsubbedTopics()));
    }

    @BotMapping(VIEW_TOPIC_COMMAND)
    public TelegramBot.Message viewTopic(TopicId id) {
        return viewTopicMessage(id);
    }

    @BotMapping(SUB_TOPIC_COMMAND)
    public TelegramBot.Message subTopic(TopicId id) {
        return viewTopicMessage(id);
    }

    @BotMapping(UNSUB_TOPIC_COMMAND)
    public TelegramBot.Message unsubTopic(TopicId id) {
        return viewTopicMessage(id);
    }

    @BotMapping(SUB_CUSTOM_TOPIC_COMMAND)
    public TelegramBot.Message subCustomTopic() {
        return new TelegramBot.Message("Введите название темы:",
                new InlineKeyboardMarkup(List.of(List.of(
                        InlineKeyboardButton.builder()
                                .text("Отмена")
                                .callbackData(WEBSITES_MENU_COMMAND).build()))),
                (text) ->
                        new TelegramBot.Message("Тема " + text + " добавлена",
                                getTopicsMenu()));
    }

//    private void addLikesHandlers(final TelegramBot bot) {
//        bot.commandArticle(LIKE_COMMAND, (id, messageId) ->
//                articleMessage(id, ArticleOpinion.LIKED, messageId));
//        bot.commandArticle(DISLIKE_COMMAND, (id, messageId) ->
//                articleMessage(id, ArticleOpinion.DISLIKED, messageId));
//        bot.commandArticle(UNLIKE_COMMAND, (id, messageId) ->
//                articleMessage(id, ArticleOpinion.NEUTRAL, messageId));
//        bot.commandArticle(UNDISLIKE_COMMAND, (id, messageId) ->
//                articleMessage(id, ArticleOpinion.NEUTRAL, messageId));
//    }
//
//    private InlineKeyboardMarkup articleMenu(
//            final ArticleId id, final ArticleOpinion opinion, final int messageId) {
//        return new InlineKeyboardMarkup(List.of(List.of(
//                opinion == ArticleOpinion.LIKED
//                        ? InlineKeyboardButton.builder()
//                        .text("✅\uD83D\uDC4D")
//                        .callbackData(UNLIKE_COMMAND + " " + id.value() + " " + messageId).build()
//                        : InlineKeyboardButton.builder()
//                        .text("\uD83D\uDC4D")
//                        .callbackData(LIKE_COMMAND + " " + id.value() + " " + messageId).build(),
//                opinion == ArticleOpinion.DISLIKED
//                        ? InlineKeyboardButton.builder()
//                        .text("✅\uD83D\uDC4E")
//                        .callbackData(UNDISLIKE_COMMAND + " " + id.value() + " " + messageId).build()
//                        : InlineKeyboardButton.builder()
//                        .text("\uD83D\uDC4E")
//                        .callbackData(DISLIKE_COMMAND + " " + id.value() + " " + messageId).build())));
//    }
//
//    private TelegramBot.Message articleMessage(
//            final ArticleId id, final ArticleOpinion opinion, final int messageId) {
//        final Article article = dataProvider.getExampleArticle();
//        return new TelegramBot.Message(article.title() + "\n" + article.url(),
//                articleMenu(id, opinion, messageId));
//    }
}
