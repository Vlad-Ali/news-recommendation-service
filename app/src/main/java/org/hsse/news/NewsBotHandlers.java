package org.hsse.news;

import org.hsse.news.api.schemas.shared.WebsiteInfo;
import org.hsse.news.database.article.models.Article;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.topic.models.TopicDto;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.website.exceptions.WebsiteAlreadyExistsException;
import org.hsse.news.database.website.exceptions.WebsiteRSSNotValidException;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

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
    private final static String SUB_CUSTOM_WEBSITE_COMMAND = "/sub-custom-website";
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
    private final static String REGISTER_COMMAND = "/register";
    private final static String DELETE_CUSTOM_WEBSITE = "/delete-custom-website";

    private final static String BACK_TEXT = "Назад";

    private final StubDataProvider dataProvider;

    private final TelegramBot telegramBot;

    private enum ArticleOpinion {
        LIKED,
        NEUTRAL,
        DISLIKED
    }

    public NewsBotHandlers(final TelegramBot bot, final StubDataProvider dataProvider) {
        this.dataProvider = dataProvider;
        this.telegramBot = bot;

        bot.command(START_COMMAND, () ->
                new TelegramBot.Message(
                        "Привет! Добавь источники и ты сможешь смотреть ленту новостей в этом боте! ",
                        getMainMenu()));

        bot.command(REGISTER_COMMAND, dataProvider::registerUser);
        bot.command(MENU_COMMAND, () ->
                new TelegramBot.Message("Меню", getMainMenu()));
        bot.command(SEND_TEST_ARTICLE_COMMAND, () ->
        {
            try {
                bot.sendArticle((messageId) ->
                        articleMessage(new ArticleId(UUID.randomUUID()), ArticleOpinion.NEUTRAL, messageId));
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        });

        addWebsitesHandlers(bot);
        addTopicsHandlers(bot);
        addLikesHandlers(bot);
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

    private InlineKeyboardMarkup buildWebsitesListMenu(final List<WebsiteInfo> websites) {
        final List<List<InlineKeyboardButton>> buttons = new ArrayList<>(
                websites.stream().map(
                        website -> List.of(InlineKeyboardButton.builder()
                                .text(website.description())
                                .callbackData(VIEW_WEBSITE_COMMAND + " " + website.websiteId())
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

    private InlineKeyboardMarkup buildTopicListMenu(final List<TopicDto> topics) {
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

    private TelegramBot.Message viewWebsiteMessage(final ChatAndWebsiteID chatAndWebsiteID) {
        final Long chatId = chatAndWebsiteID.chatId();
        final Long websiteId = chatAndWebsiteID.websiteId();
        final WebsiteInfo website = dataProvider.findWebsite(websiteId).orElseThrow();
        final String subCommand =
                (dataProvider.isSubbedWebsite(chatId, websiteId) ? UNSUB_WEBSITE_COMMAND : SUB_WEBSITE_COMMAND)
                        + " " + websiteId;
        if (!dataProvider.isCustomCreatedWebsiteByUser(chatId, websiteId)){
        return new TelegramBot.Message(website.description() + "\n" + website.url(),
                new InlineKeyboardMarkup(List.of(
                        List.of(InlineKeyboardButton.builder()
                                .text(dataProvider.isSubbedWebsite(chatId, websiteId) ? "Отписаться" : "Подписаться")
                                .callbackData(subCommand).build()),
                        List.of(InlineKeyboardButton.builder()
                                .text(BACK_TEXT)
                                .callbackData(dataProvider.isSubbedWebsite(chatId, websiteId)
                                        ? LIST_SUBBED_WEBSITES_COMMAND
                                        : LIST_NOT_SUBBED_WEBSITES_COMMAND)
                                .build()))));
        }
        return new TelegramBot.Message(website.description() + "\n" + website.url(),
                new InlineKeyboardMarkup(List.of(
                        List.of(InlineKeyboardButton.builder()
                                .text(dataProvider.isSubbedWebsite(chatId, websiteId) ? "Отписаться" : "Подписаться")
                                .callbackData(subCommand).build()),
                        List.of(InlineKeyboardButton.builder()
                                .text("Удалить свой созданный сайт")
                                .callbackData(DELETE_CUSTOM_WEBSITE +" "+websiteId).build()),
                        List.of(InlineKeyboardButton.builder()
                                .text(BACK_TEXT)
                                .callbackData(dataProvider.isSubbedWebsite(chatId, websiteId)
                                        ? LIST_SUBBED_WEBSITES_COMMAND
                                        : LIST_NOT_SUBBED_WEBSITES_COMMAND)
                                .build()))));
    }

    private TelegramBot.Message viewUserSubWebsites(final Long chatId){
        return new TelegramBot.Message("Подписки:",
                buildWebsitesListMenu(dataProvider.getSubbedWebsites(chatId)));
    }

    private TelegramBot.Message viewUserUnSubWebsites(final Long chatId){
        return new TelegramBot.Message("Вы не подписаны на:",
                buildWebsitesListMenu(dataProvider.getUnsubbedWebsites(chatId)));
    }

    private TelegramBot.Message deleteCustomWebsite(final ChatAndWebsiteID chatAndWebsiteID){
        final Long chatId = chatAndWebsiteID.chatId();
        final Long websiteId = chatAndWebsiteID.websiteId();
        if (dataProvider.isSubbedWebsite(chatId, websiteId)){
            dataProvider.deleteCustomWebsite(chatId, websiteId);
            return viewUserSubWebsites(chatId);
        } else{
            dataProvider.deleteCustomWebsite(chatId, websiteId);
            return viewUserUnSubWebsites(chatId);
        }
    }

    private TelegramBot.Message subOrUnSubWebsite(final ChatAndWebsiteID chatAndWebsiteID){
        final Long chatId = chatAndWebsiteID.chatId();
        final Long websiteId = chatAndWebsiteID.websiteId();
        if (dataProvider.isSubbedWebsite(chatId, websiteId)){
            dataProvider.unSubWebsite(chatId, websiteId);
            return viewUserSubWebsites(chatId);
        } else {
            if (dataProvider.subWebsite(chatId, websiteId)){
                return viewUserUnSubWebsites(chatId);
            }
            try {
                telegramBot.sendMessage(chatId, "Превышен лимит по выбранным сайтам");
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
            return viewUserUnSubWebsites(chatId);
        }
    }

    private TelegramBot.Message createCustomWebsite(final ChatIdAndWebsiteInfo chatIdAndWebsiteInfo){
        final Long chatId = chatIdAndWebsiteInfo.chatId();
        final String url = chatIdAndWebsiteInfo.url();
        final String description = chatIdAndWebsiteInfo.url();
        try {
            dataProvider.createCustomWebsite(chatId, url, description);
            return new TelegramBot.Message("Источник " + description + " добавлен", getWebsitesMenu());
        } catch (WebsiteRSSNotValidException | WebsiteAlreadyExistsException e) {
            return new TelegramBot.Message(e.getMessage(), getWebsitesMenu());
        }
    }

    private TelegramBot.Message viewCreateCustomWebsite(final Long chatId){
        return new TelegramBot.Message("Введите URI и описание через пробел:",
                new InlineKeyboardMarkup(List.of(List.of(
                        InlineKeyboardButton.builder()
                                .text("Отмена")
                                .callbackData(WEBSITES_MENU_COMMAND).build()))),
                (text) -> createCustomWebsite(new ChatIdAndWebsiteInfo(chatId, text))
                                /*new TelegramBot.Message("Источник " + text + " добавлен",
                                        getWebsitesMenu())*/);
    }

    private void addWebsitesHandlers(final TelegramBot bot) {
        bot.command(WEBSITES_MENU_COMMAND, () ->
                new TelegramBot.Message("Источники", getWebsitesMenu()));
        bot.command(LIST_SUBBED_WEBSITES_COMMAND, this::viewUserSubWebsites);
        bot.command(LIST_NOT_SUBBED_WEBSITES_COMMAND, this::viewUserUnSubWebsites);
        bot.commandWebsite(SUB_WEBSITE_COMMAND, this::subOrUnSubWebsite);
        bot.commandWebsite(UNSUB_WEBSITE_COMMAND, this::subOrUnSubWebsite);
        bot.commandWebsite(DELETE_CUSTOM_WEBSITE, this::deleteCustomWebsite);
        bot.command(SUB_CUSTOM_WEBSITE_COMMAND, this::viewCreateCustomWebsite);
        bot.commandWebsite(VIEW_WEBSITE_COMMAND, this::viewWebsiteMessage);

    }

    private TelegramBot.Message viewTopicMessage(final TopicId id) {
        final TopicDto topic = dataProvider.findTopic(id).orElseThrow();
        final String subCommandName = dataProvider.isSubbedWebsite(id) ? "Отписаться" : "Подписаться";
        final String subCommand = dataProvider.isSubbedWebsite(id) ? UNSUB_TOPIC_COMMAND : SUB_TOPIC_COMMAND;

        return new TelegramBot.Message(topic.description(),
                new InlineKeyboardMarkup(List.of(
                        List.of(InlineKeyboardButton.builder()
                                .text(subCommandName)
                                .callbackData(subCommand + " " + id).build()),
                        List.of(InlineKeyboardButton.builder()
                                .text(BACK_TEXT)
                                .callbackData(dataProvider.isSubbedWebsite(id)
                                        ? LIST_SUBBED_TOPICS_COMMAND
                                        : LIST_NOT_SUBBED_TOPICS_COMMAND)
                                .build()))));
    }

    private void addTopicsHandlers(final TelegramBot bot) {
        bot.command(TOPICS_MENU_COMMAND, () ->
                new TelegramBot.Message("Темы", getWebsitesMenu()));
        bot.command(LIST_SUBBED_TOPICS_COMMAND, () ->
                new TelegramBot.Message("Подписки:",
                        buildTopicListMenu(dataProvider.getSubbedTopics())));
        bot.command(LIST_NOT_SUBBED_TOPICS_COMMAND, () ->
                new TelegramBot.Message("Вы не подписаны на:",
                        buildTopicListMenu(dataProvider.getUnsubbedTopics())));
        bot.commandTopic(SUB_TOPIC_COMMAND, this::viewTopicMessage);
        bot.commandTopic(UNSUB_TOPIC_COMMAND, this::viewTopicMessage);
        bot.command(SUB_CUSTOM_TOPIC_COMMAND, () ->
                new TelegramBot.Message("Введите название темы:",
                        new InlineKeyboardMarkup(List.of(List.of(
                                InlineKeyboardButton.builder()
                                        .text("Отмена")
                                        .callbackData(WEBSITES_MENU_COMMAND).build()))),
                        (text) ->
                                new TelegramBot.Message("Тема " + text + " добавлена",
                                        getTopicsMenu())));
        bot.commandTopic(VIEW_TOPIC_COMMAND, this::viewTopicMessage);
    }

    private void addLikesHandlers(final TelegramBot bot) {
        bot.commandArticle(LIKE_COMMAND, (id, messageId) ->
                articleMessage(id, ArticleOpinion.LIKED, messageId));
        bot.commandArticle(DISLIKE_COMMAND, (id, messageId) ->
                articleMessage(id, ArticleOpinion.DISLIKED, messageId));
        bot.commandArticle(UNLIKE_COMMAND, (id, messageId) ->
                articleMessage(id, ArticleOpinion.NEUTRAL, messageId));
        bot.commandArticle(UNDISLIKE_COMMAND, (id, messageId) ->
                articleMessage(id, ArticleOpinion.NEUTRAL, messageId));
    }

    private InlineKeyboardMarkup articleMenu(
            final ArticleId id, final ArticleOpinion opinion, final int messageId) {
        return new InlineKeyboardMarkup(List.of(List.of(
                opinion == ArticleOpinion.LIKED
                        ? InlineKeyboardButton.builder()
                        .text("✅\uD83D\uDC4D")
                        .callbackData(UNLIKE_COMMAND + " " + id.value() + " " + messageId).build()
                        : InlineKeyboardButton.builder()
                        .text("\uD83D\uDC4D")
                        .callbackData(LIKE_COMMAND + " " + id.value() + " " + messageId).build(),
                opinion == ArticleOpinion.DISLIKED
                        ? InlineKeyboardButton.builder()
                        .text("✅\uD83D\uDC4E")
                        .callbackData(UNDISLIKE_COMMAND + " " + id.value() + " " + messageId).build()
                        : InlineKeyboardButton.builder()
                        .text("\uD83D\uDC4E")
                        .callbackData(DISLIKE_COMMAND + " " + id.value() + " " + messageId).build())));
    }

    private TelegramBot.Message articleMessage(
            final ArticleId id, final ArticleOpinion opinion, final int messageId) {
        final Article article = dataProvider.getExampleArticle();
        return new TelegramBot.Message(article.title() + "\n" + article.url(),
                articleMenu(id, opinion, messageId));
    }
}
