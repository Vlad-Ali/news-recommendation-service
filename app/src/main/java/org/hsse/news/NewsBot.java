package org.hsse.news;

import lombok.SneakyThrows;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.topic.models.Topic;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.website.models.Website;
import org.hsse.news.database.website.models.WebsiteId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NewsBot extends TelegramLongPollingBot {
    private final static String START_COMMAND = "/start";
    private final static String MENU_COMMAND = "/menu";
    private final static String WEBSITES_MENU_COMMAND = "/websites";
    private final static String LIST_SUBBED_COMMAND = "/subs";
    private final static String LIST_NOT_SUBBED_COMMAND = "/browse";
    private final static String VIEW_COMMAND = "/view-website";
    private final static String SUB_COMMAND = "/sub";
    private final static String UNSUB_COMMAND = "/unsub";
    private final static String SUB_CUSTOM_COMMAND = "/sub-custom";
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
    private final static String CLOSE_COMMAND = "/close";

    private final Map<Long, ChatState> chatStates = new ConcurrentHashMap<>();
    private final Map<Long, Integer> latestMenuMessageId = new ConcurrentHashMap<>();

    enum ChatState {
        NORMAL,
        AWAITING_CUSTOM_WEBSITE_URI,
        AWAITING_CUSTOM_TOPIC_NAME
    }

    @Autowired
    public NewsBot(final Environment environment) {
        super(environment.getProperty("bot-token"));
    }

    @Override
    public String getBotUsername() {
        return "HsseNewsTeam1Bot";
    }

    private List<Website> getSubbedWebsites() {
        return List.of(new Website(new WebsiteId(0L), "example.org", "example",
                new UserId(UUID.fromString("027e71c2-f90b-43b9-8dbf-5e7f2da771ae"))));
    }

    private List<Website> getUnsubbedWebsites() {
        return List.of(new Website(new WebsiteId(1L), "example.org", "example2",
                new UserId(UUID.fromString("027e71c2-f90b-43b9-8dbf-5e7f2da771ae"))));
    }

    private Optional<Website> findWebsite(WebsiteId id) {
        return switch (id.value().intValue()) {
            case 0 -> Optional.of(new Website(new WebsiteId(0L),
                    "example.org", "example",
                    new UserId(UUID.fromString("027e71c2-f90b-43b9-8dbf-5e7f2da771ae"))));
            case 1 -> Optional.of(new Website(new WebsiteId(1L),
                    "example.org", "example2",
                    new UserId(UUID.fromString("027e71c2-f90b-43b9-8dbf-5e7f2da771ae"))));
            default -> Optional.empty();
        };
    }

    private boolean isSubbed(WebsiteId id) {
        return id.value() == 0;
    }

    private List<Topic> getSubbedTopics() {
        return List.of(new Topic(new TopicId(0L), "test"));
    }

    private List<Topic> getUnsubbedTopics() {
        return List.of(new Topic(new TopicId(1L), "test2"));
    }

    private Optional<Topic> findTopic(TopicId id) {
        return switch (id.value().intValue()) {
            case 0 -> Optional.of(new Topic(new TopicId(0L), "test"));
            case 1 -> Optional.of(new Topic(new TopicId(1L), "test2"));
            default -> Optional.empty();
        };
    }

    private boolean isSubbed(TopicId id) {
        return id.value() == 0;
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
                        .callbackData(LIST_SUBBED_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text("Не подписан")
                        .callbackData(LIST_NOT_SUBBED_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text("Добавить...")
                        .callbackData(SUB_CUSTOM_COMMAND).build())));
    }

    private InlineKeyboardMarkup buildWebsitesListMenu(List<Website> websites) {
        return new InlineKeyboardMarkup(websites.stream().map(
                website -> List.of(InlineKeyboardButton.builder()
                        .text(website.description())
                        .callbackData(VIEW_COMMAND + " " + website.id().value()).build())).toList());
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
                        .callbackData(SUB_CUSTOM_TOPIC_COMMAND).build())));
    }

    private InlineKeyboardMarkup getSubbedTopicsMenu() {
        return new InlineKeyboardMarkup(getSubbedTopics().stream().map(
                topic -> List.of(InlineKeyboardButton.builder()
                        .text(topic.description())
                        .callbackData(VIEW_TOPIC_COMMAND + " " + topic.id()).build())).toList());
    }

    private InlineKeyboardMarkup getUnsubbedTopicsMenu() {
        return new InlineKeyboardMarkup(getUnsubbedTopics().stream().map(
                topic -> List.of(InlineKeyboardButton.builder()
                        .text(topic.description())
                        .callbackData(VIEW_TOPIC_COMMAND + " " + topic.id()).build())).toList());
    }

    private static InlineKeyboardMarkup getNeutralArticle(ArticleId id) {
        return new InlineKeyboardMarkup(List.of(List.of(
                InlineKeyboardButton.builder()
                        .text("\uD83D\uDC4D")
                        .callbackData(LIKE_COMMAND + " " + id.value()).build(),
                InlineKeyboardButton.builder()
                        .text("\uD83D\uDC4E")
                        .callbackData(DISLIKE_COMMAND + " " + id.value()).build())));
    }

    private static InlineKeyboardMarkup getLikedArticle(ArticleId id) {
        return new InlineKeyboardMarkup(List.of(List.of(
                InlineKeyboardButton.builder()
                        .text("✅\uD83D\uDC4D")
                        .callbackData(UNLIKE_COMMAND + " " + id.value()).build(),
                InlineKeyboardButton.builder()
                        .text("\uD83D\uDC4E")
                        .callbackData(DISLIKE_COMMAND + " " + id.value()).build())));
    }

    private static InlineKeyboardMarkup getDislikedArticle(ArticleId id) {
        return new InlineKeyboardMarkup(List.of(List.of(
                InlineKeyboardButton.builder()
                        .text("\uD83D\uDC4D")
                        .callbackData(LIKE_COMMAND + " " + id.value()).build(),
                InlineKeyboardButton.builder()
                        .text("✅\uD83D\uDC4E")
                        .callbackData(UNDISLIKE_COMMAND + " " + id.value()).build())));
    }

    private void sendMenuMessage(final long chatId, final String text,
                                 final InlineKeyboardMarkup keyboard)
            throws TelegramApiException {
        final Integer messageId = latestMenuMessageId.get(chatId);
        if (messageId == null) {
            final SendMessage message = new SendMessage();
            message.setChatId(chatId);

            message.setText(text);
            message.setReplyMarkup(keyboard);

            int id = execute(message).getMessageId();
            latestMenuMessageId.put(chatId, id);
        } else {
            final EditMessageText edit = new EditMessageText();
            edit.setChatId(chatId);
            edit.setMessageId(messageId);
            edit.setText(text);
            edit.setReplyMarkup(keyboard);

            execute(edit);
        }
    }

    private Optional<WebsiteId> parseWebsiteIdArg(String text, String command) {
        if (text.toLowerCase().startsWith(command)) {
            return Optional.of(new WebsiteId(Long.parseLong(text.substring(command.length()).strip())));
        } else {
            return Optional.empty();
        }
    }

    private Optional<TopicId> parseTopicIdArg(String text, String command) {
        if (text.toLowerCase().startsWith(command)) {
            return Optional.of(new TopicId(Long.parseLong(text.substring(command.length()).strip())));
        } else {
            return Optional.empty();
        }
    }

    private Optional<ArticleId> parseArticleIdArg(String text, String command) {
        if (text.toLowerCase().startsWith(command)) {
            return Optional.of(new ArticleId(UUID.fromString(text.substring(command.length()).strip())));
        } else {
            return Optional.empty();
        }
    }

    private boolean handleViewWebsite(long chatId, String text)
            throws TelegramApiException {
        final Optional<WebsiteId> id = parseWebsiteIdArg(text, VIEW_COMMAND);
        if (id.isPresent()) {
            final Website website = findWebsite(id.get()).orElseThrow();
            final String subCommandName = isSubbed(id.get()) ? "Отписаться" : "Подписаться";
            final String subCommand = isSubbed(id.get()) ? UNSUB_TOPIC_COMMAND : SUB_TOPIC_COMMAND;

            sendMenuMessage(chatId, website.description() + "\n" + website.url(),
                    new InlineKeyboardMarkup(List.of(
                            List.of(InlineKeyboardButton.builder()
                                    .text(subCommandName)
                                    .callbackData(subCommand + " " + id.get()).build()))));
            return true;
        } else {
            return false;
        }
    }

    private boolean handleWebsitesCommand(long chatId, String text)
            throws TelegramApiException {
        if (WEBSITES_MENU_COMMAND.equalsIgnoreCase(text)) {
            sendMenuMessage(chatId, "Источники", getWebsitesMenu());
        } else if (LIST_SUBBED_COMMAND.equalsIgnoreCase(text)) {
            sendMenuMessage(chatId, "Подписки:", buildWebsitesListMenu(getSubbedWebsites()));
        } else if (LIST_NOT_SUBBED_COMMAND.equalsIgnoreCase(text)) {
            sendMenuMessage(chatId, "Вы не подписаны на:", buildWebsitesListMenu(getUnsubbedWebsites()));
        } else if (SUB_CUSTOM_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, ChatState.AWAITING_CUSTOM_WEBSITE_URI);
            sendMenuMessage(chatId, "Введите URI:", null);
        } else if (parseWebsiteIdArg(text, SUB_COMMAND).isPresent()) {
            final WebsiteId id = parseWebsiteIdArg(text, SUB_COMMAND).get();
            sendMenuMessage(chatId, "Предстааавьте, что вы подписались на вебсайт " + id, null);
        } else if (parseWebsiteIdArg(text, UNSUB_COMMAND).isPresent()) {
            final WebsiteId id = parseWebsiteIdArg(text, UNSUB_COMMAND).get();
            sendMenuMessage(chatId, "Предстааавьте, что вы отписались от вебсайта " + id, null);
        } else {
            return handleViewWebsite(chatId, text);
        }
        return true;
    }

    private boolean handleViewTopic(long chatId, String text)
            throws TelegramApiException {
        final Optional<TopicId> id = parseTopicIdArg(text, VIEW_TOPIC_COMMAND);
        if (id.isPresent()) {
            final Topic topic = findTopic(id.get()).orElseThrow();
            final String subCommandName = isSubbed(id.get()) ? "Отписаться" : "Подписаться";
            final String subCommand = isSubbed(id.get()) ? UNSUB_TOPIC_COMMAND : SUB_TOPIC_COMMAND;

            sendMenuMessage(chatId, topic.description(),
                    new InlineKeyboardMarkup(List.of(
                            List.of(InlineKeyboardButton.builder()
                                    .text(subCommandName)
                                    .callbackData(subCommand + " " + id.get()).build()))));
            return true;
        } else {
            return false;
        }
    }

    private boolean handleTopicsCommand(long chatId, String text)
            throws TelegramApiException {
        if (TOPICS_MENU_COMMAND.equalsIgnoreCase(text)) {
            sendMenuMessage(chatId, "Темы", getTopicsMenu());
        } else if (LIST_SUBBED_TOPICS_COMMAND.equalsIgnoreCase(text)) {
            sendMenuMessage(chatId, "Подписки:", getSubbedTopicsMenu());
        } else if (LIST_NOT_SUBBED_TOPICS_COMMAND.equalsIgnoreCase(text)) {
            sendMenuMessage(chatId, "Вы не подписаны на:", getUnsubbedTopicsMenu());
        } else if (SUB_CUSTOM_TOPIC_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, ChatState.AWAITING_CUSTOM_TOPIC_NAME);
            sendMenuMessage(chatId, "Введите название темы:", null);
        } else if (parseTopicIdArg(text, SUB_TOPIC_COMMAND).isPresent()) {
            final TopicId id = parseTopicIdArg(text, SUB_TOPIC_COMMAND).get();
            sendMenuMessage(chatId, "Предстааавьте, что вы подписались на тему " + id, null);
        } else if (parseTopicIdArg(text, UNSUB_TOPIC_COMMAND).isPresent()) {
            final TopicId id = parseTopicIdArg(text, UNSUB_TOPIC_COMMAND).get();
            sendMenuMessage(chatId, "Предстааавьте, что вы отписались от темы " + id, null);
        } else {
            return handleViewTopic(chatId, text);
        }
        return true;
    }

    private boolean handleLikesCommand(long chatId, String text)
            throws TelegramApiException {
        if (parseArticleIdArg(text, LIKE_COMMAND).isPresent()) {
            final ArticleId id = parseArticleIdArg(text, LIKE_COMMAND).get();
            sendMenuMessage(chatId, "Предстааавьте, что мы поставили лайк на статью " + id, null);
        } else if (parseArticleIdArg(text, DISLIKE_COMMAND).isPresent()) {
            final ArticleId id = parseArticleIdArg(text, DISLIKE_COMMAND).get();
            sendMenuMessage(chatId, "Предстааавьте, что мы убрали лайк со статьи " + id, null);
        } else if (parseArticleIdArg(text, UNLIKE_COMMAND).isPresent()) {
            final ArticleId id = parseArticleIdArg(text, UNLIKE_COMMAND).get();
            sendMenuMessage(chatId, "Предстааавьте, что мы поставили дизлайк на статью " + id, null);
        } else if (parseArticleIdArg(text, UNDISLIKE_COMMAND).isPresent()) {
            final ArticleId id = parseArticleIdArg(text, UNDISLIKE_COMMAND).get();
            sendMenuMessage(chatId, "Предстааавьте, что мы убрали дизлайк со статьи " + id, null);
        } else {
            return false;
        }
        return true;
    }

    private void handleCommand(long chatId, String text)
            throws TelegramApiException {
        if (START_COMMAND.equalsIgnoreCase(text)) {
            sendMenuMessage(chatId, "Привет! " +
                    "Добавь источники и ты сможешь смотреть ленту новостей в этом боте! ", getMainMenu());
        } else if (MENU_COMMAND.equalsIgnoreCase(text)) {
            sendMenuMessage(chatId, "Меню", getMainMenu());
        } else if (!handleWebsitesCommand(chatId, text)
                && !handleTopicsCommand(chatId, text)
                && !handleLikesCommand(chatId, text)) {
            sendMenuMessage(chatId, "Операция " + text + " не поддерживается", null);
        }
    }

    private void handleInput(long chatId, String text) throws TelegramApiException {
        switch (chatStates.getOrDefault(chatId, ChatState.NORMAL)) {
            case NORMAL -> handleCommand(chatId, text);
            case AWAITING_CUSTOM_WEBSITE_URI -> sendMenuMessage(
                    chatId, "Источник " + text + " добавлен", getWebsitesMenu());
            case AWAITING_CUSTOM_TOPIC_NAME -> sendMenuMessage(
                    chatId, "Тема " + text + " добавлена", getTopicsMenu());
        }
        chatStates.put(chatId, ChatState.NORMAL);
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(final Update update) {
        if (update.hasMessage()) {
            handleInput(update.getMessage().getChatId(), update.getMessage().getText());
        } else if (update.hasCallbackQuery()) {
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            handleCommand(chatId, update.getCallbackQuery().getData());
            chatStates.put(chatId, ChatState.NORMAL);
        }
    }
}
