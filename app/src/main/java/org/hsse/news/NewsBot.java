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
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
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

    private final Map<Long, ChatState> chatStates = new ConcurrentHashMap<>();

    private sealed interface ChatState {
    }

    private record NormalChatState(Optional<Long> post, Optional<Boolean> liked) implements ChatState {
        public NormalChatState() {
            this(Optional.empty(), Optional.empty());
        }

        public NormalChatState(final long post) {
            this(Optional.of(post), Optional.empty());
        }

        public NormalChatState(final long post, final boolean liked) {
            this(Optional.of(post), Optional.of(liked));
        }

        boolean isLiked() {
            return liked.equals(Optional.of(true));
        }

        boolean isDisliked() {
            return liked.equals(Optional.of(false));
        }
    }

    private static final class WebsitesChatState implements ChatState {
    }

    private static final class AwaitingUriChatState implements ChatState {
    }

    private static final class TopicsChatState implements ChatState {
    }

    private static final class AwaitingTopicChatState implements ChatState {
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

    private ReplyKeyboard getMainMenu() {
        return new InlineKeyboardMarkup(List.of(
                List.of(InlineKeyboardButton.builder()
                        .text(WEBSITES_MENU_COMMAND)
                        .callbackData(WEBSITES_MENU_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text(TOPICS_MENU_COMMAND)
                        .callbackData(TOPICS_MENU_COMMAND).build())));
    }

    private ReplyKeyboard getWebsitesMenu() {
        return new InlineKeyboardMarkup(List.of(
                List.of(InlineKeyboardButton.builder()
                        .text(LIST_SUBBED_COMMAND)
                        .callbackData(LIST_SUBBED_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text(LIST_NOT_SUBBED_COMMAND)
                        .callbackData(LIST_NOT_SUBBED_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text(SUB_CUSTOM_COMMAND)
                        .callbackData(SUB_CUSTOM_COMMAND).build())));
    }

    private ReplyKeyboard getSubbedWebsitesMenu() {
        return new InlineKeyboardMarkup(getSubbedWebsites().stream().map(
                website -> List.of(InlineKeyboardButton.builder()
                        .text(website.description())
                        .callbackData(VIEW_COMMAND + " " + website.id().value()).build())).toList());
    }

    private ReplyKeyboard getUnsubbedWebsitesMenu() {
        return new InlineKeyboardMarkup(getUnsubbedWebsites().stream().map(
                website -> List.of(InlineKeyboardButton.builder()
                        .text(website.description())
                        .callbackData(VIEW_COMMAND + " " + website.id().value()).build())).toList());
    }

    private ReplyKeyboard getViewingSubbedSubbableMenu() {
        return new InlineKeyboardMarkup(List.of(
                List.of(InlineKeyboardButton.builder()
                        .text(UNSUB_COMMAND)
                        .callbackData(UNSUB_COMMAND).build())));
    }

    private ReplyKeyboard getViewingUnsubbedSubbableMenu() {
        return new InlineKeyboardMarkup(List.of(
                List.of(InlineKeyboardButton.builder()
                        .text(SUB_COMMAND)
                        .callbackData(SUB_COMMAND).build())));
    }

    private ReplyKeyboard getTopicsMenu() {
        return new InlineKeyboardMarkup(List.of(
                List.of(InlineKeyboardButton.builder()
                        .text(LIST_SUBBED_TOPICS_COMMAND)
                        .callbackData(LIST_SUBBED_TOPICS_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text(LIST_NOT_SUBBED_TOPICS_COMMAND)
                        .callbackData(LIST_NOT_SUBBED_TOPICS_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text(SUB_CUSTOM_TOPIC_COMMAND)
                        .callbackData(SUB_CUSTOM_TOPIC_COMMAND).build())));
    }

    private ReplyKeyboard getSubbedTopicsMenu() {
        return new InlineKeyboardMarkup(getSubbedTopics().stream().map(
                topic -> List.of(InlineKeyboardButton.builder()
                        .text(topic.description())
                        .callbackData(VIEW_TOPIC_COMMAND + " " + topic.id()).build())).toList());
    }

    private ReplyKeyboard getUnsubbedTopicsMenu() {
        return new InlineKeyboardMarkup(getUnsubbedTopics().stream().map(
                topic -> List.of(InlineKeyboardButton.builder()
                        .text(topic.description())
                        .callbackData(VIEW_TOPIC_COMMAND + " " + topic.id()).build())).toList());
    }

    private static ReplyKeyboard getNeutralArticle(ArticleId id) {
        return new InlineKeyboardMarkup(List.of(List.of(
                InlineKeyboardButton.builder()
                        .text(LIKE_COMMAND)
                        .callbackData(LIKE_COMMAND + " " + id.value()).build(),
                InlineKeyboardButton.builder()
                        .text(DISLIKE_COMMAND)
                        .callbackData(DISLIKE_COMMAND + " " + id.value()).build())));
    }

    private static ReplyKeyboard getLikedArticle(ArticleId id) {
        return new InlineKeyboardMarkup(List.of(List.of(
                InlineKeyboardButton.builder()
                        .text(UNLIKE_COMMAND)
                        .callbackData(UNLIKE_COMMAND + " " + id.value()).build(),
                InlineKeyboardButton.builder()
                        .text(DISLIKE_COMMAND)
                        .callbackData(DISLIKE_COMMAND + " " + id.value()).build())));
    }

    private static ReplyKeyboard getDislikedArticle(ArticleId id) {
        return new InlineKeyboardMarkup(List.of(List.of(
                InlineKeyboardButton.builder()
                        .text(LIKE_COMMAND)
                        .callbackData(LIKE_COMMAND + " " + id.value()).build(),
                InlineKeyboardButton.builder()
                        .text(UNDISLIKE_COMMAND)
                        .callbackData(UNDISLIKE_COMMAND + " " + id.value()).build())));
    }

    private void sendMessage(final long chatId, final String text, final ReplyKeyboard keyboard)
            throws TelegramApiException {
        final SendMessage message = new SendMessage();
        message.setChatId(chatId);

        message.setText(text);
        message.setReplyMarkup(keyboard);

        execute(message);
    }

    private void handleCommand(long chatId, final ChatState state, String text)
            throws TelegramApiException {
        if (state == null) {
            chatStates.put(chatId, new NormalChatState());
        }

        if (START_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, new NormalChatState());
            sendMessage(chatId, "Привет! " +
                    "Добавь источники и ты сможешь смотреть ленту новостей в этом боте! ", getMainMenu());
        } else if (MENU_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, new NormalChatState());
            sendMessage(chatId, "", getMainMenu());
        } else if (WEBSITES_MENU_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, new WebsitesChatState());
            sendMessage(chatId, "Источники", getWebsitesMenu());
        } else if (LIST_SUBBED_COMMAND.equalsIgnoreCase(text)) {
            sendMessage(chatId, "Подписки:", getSubbedWebsitesMenu());
        } else if (LIST_NOT_SUBBED_COMMAND.equalsIgnoreCase(text)) {
            sendMessage(chatId, "Вы не подписаны на:", getUnsubbedWebsitesMenu());
        } else if (text.toLowerCase().startsWith(VIEW_COMMAND)) {
            final WebsiteId id = new WebsiteId(Long.parseLong(text.substring(VIEW_COMMAND.length()).strip()));
            final Website website = findWebsite(id).orElseThrow();
            final String subAction = isSubbed(id) ? UNSUB_COMMAND : SUB_COMMAND;

            sendMessage(chatId, website.description() + "\n" + website.url(),
                    new InlineKeyboardMarkup(List.of(
                            List.of(InlineKeyboardButton.builder()
                                    .text(subAction)
                                    .callbackData(subAction + " " + id).build()))));
        } else if (SUB_CUSTOM_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, new AwaitingUriChatState());
            sendMessage(chatId, "Введите URI:", null);
        } else if (text.toLowerCase().startsWith(SUB_COMMAND)) {
            final WebsiteId id = new WebsiteId(Long.parseLong(text.substring(SUB_COMMAND.length()).strip()));
            sendMessage(chatId, "Предстааавьте, что вы подписались на вебсайт " + id, null);
        } else if (text.toLowerCase().startsWith(UNSUB_COMMAND)) {
            final WebsiteId id = new WebsiteId(Long.parseLong(text.substring(UNSUB_COMMAND.length()).strip()));
            sendMessage(chatId, "Предстааавьте, что вы отписались от вебсайта " + id, null);
        } else if (TOPICS_MENU_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, new TopicsChatState());
            sendMessage(chatId, "Темы", getTopicsMenu());
        } else if (LIST_SUBBED_TOPICS_COMMAND.equalsIgnoreCase(text)) {
            sendMessage(chatId, "Подписки:", getSubbedTopicsMenu());
        } else if (LIST_NOT_SUBBED_TOPICS_COMMAND.equalsIgnoreCase(text)) {
            sendMessage(chatId, "Вы не подписаны на:", getUnsubbedTopicsMenu());
        } else if (text.toLowerCase().startsWith(VIEW_TOPIC_COMMAND)) {
            final TopicId id = new TopicId(Long.parseLong(text.substring(VIEW_TOPIC_COMMAND.length()).strip()));
            final Topic topic = findTopic(id).orElseThrow();
            final String subAction = isSubbed(id) ? UNSUB_TOPIC_COMMAND : SUB_TOPIC_COMMAND;

            sendMessage(chatId, topic.description(),
                    new InlineKeyboardMarkup(List.of(
                            List.of(InlineKeyboardButton.builder()
                                    .text(subAction)
                                    .callbackData(subAction + " " + id).build()))));
        } else if (SUB_CUSTOM_TOPIC_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, new AwaitingTopicChatState());
            sendMessage(chatId, "Введите название темы:", null);
        } else if (text.toLowerCase().startsWith(SUB_TOPIC_COMMAND)) {
            final TopicId id = new TopicId(Long.parseLong(text.substring(SUB_TOPIC_COMMAND.length()).strip()));
            sendMessage(chatId, "Предстааавьте, что вы подписались на тему " + id, null);
        } else if (text.toLowerCase().startsWith(UNSUB_TOPIC_COMMAND)) {
            final TopicId id = new TopicId(Long.parseLong(text.substring(UNSUB_TOPIC_COMMAND.length()).strip()));
            sendMessage(chatId, "Предстааавьте, что вы отписались от темы " + id, null);
        } else if (text.toLowerCase().startsWith(LIKE_COMMAND)) {
            final ArticleId id = new ArticleId(UUID.fromString(text.substring(LIKE_COMMAND.length()).strip()));
            sendMessage(chatId, "Предстааавьте, что мы поставили лайк на статью " + id, null);
        } else if (text.toLowerCase().startsWith(DISLIKE_COMMAND)) {
            final ArticleId id = new ArticleId(UUID.fromString(text.substring(DISLIKE_COMMAND.length()).strip()));
            sendMessage(chatId, "Предстааавьте, что мы убрали лайк со статьи " + id, null);
        } else if (text.toLowerCase().startsWith(UNLIKE_COMMAND)) {
            final ArticleId id = new ArticleId(UUID.fromString(text.substring(UNLIKE_COMMAND.length()).strip()));
            sendMessage(chatId, "Предстааавьте, что мы поставили дизлайк на статью " + id, null);
        } else if (text.toLowerCase().startsWith(UNDISLIKE_COMMAND)) {
            final ArticleId id = new ArticleId(UUID.fromString(text.substring(UNDISLIKE_COMMAND.length()).strip()));
            sendMessage(chatId, "Предстааавьте, что мы убрали дизлайк со статьи " + id, null);
        } else {
            sendMessage(chatId, "Операция " + text + " не поддерживается", null);
        }
    }

    private void handleInput(long chatId, String text) throws TelegramApiException {
        final ChatState state = chatStates.get(chatId);

        if (state == null || START_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, new NormalChatState());
            sendMessage(chatId, "Привет! " +
                    "Добавь источники и ты сможешь смотреть ленту новостей в этом боте! ", getMainMenu());
        } else if (state instanceof AwaitingUriChatState) {
            chatStates.put(chatId, new WebsitesChatState());
            sendMessage(chatId, "Источник " + text + " добавлен", getWebsitesMenu());
        } else if (state instanceof AwaitingTopicChatState) {
            chatStates.put(chatId, new WebsitesChatState());
            sendMessage(chatId, "Источник " + text + " добавлен", getTopicsMenu());
        } else {
            handleCommand(chatId, state, text);
        }
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(final Update update) {
        if (update.hasMessage()) {
            handleInput(update.getMessage().getChatId(), update.getMessage().getText());
        } else if (update.hasCallbackQuery()) {
            handleCommand(update.getCallbackQuery().getMessage().getChatId(),
                    chatStates.get(update.getCallbackQuery().getMessage().getChatId()),
                    update.getCallbackQuery().getData());
        }
    }
}
