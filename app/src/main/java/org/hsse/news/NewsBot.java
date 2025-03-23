package org.hsse.news;

import lombok.SneakyThrows;
import org.hsse.news.database.article.models.Article;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.topic.models.Topic;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.website.models.Website;
import org.hsse.news.database.website.models.WebsiteId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
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
    private final static String SEND_TEST_ARTICLE_COMMAND = "/test-feed";

    private final static String BACK_TEXT = "Назад";

    private final Map<Long, ChatState> chatStates = new ConcurrentHashMap<>();
    private final Map<Long, Integer> latestMenuMessageId = new ConcurrentHashMap<>();

    enum ChatState {
        NORMAL,
        AWAITING_CUSTOM_WEBSITE_URI,
        AWAITING_CUSTOM_TOPIC_NAME
    }

    private final StubDataProvider dataProvider;

    @Autowired
    public NewsBot(final Environment environment, final StubDataProvider dataProvider) {
        super(environment.getProperty("bot-token"));
        this.dataProvider = dataProvider;
    }

    @Override
    public String getBotUsername() {
        return "HsseNewsTeam1Bot";
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
                        .callbackData(SUB_CUSTOM_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text(BACK_TEXT)
                        .callbackData("/menu").build())));
    }

    private InlineKeyboardMarkup buildWebsitesListMenu(final List<Website> websites) {
        final List<List<InlineKeyboardButton>> buttons = new ArrayList<>(
                websites.stream().map(
                        website -> List.of(InlineKeyboardButton.builder()
                                .text(website.description())
                                .callbackData(VIEW_COMMAND + " " + website.id().value())
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

    private void sendMenuMessage(final long chatId, final String text,
                                 final InlineKeyboardMarkup keyboard)
            throws TelegramApiException {
        final Integer messageId = latestMenuMessageId.get(chatId);
        if (messageId == null) {
            final SendMessage message = new SendMessage();
            message.setChatId(chatId);

            message.setText(text);
            message.setReplyMarkup(keyboard);

            final int id = execute(message).getMessageId();
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

    private Optional<WebsiteId> parseWebsiteIdArg(final String text, final String command) {
        if (text.toLowerCase(Locale.US).startsWith(command)) {
            return Optional.of(new WebsiteId(Long.parseLong(text.substring(command.length()).strip())));
        } else {
            return Optional.empty();
        }
    }

    private Optional<TopicId> parseTopicIdArg(final String text, final String command) {
        if (text.toLowerCase(Locale.US).startsWith(command)) {
            return Optional.of(new TopicId(Long.parseLong(text.substring(command.length()).strip())));
        } else {
            return Optional.empty();
        }
    }

    record SendArticleData(ArticleId id, int messageId) {
    }

    private Optional<SendArticleData> parseSendArticleDataArg(final String text, final String command) {
        if (text.toLowerCase(Locale.US).startsWith(command)) {
            final List<String> params = Arrays.stream(
                            text.substring(command.length()).split(" "))
                    .filter((string) -> !string.isBlank()).toList();
            final int requiredParamCount = 2;
            if (params.size() != requiredParamCount) {
                return Optional.empty();
            }
            return Optional.of(new SendArticleData(
                    new ArticleId(UUID.fromString(params.get(0).strip())),
                    Integer.parseInt(params.get(1).strip())));
        } else {
            return Optional.empty();
        }
    }

    private boolean handleViewWebsite(final long chatId, final String text)
            throws TelegramApiException {
        final Optional<WebsiteId> id = parseWebsiteIdArg(text, VIEW_COMMAND);
        if (id.isPresent()) {
            final Website website = dataProvider.findWebsite(id.get()).orElseThrow();
            final String subCommand =
                    (dataProvider.isSubbed(id.get()) ? UNSUB_TOPIC_COMMAND : SUB_TOPIC_COMMAND)
                            + " " + id.get();

            sendMenuMessage(chatId, website.description() + "\n" + website.url(),
                    new InlineKeyboardMarkup(List.of(
                            List.of(InlineKeyboardButton.builder()
                                    .text(dataProvider.isSubbed(id.get()) ? "Отписаться" : "Подписаться")
                                    .callbackData(subCommand + " " + id.get()).build()),
                            List.of(InlineKeyboardButton.builder()
                                    .text(BACK_TEXT)
                                    .callbackData(dataProvider.isSubbed(id.get())
                                            ? LIST_SUBBED_COMMAND
                                            : LIST_NOT_SUBBED_COMMAND)
                                    .build()))));
            return true;
        } else {
            return false;
        }
    }

    private boolean handleWebsitesCommand(final long chatId, final String text)
            throws TelegramApiException {
        if (WEBSITES_MENU_COMMAND.equalsIgnoreCase(text)) {
            sendMenuMessage(chatId, "Источники", getWebsitesMenu());
        } else if (LIST_SUBBED_COMMAND.equalsIgnoreCase(text)) {
            sendMenuMessage(chatId, "Подписки:",
                    buildWebsitesListMenu(dataProvider.getSubbedWebsites()));
        } else if (LIST_NOT_SUBBED_COMMAND.equalsIgnoreCase(text)) {
            sendMenuMessage(chatId, "Вы не подписаны на:",
                    buildWebsitesListMenu(dataProvider.getUnsubbedWebsites()));
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

    private void sendViewTopic(final long chatId, final TopicId id) throws TelegramApiException {
        final Topic topic = dataProvider.findTopic(id).orElseThrow();
        final String subCommandName = dataProvider.isSubbed(id) ? "Отписаться" : "Подписаться";
        final String subCommand = dataProvider.isSubbed(id) ? UNSUB_TOPIC_COMMAND : SUB_TOPIC_COMMAND;

        sendMenuMessage(chatId, topic.description(),
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

    private boolean handleTopicsCommand(final long chatId, final String text)
            throws TelegramApiException {
        if (TOPICS_MENU_COMMAND.equalsIgnoreCase(text)) {
            sendMenuMessage(chatId, "Темы", getTopicsMenu());
        } else if (LIST_SUBBED_TOPICS_COMMAND.equalsIgnoreCase(text)) {
            sendMenuMessage(chatId, "Подписки:",
                    buildTopicListMenu(dataProvider.getSubbedTopics()));
        } else if (LIST_NOT_SUBBED_TOPICS_COMMAND.equalsIgnoreCase(text)) {
            sendMenuMessage(chatId, "Вы не подписаны на:",
                    buildTopicListMenu(dataProvider.getUnsubbedTopics()));
        } else if (parseTopicIdArg(text, VIEW_TOPIC_COMMAND).isPresent()) {
            final TopicId id = parseTopicIdArg(text, SUB_TOPIC_COMMAND).get();
            sendViewTopic(chatId, id);
        } else if (SUB_CUSTOM_TOPIC_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, ChatState.AWAITING_CUSTOM_TOPIC_NAME);
            sendMenuMessage(chatId, "Введите название темы:",
                    new InlineKeyboardMarkup(List.of(List.of(
                            InlineKeyboardButton.builder()
                                    .text("Отмена")
                                    .callbackData(WEBSITES_MENU_COMMAND).build()))));
        } else if (parseTopicIdArg(text, SUB_TOPIC_COMMAND).isPresent()) {
            final TopicId id = parseTopicIdArg(text, SUB_TOPIC_COMMAND).get();
            sendViewTopic(chatId, id);
        } else if (parseTopicIdArg(text, UNSUB_TOPIC_COMMAND).isPresent()) {
            final TopicId id = parseTopicIdArg(text, UNSUB_TOPIC_COMMAND).get();
            sendViewTopic(chatId, id);
        } else {
            return false;
        }
        return true;
    }

    private boolean handleLikesCommand(final long chatId, final String text)
            throws TelegramApiException {
        if (parseSendArticleDataArg(text, LIKE_COMMAND).isPresent()) {
            final SendArticleData data = parseSendArticleDataArg(text, LIKE_COMMAND).get();
            updateArticleMenu(data.id(), ArticleOpinion.LIKED, chatId, data.messageId());
        } else if (parseSendArticleDataArg(text, DISLIKE_COMMAND).isPresent()) {
            final SendArticleData data = parseSendArticleDataArg(text, DISLIKE_COMMAND).get();
            updateArticleMenu(data.id(), ArticleOpinion.DISLIKED, chatId, data.messageId());
        } else if (parseSendArticleDataArg(text, UNLIKE_COMMAND).isPresent()) {
            final SendArticleData data = parseSendArticleDataArg(text, UNLIKE_COMMAND).get();
            updateArticleMenu(data.id(), ArticleOpinion.NEUTRAL, chatId, data.messageId());
        } else if (parseSendArticleDataArg(text, UNDISLIKE_COMMAND).isPresent()) {
            final SendArticleData data = parseSendArticleDataArg(text, UNDISLIKE_COMMAND).get();
            updateArticleMenu(data.id(), ArticleOpinion.NEUTRAL, chatId, data.messageId());
        } else {
            return false;
        }
        return true;
    }

    private void handleCommand(final long chatId, final String text)
            throws TelegramApiException {
        if (START_COMMAND.equalsIgnoreCase(text)) {
            sendMenuMessage(chatId, "Привет! " +
                            "Добавь источники и ты сможешь смотреть ленту новостей в этом боте! ",
                    getMainMenu());
        } else if (MENU_COMMAND.equalsIgnoreCase(text)) {
            sendMenuMessage(chatId, "Меню", getMainMenu());
        } else if (SEND_TEST_ARTICLE_COMMAND.equalsIgnoreCase(text)) {
            sendArticle(chatId, dataProvider.getExampleArticle(), ArticleOpinion.NEUTRAL);
        } else if (!handleWebsitesCommand(chatId, text)
                && !handleTopicsCommand(chatId, text)
                && !handleLikesCommand(chatId, text)) {
            sendMenuMessage(chatId, "Операция " + text + " не поддерживается", getMainMenu());
        }
    }

    public enum ArticleOpinion {
        LIKED,
        NEUTRAL,
        DISLIKED
    }

    private void updateArticleMenu(
            final ArticleId id, final ArticleOpinion opinion, final long chatId, final int messageId)
            throws TelegramApiException {
        final EditMessageReplyMarkup edit = new EditMessageReplyMarkup();
        edit.setChatId(chatId);
        edit.setMessageId(messageId);
        edit.setReplyMarkup(new InlineKeyboardMarkup(List.of(List.of(
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
                        .callbackData(DISLIKE_COMMAND + " " + id.value() + " " + messageId).build()))));
        execute(edit);
    }

    public void sendArticle(final long chatId, final Article article, final ArticleOpinion articleOpinion)
            throws TelegramApiException {
        if (latestMenuMessageId.containsKey(chatId)) {
            deleteMessage(chatId, latestMenuMessageId.get(chatId));
        }

        final SendMessage message = new SendMessage();
        message.setChatId(chatId);

        message.setText(article.title() + "\n" + article.url());

        final int messageId = execute(message).getMessageId();
        updateArticleMenu(article.id(), articleOpinion, chatId, messageId);
    }

    private void deleteMessage(final long chatId, final int messageId)
            throws TelegramApiException {
        final DeleteMessage request = new DeleteMessage();
        request.setChatId(chatId);
        request.setMessageId(messageId);
        execute(request);
    }

    private void handleInput(final long chatId, final String text, final int messageId)
            throws TelegramApiException {
        switch (chatStates.getOrDefault(chatId, ChatState.NORMAL)) {
            case NORMAL -> handleCommand(chatId, text);
            case AWAITING_CUSTOM_WEBSITE_URI -> {
                sendMenuMessage(chatId, "Источник " + text + " добавлен", getWebsitesMenu());
                deleteMessage(chatId, messageId);
            }
            case AWAITING_CUSTOM_TOPIC_NAME -> {
                sendMenuMessage(chatId, "Тема " + text + " добавлена", getTopicsMenu());
                deleteMessage(chatId, messageId);
            }
        }
        chatStates.put(chatId, ChatState.NORMAL);
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(final Update update) {
        if (update.hasMessage()) {
            handleInput(update.getMessage().getChatId(), update.getMessage().getText(),
                    update.getMessage().getMessageId());
            deleteMessage(update.getMessage().getChatId(), update.getMessage().getMessageId());
        } else if (update.hasCallbackQuery()) {
            final long chatId = update.getCallbackQuery().getMessage().getChatId();
            chatStates.put(chatId, ChatState.NORMAL);
            handleCommand(chatId, update.getCallbackQuery().getData());
        }
    }
}
