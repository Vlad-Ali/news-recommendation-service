package org.hsse.news;

import lombok.SneakyThrows;
import org.glassfish.jersey.internal.util.Producer;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.website.models.WebsiteId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
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
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
public class TelegramBot extends TelegramLongPollingBot {
    private final Set<Long> activeChats = new HashSet<>();
    private final Map<Long, Integer> latestMenuMessageId = new ConcurrentHashMap<>();

    public record Message(String text, InlineKeyboardMarkup keyboard,
                          Function<String, Message> onNextMessage) {
        Message(String text, InlineKeyboardMarkup keyboard) {
            this(text, keyboard, null);
        }

        Message(String text, Function<String, Message> onNextMessage) {
            this(text, null, onNextMessage);
        }
    }

    private final Map<String, Runnable> noArgsNoMessage = new ConcurrentHashMap<>();
    private final Map<String, Producer<Message>> noArgs = new ConcurrentHashMap<>();
    private final Map<String, Function<WebsiteId, Message>> websiteIdArg = new ConcurrentHashMap<>();
    private final Map<String, Function<TopicId, Message>> topicIdArg = new ConcurrentHashMap<>();

    @FunctionalInterface
    public interface ArticleCommand {
        Message apply(ArticleId id, int messageId);
    }

    private final Map<String, ArticleCommand> articleArg = new ConcurrentHashMap<>();

    private final Map<Long, Function<String, Message>> onNextMessage = new ConcurrentHashMap<>();

    @Autowired
    public TelegramBot(final Environment environment) {
        super(environment.getProperty("bot-token"));
    }

    @Override
    public String getBotUsername() {
        return "HsseNewsTeam1Bot";
    }

    public void command(String text, Runnable command) {
        noArgsNoMessage.put(text, command);
    }

    public void command(String text, Producer<Message> command) {
        noArgs.put(text, command);
    }

    public void commandWebsite(String prefix, Function<WebsiteId, Message> command) {
        websiteIdArg.put(prefix, command);
    }

    public void commandTopic(String prefix, Function<TopicId, Message> command) {
        topicIdArg.put(prefix, command);
    }

    public void commandArticle(String prefix, ArticleCommand command) {
        articleArg.put(prefix, command);
    }

    private void sendMenuMessage(final long chatId, final Message message)
            throws TelegramApiException {
        final Integer messageId = latestMenuMessageId.get(chatId);
        if (messageId == null) {
            final SendMessage send = new SendMessage();
            send.setChatId(chatId);

            send.setText(message.text());
            send.setReplyMarkup(message.keyboard());

            final int id = execute(send).getMessageId();
            latestMenuMessageId.put(chatId, id);
        } else {
            final EditMessageText edit = new EditMessageText();
            edit.setChatId(chatId);
            edit.setMessageId(messageId);
            edit.setText(message.text());
            edit.setReplyMarkup(message.keyboard);

            execute(edit);
        }

        if (message.onNextMessage() != null) {
            onNextMessage.put(chatId, message.onNextMessage());
        } else {
            onNextMessage.remove(chatId);
        }
    }

    @SneakyThrows
    public void sendArticle(final Function<Integer, Message> messageIdToMessage) {
        for (long chatId : activeChats) {
            if (latestMenuMessageId.containsKey(chatId)) {
                deleteMessage(chatId, latestMenuMessageId.get(chatId));
            }

            final SendMessage send = new SendMessage();
            send.setChatId(chatId);
            send.setText("...fetching article...");

            final int messageId = execute(send).getMessageId();
            final Message message = messageIdToMessage.apply(messageId);

            EditMessageText edit = new EditMessageText();
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

    private void handleCommand(long chatId, String text)
            throws TelegramApiException {
        onNextMessage.remove(chatId);

        Runnable noArgNoMessageCommand = noArgsNoMessage.get(text.toLowerCase(Locale.US));
        if (noArgNoMessageCommand != null) {
            noArgNoMessageCommand.run();
            return;
        }

        Producer<Message> noArgCommand = noArgs.get(text.toLowerCase(Locale.US));
        if (noArgCommand != null) {
            sendMenuMessage(chatId, noArgCommand.call());
            return;
        }

        for (String command : websiteIdArg.keySet()) {
            if (text.toLowerCase(Locale.US).startsWith(command)) {
                WebsiteId id = new WebsiteId(Long.parseLong(text.substring(command.length()).strip()));
                sendMenuMessage(chatId, websiteIdArg.get(command).apply(id));
                return;
            }
        }

        for (String command : topicIdArg.keySet()) {
            if (text.toLowerCase(Locale.US).startsWith(command)) {
                TopicId id = new TopicId(Long.parseLong(text.substring(command.length()).strip()));
                sendMenuMessage(chatId, topicIdArg.get(command).apply(id));
                return;
            }
        }

        for (String command : articleArg.keySet()) {
            if (text.toLowerCase(Locale.US).startsWith(command)) {
                final List<String> params = Arrays.stream(
                                text.substring(command.length()).split(" "))
                        .filter((string) -> !string.isBlank()).toList();
                final int requiredParamCount = 2;
                if (params.size() != requiredParamCount) {
                    return;
                }

                ArticleId id = new ArticleId(UUID.fromString(params.get(0).strip()));
                int messageId = Integer.parseInt(params.get(1).strip());

                Message newMessage = articleArg.get(command).apply(id, messageId);

                EditMessageText edit = new EditMessageText();
                edit.setChatId(chatId);
                edit.setMessageId(messageId);
                edit.setText(newMessage.text());
                edit.setReplyMarkup(newMessage.keyboard());
                execute(edit);

                return;
            }
        }
    }

    private void handleInput(long chatId, String text, int messageId)
            throws TelegramApiException {
        final Function<String, Message> callback = onNextMessage.get(chatId);
        if (callback != null) {
            Message message = callback.apply(text);
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
