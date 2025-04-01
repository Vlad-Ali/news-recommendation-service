package org.hsse.news.bot;

import lombok.SneakyThrows;
import org.glassfish.jersey.internal.util.Producer;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.website.models.WebsiteId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final Set<Long> activeChats = new HashSet<>();
    private final Map<Long, SendMessageData> latestMenuMessage = new ConcurrentHashMap<>();

    private final Map<String, Runnable> noArgsNoMessage = new ConcurrentHashMap<>();
    private final Map<String, Producer<Message>> noArgs = new ConcurrentHashMap<>();
    private final Map<String, Function<WebsiteId, Message>> websiteIdArg = new ConcurrentHashMap<>();
    private final Map<String, Function<TopicId, Message>> topicIdArg = new ConcurrentHashMap<>();
    private final Map<String, ArticleCommand> articleArg = new ConcurrentHashMap<>();

    private final Map<Long, Function<String, Message>> onNextMessage = new ConcurrentHashMap<>();

    public record Message(String text, InlineKeyboardMarkup keyboard,
                          Function<String, Message> onNextMessage) {
        Message(final String text, final InlineKeyboardMarkup keyboard) {
            this(text, keyboard, null);
        }
    }

    private record SendMessageData(int id, String text, InlineKeyboardMarkup keyboard) {}

    @FunctionalInterface
    public interface ArticleCommand {
        Message apply(ArticleId id, int messageId);
    }

    @Autowired
    public TelegramBot(final String token) {
        super(token);
    }

    @Override
    public String getBotUsername() {
        return "HsseNewsTeam1Bot";
    }

    public void command(final String text, final Runnable command) {
        noArgsNoMessage.put(text, command);
    }

    public void command(final String text, final Producer<Message> command) {
        noArgs.put(text, command);
    }

    public void commandWebsite(final String prefix, final Function<WebsiteId, Message> command) {
        websiteIdArg.put(prefix, command);
    }

    public void commandTopic(final String prefix, final Function<TopicId, Message> command) {
        topicIdArg.put(prefix, command);
    }

    public void commandArticle(final String prefix, final ArticleCommand command) {
        articleArg.put(prefix, command);
    }

    private void sendMenuMessage(final long chatId, final Message message)
            throws TelegramApiException {
        if (!latestMenuMessage.containsKey(chatId)) {
            final SendMessage send = new SendMessage();
            send.setChatId(chatId);

            send.setText(message.text());
            send.setReplyMarkup(message.keyboard());

            final int id = execute(send).getMessageId();
            latestMenuMessage.put(chatId, new SendMessageData(id, message.text(), message.keyboard()));
        } else {
            editMessage(chatId, message, latestMenuMessage.get(chatId).id());
        }

        if (message.onNextMessage() != null) {
            onNextMessage.put(chatId, message.onNextMessage());
        } else {
            onNextMessage.remove(chatId);
        }
    }

    private void editMessage(long chatId, final Message message, int replacedId)
            throws TelegramApiException {
        final EditMessageText edit = new EditMessageText();
        edit.setChatId(chatId);
        edit.setMessageId(replacedId);
        edit.setText(message.text());
        edit.setReplyMarkup(message.keyboard());

        execute(edit);
    }

    @SneakyThrows
    public void sendMessage(final long chatId, final Message message) {
        if (latestMenuMessage.containsKey(chatId)) {
            final SendMessageData old = latestMenuMessage.get(chatId);

            editMessage(chatId, message, old.id());

            final SendMessage send = new SendMessage();
            send.setChatId(chatId);

            send.setText(old.text());
            send.setReplyMarkup(old.keyboard());

            final int id = execute(send).getMessageId();
            latestMenuMessage.put(chatId,
                    new SendMessageData(id, old.text(), old.keyboard()));
        } else {
            final SendMessage send = new SendMessage();
            send.setChatId(chatId);

            send.setText(message.text());
            send.setReplyMarkup(message.keyboard());

            execute(send).getMessageId();
        }
    }

    @SneakyThrows
    public void sendArticle(final Function<Integer, Message> messageIdToMessage) {
        final SendMessage send = new SendMessage();
        final EditMessageText edit = new EditMessageText();

        for (final long chatId : activeChats) {
            if (latestMenuMessage.containsKey(chatId)) {
                deleteMessage(chatId, latestMenuMessage.get(chatId).id());
            }

            send.setChatId(chatId);
            send.setText("...fetching article...");

            final int messageId = execute(send).getMessageId();
            final Message message = messageIdToMessage.apply(messageId);

            edit.setChatId(chatId);
            edit.setMessageId(messageId);
            edit.setText(message.text());
            edit.setReplyMarkup(message.keyboard());
            execute(edit);
        }
    }

    private void deleteMessage(final long chatId, final int messageId)
            throws TelegramApiException {
        final DeleteMessage request = new DeleteMessage();
        request.setChatId(chatId);
        request.setMessageId(messageId);
        execute(request);
    }

    private WebsiteId parseWebsiteId(final String text) {
        return new WebsiteId(Long.parseLong(text.strip()));
    }

    private TopicId parseTopicId(final String text) {
        return new TopicId(Long.parseLong(text.strip()));
    }

    private ArticleId parseArticleId(final String text) {
        return new ArticleId(UUID.fromString(text.strip()));
    }

    private int parseMessageId(final String text) {
        return Integer.parseInt(text.strip());
    }

    private void handleCommand(final long chatId, final String text)
            throws TelegramApiException {
        onNextMessage.remove(chatId);

        final Runnable noArgNoMessageCommand = noArgsNoMessage.get(text.toLowerCase(Locale.US));
        if (noArgNoMessageCommand != null) {
            noArgNoMessageCommand.run();
            return;
        }

        final Producer<Message> noArgCommand = noArgs.get(text.toLowerCase(Locale.US));
        if (noArgCommand != null) {
            sendMenuMessage(chatId, noArgCommand.call());
            return;
        }

        final Optional<String> websiteArgCommand = websiteIdArg.keySet().stream()
                .filter(prefix -> text.toLowerCase(Locale.US).startsWith(prefix)).findFirst();
        if (websiteArgCommand.isPresent()) {
            sendMenuMessage(chatId, websiteIdArg.get(websiteArgCommand.get()).apply(
                    parseWebsiteId(text.substring(websiteArgCommand.get().length()))));
            return;
        }

        final Optional<String> topicArgCommand = topicIdArg.keySet().stream()
                .filter(prefix -> text.toLowerCase(Locale.US).startsWith(prefix)).findFirst();
        if (topicArgCommand.isPresent()) {
            sendMenuMessage(chatId, topicIdArg.get(topicArgCommand.get()).apply(
                    parseTopicId(text.substring(topicArgCommand.get().length()))));
            return;
        }

        final EditMessageText edit = new EditMessageText();
        final Optional<String> articleArgCommand = articleArg.keySet().stream()
                .filter(prefix -> text.toLowerCase(Locale.US).startsWith(prefix)).findFirst();
        if (articleArgCommand.isPresent()) {
            final List<String> params = Arrays.stream(
                            text.substring(articleArgCommand.get().length()).split(" "))
                    .filter((string) -> !string.isBlank()).toList();
            final int requiredParamCount = 2;
            if (params.size() != requiredParamCount) {
                return;
            }

            final Message newMessage = articleArg.get(articleArgCommand.get()).apply(
                    parseArticleId(params.get(0)), parseMessageId(params.get(1)));

            edit.setChatId(chatId);
            edit.setMessageId(parseMessageId(params.get(1)));
            edit.setText(newMessage.text());
            edit.setReplyMarkup(newMessage.keyboard());
            execute(edit);
        }
    }

    private void handleInput(final long chatId, final String text, final int messageId)
            throws TelegramApiException {
        final Function<String, Message> callback = onNextMessage.get(chatId);
        if (callback != null) {
            final Message message = callback.apply(text);
            sendMenuMessage(chatId, message);
            deleteMessage(chatId, messageId);
        } else {
            handleCommand(chatId, text);
        }
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(final Update update) {
        if (update.hasMessage()) {
            activeChats.add(update.getMessage().getChatId());
            handleInput(update.getMessage().getChatId(), update.getMessage().getText(),
                    update.getMessage().getMessageId());
            deleteMessage(update.getMessage().getChatId(), update.getMessage().getMessageId());
        } else if (update.hasCallbackQuery()) {
            activeChats.add(update.getCallbackQuery().getMessage().getChatId());
            handleCommand(update.getCallbackQuery().getMessage().getChatId(),
                    update.getCallbackQuery().getData());
        }
    }
}
