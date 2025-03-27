package org.hsse.news.bot;

import org.hsse.news.database.article.models.Article;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.topic.models.Topic;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.website.models.Website;
import org.hsse.news.database.website.models.WebsiteId;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.MemberStatus;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

//    private final StubDataProvider dataProvider;

    private enum ArticleOpinion {
        LIKED,
        NEUTRAL,
        DISLIKED
    }

//    public NewsBotHandlers(final TelegramBot bot, final StubDataProvider dataProvider) {
//        this.dataProvider = dataProvider;
//
//        bot.command(START_COMMAND, () ->
//                new TelegramBot.Message(
//                        "Привет! Добавь источники и ты сможешь смотреть ленту новостей в этом боте! ",
//                        getMainMenu()));
//        bot.command(MENU_COMMAND, () ->
//                new TelegramBot.Message("Меню", getMainMenu()));
//        bot.command(SEND_TEST_ARTICLE_COMMAND, () ->
//                bot.sendArticle((messageId) ->
//                        articleMessage(new ArticleId(UUID.randomUUID()), ArticleOpinion.NEUTRAL, messageId)));
//
//        addWebsitesHandlers(bot);
//        addTopicsHandlers(bot);
//        addLikesHandlers(bot);
//    }

//    private InlineKeyboardMarkup getMainMenu() {
//        return new InlineKeyboardMarkup(List.of(
//                List.of(InlineKeyboardButton.builder()
//                        .text("Источники")
//                        .callbackData(WEBSITES_MENU_COMMAND).build()),
//                List.of(InlineKeyboardButton.builder()
//                        .text("Темы")
//                        .callbackData(TOPICS_MENU_COMMAND).build())));
//    }
//
//    private InlineKeyboardMarkup getWebsitesMenu() {
//        return new InlineKeyboardMarkup(List.of(
//                List.of(InlineKeyboardButton.builder()
//                        .text("Подписан")
//                        .callbackData(LIST_SUBBED_WEBSITES_COMMAND).build()),
//                List.of(InlineKeyboardButton.builder()
//                        .text("Не подписан")
//                        .callbackData(LIST_NOT_SUBBED_WEBSITES_COMMAND).build()),
//                List.of(InlineKeyboardButton.builder()
//                        .text("Добавить...")
//                        .callbackData(SUB_CUSTOM_WEBSITE_COMMAND).build()),
//                List.of(InlineKeyboardButton.builder()
//                        .text(BACK_TEXT)
//                        .callbackData("/menu").build())));
//    }
//
//    private InlineKeyboardMarkup buildWebsitesListMenu(final List<Website> websites) {
//        final List<List<InlineKeyboardButton>> buttons = new ArrayList<>(
//                websites.stream().map(
//                        website -> List.of(InlineKeyboardButton.builder()
//                                .text(website.description())
//                                .callbackData(VIEW_WEBSITE_COMMAND + " " + website.id().value())
//                                .build())).toList());
//        buttons.add(List.of(InlineKeyboardButton.builder()
//                .text(BACK_TEXT)
//                .callbackData(WEBSITES_MENU_COMMAND).build()));
//        return new InlineKeyboardMarkup(buttons);
//    }
//
//    private InlineKeyboardMarkup getTopicsMenu() {
//        return new InlineKeyboardMarkup(List.of(
//                List.of(InlineKeyboardButton.builder()
//                        .text("Подписан")
//                        .callbackData(LIST_SUBBED_TOPICS_COMMAND).build()),
//                List.of(InlineKeyboardButton.builder()
//                        .text("Не подписан")
//                        .callbackData(LIST_NOT_SUBBED_TOPICS_COMMAND).build()),
//                List.of(InlineKeyboardButton.builder()
//                        .text("Добавить...")
//                        .callbackData(SUB_CUSTOM_TOPIC_COMMAND).build()),
//                List.of(InlineKeyboardButton.builder()
//                        .text(BACK_TEXT)
//                        .callbackData("/menu").build())));
//    }
//
//    private InlineKeyboardMarkup buildTopicListMenu(final List<Topic> topics) {
//        final List<List<InlineKeyboardButton>> buttons = new ArrayList<>(
//                topics.stream().map(
//                        topic -> List.of(InlineKeyboardButton.builder()
//                                .text(topic.description())
//                                .callbackData(VIEW_TOPIC_COMMAND + " " + topic.id())
//                                .build())).toList());
//        buttons.add(List.of(
//                InlineKeyboardButton.builder()
//                        .text(BACK_TEXT)
//                        .callbackData(TOPICS_MENU_COMMAND).build()));
//        return new InlineKeyboardMarkup(buttons);
//    }
//
//    private TelegramBot.Message viewWebsiteMessage(final WebsiteId id) {
//        final Website website = dataProvider.findWebsite(id).orElseThrow();
//        final String subCommand =
//                (dataProvider.isSubbed(id) ? UNSUB_TOPIC_COMMAND : SUB_TOPIC_COMMAND)
//                        + " " + id;
//
//        return new TelegramBot.Message(website.description() + "\n" + website.url(),
//                new InlineKeyboardMarkup(List.of(
//                        List.of(InlineKeyboardButton.builder()
//                                .text(dataProvider.isSubbed(id) ? "Отписаться" : "Подписаться")
//                                .callbackData(subCommand + " " + id).build()),
//                        List.of(InlineKeyboardButton.builder()
//                                .text(BACK_TEXT)
//                                .callbackData(dataProvider.isSubbed(id)
//                                        ? LIST_SUBBED_WEBSITES_COMMAND
//                                        : LIST_NOT_SUBBED_WEBSITES_COMMAND)
//                                .build()))));
//    }
//
//    private void addWebsitesHandlers(final TelegramBot bot) {
//        bot.command(WEBSITES_MENU_COMMAND, () ->
//                new TelegramBot.Message("Источники", getWebsitesMenu()));
//        bot.command(LIST_SUBBED_WEBSITES_COMMAND, () ->
//                new TelegramBot.Message("Подписки:",
//                        buildWebsitesListMenu(dataProvider.getSubbedWebsites())));
//        bot.command(LIST_NOT_SUBBED_WEBSITES_COMMAND, () ->
//                new TelegramBot.Message("Вы не подписаны на:",
//                        buildWebsitesListMenu(dataProvider.getUnsubbedWebsites())));
//        bot.commandWebsite(SUB_WEBSITE_COMMAND, this::viewWebsiteMessage);
//        bot.commandWebsite(UNSUB_WEBSITE_COMMAND, this::viewWebsiteMessage);
//        bot.command(SUB_CUSTOM_WEBSITE_COMMAND, () ->
//                new TelegramBot.Message("Введите URI:",
//                        new InlineKeyboardMarkup(List.of(List.of(
//                                InlineKeyboardButton.builder()
//                                        .text("Отмена")
//                                        .callbackData(TOPICS_MENU_COMMAND).build()))),
//                        (text) ->
//                                new TelegramBot.Message("Источник " + text + " добавлен",
//                                        getWebsitesMenu())));
//        bot.commandWebsite(VIEW_WEBSITE_COMMAND, this::viewWebsiteMessage);
//
//    }
//
//    private TelegramBot.Message viewTopicMessage(final TopicId id) {
//        final Topic topic = dataProvider.findTopic(id).orElseThrow();
//        final String subCommandName = dataProvider.isSubbed(id) ? "Отписаться" : "Подписаться";
//        final String subCommand = dataProvider.isSubbed(id) ? UNSUB_TOPIC_COMMAND : SUB_TOPIC_COMMAND;
//
//        return new TelegramBot.Message(topic.description(),
//                new InlineKeyboardMarkup(List.of(
//                        List.of(InlineKeyboardButton.builder()
//                                .text(subCommandName)
//                                .callbackData(subCommand + " " + id).build()),
//                        List.of(InlineKeyboardButton.builder()
//                                .text(BACK_TEXT)
//                                .callbackData(dataProvider.isSubbed(id)
//                                        ? LIST_SUBBED_TOPICS_COMMAND
//                                        : LIST_NOT_SUBBED_TOPICS_COMMAND)
//                                .build()))));
//    }
//
//    private void addTopicsHandlers(final TelegramBot bot) {
//        bot.command(TOPICS_MENU_COMMAND, () ->
//                new TelegramBot.Message("Темы", getWebsitesMenu()));
//        bot.command(LIST_SUBBED_TOPICS_COMMAND, () ->
//                new TelegramBot.Message("Подписки:",
//                        buildTopicListMenu(dataProvider.getSubbedTopics())));
//        bot.command(LIST_NOT_SUBBED_TOPICS_COMMAND, () ->
//                new TelegramBot.Message("Вы не подписаны на:",
//                        buildTopicListMenu(dataProvider.getUnsubbedTopics())));
//        bot.commandTopic(SUB_TOPIC_COMMAND, this::viewTopicMessage);
//        bot.commandTopic(UNSUB_TOPIC_COMMAND, this::viewTopicMessage);
//        bot.command(SUB_CUSTOM_TOPIC_COMMAND, () ->
//                new TelegramBot.Message("Введите название темы:",
//                        new InlineKeyboardMarkup(List.of(List.of(
//                                InlineKeyboardButton.builder()
//                                        .text("Отмена")
//                                        .callbackData(WEBSITES_MENU_COMMAND).build()))),
//                        (text) ->
//                                new TelegramBot.Message("Тема " + text + " добавлена",
//                                        getTopicsMenu())));
//        bot.commandTopic(VIEW_TOPIC_COMMAND, this::viewTopicMessage);
//    }
//
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

    @BotMapping("/test")
    TelegramBot.Message testAnnotatedMethod(WebsiteId id) {
        return new TelegramBot.Message("Hi", null);
    }
}
