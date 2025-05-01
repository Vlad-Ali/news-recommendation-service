package org.hsse.news.bot;

import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Getter
    private final Set<ChatId> activeChats = new HashSet<>();

    private final Map<ChatId, SendMessageData> latestMenuMessage = new ConcurrentHashMap<>();

    private final Map<String, BiFunction<List<String>, ChatId, Optional<Message>>> commands
            = new ConcurrentHashMap<>();

    private final Map<ChatId, Function<String, Message>> onNextMessage = new ConcurrentHashMap<>();

    private record SendMessageData(MessageId id, String text, InlineKeyboardMarkup keyboard) {
    }

    public TelegramBot(final @Value("${tg-bot.token}") String token) {
        super(token);
    }

    public void addCommand(
            final String command,
            final BiFunction<List<String>, ChatId, Optional<Message>> toRun) {
        commands.put(command, toRun);
    }

    @Override
    public String getBotUsername() {
        return "HsseNewsTeam2Bot";
    }

    private void editMessage(final ChatId chatId, final Message message, final MessageId replaced)
            throws TelegramApiException {
        final EditMessageText edit = new EditMessageText();
        edit.setChatId(chatId.value());
        edit.setMessageId(replaced.value());
        edit.setText(message.text());
        edit.setReplyMarkup(message.keyboard());

        execute(edit);
    }

    private void sendMenuMessage(final ChatId chatId, final Message message) throws TelegramApiException {
        if (message.replace().isPresent()) {
            editMessage(chatId, message, message.replace().get());
        } else if (latestMenuMessage.containsKey(chatId)) {
            editMessage(chatId, message, latestMenuMessage.get(chatId).id());
        } else {
            final SendMessage send = new SendMessage();
            send.setChatId(chatId.value());

            send.setText(message.text());
            send.setReplyMarkup(message.keyboard());

            final MessageId id = new MessageId(execute(send).getMessageId());
            latestMenuMessage.put(chatId,
                    new SendMessageData(id, message.text(), message.keyboard()));
        }

        if (message.onNextMessage() != null) {
            onNextMessage.put(chatId, message.onNextMessage());
        } else {
            onNextMessage.remove(chatId);
        }
    }

    @SneakyThrows
    public void sendMessage(final ChatId chatId, final Message message) {
        if (message.replace().isPresent()) {
            editMessage(chatId, message, message.replace().get());
        }

        if (latestMenuMessage.containsKey(chatId)) {
            final SendMessageData old = latestMenuMessage.get(chatId);

            editMessage(chatId, message, old.id());

            final SendMessage send = new SendMessage();
            send.setChatId(chatId.value());

            send.setText(old.text());
            send.setReplyMarkup(old.keyboard());
            send.enableHtml(true);

            final MessageId id = new MessageId(execute(send).getMessageId());
            latestMenuMessage.put(chatId,
                    new SendMessageData(id, old.text(), old.keyboard()));
        } else {
            final SendMessage send = new SendMessage();
            send.setChatId(chatId.value());

            send.setText(message.text());
            send.setReplyMarkup(message.keyboard());

            execute(send).getMessageId();
        }
    }

    @SneakyThrows
    public void sendArticleTo(final ChatId chatId,
                               final Function<MessageId, Message> messageIdToMessage) {

        if (latestMenuMessage.containsKey(chatId)) {
            deleteMessage(chatId, latestMenuMessage.get(chatId).id());
        }

        final SendMessage send = new SendMessage();
        send.setChatId(chatId.value());
        send.setText("...fetching article...");

        final MessageId messageId = new MessageId(execute(send).getMessageId());
        final Message message = messageIdToMessage.apply(messageId);

        editMessage(chatId, message, messageId);
    }

    @SneakyThrows
    public void sendArticle(final Function<MessageId, Message> messageIdToMessage) {
        for (final ChatId chatId : activeChats) {
            sendArticleTo(chatId, messageIdToMessage);
        }
    }

    private void deleteMessage(final ChatId chatId, final MessageId messageId)
            throws TelegramApiException {
        final DeleteMessage request = new DeleteMessage();
        request.setChatId(chatId.value());
        request.setMessageId(messageId.value());
        execute(request);
    }

    private void handleCommand(final ChatId chatId, final String text)
            throws TelegramApiException {
        onNextMessage.remove(chatId);

        final String largestPrefix = commands.keySet().stream()
                .filter(prefix -> text.toLowerCase(Locale.US).startsWith(prefix.toLowerCase(Locale.US)))
                .max(Comparator.comparing(String::length)).orElseThrow();

        final List<String> args = Arrays.stream(text.substring(largestPrefix.length()).split(" "))
                .filter(string -> !string.isBlank()).toList();
        final Optional<Message> message = commands.get(largestPrefix).apply(args, chatId);
        if (message.isPresent()) {
            sendMenuMessage(chatId, message.get());
        }
    }

    private void handleInput(final ChatId chatId, final String text, final MessageId messageId)
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
    public void onUpdateReceived(final Update update) {
        try {
            if (update.hasMessage()) {
                final ChatId chatId = new ChatId(update.getMessage().getChatId());
                final MessageId messageId = new MessageId(update.getMessage().getMessageId());

            activeChats.add(chatId);
            handleInput(chatId, update.getMessage().getText(), messageId);
            deleteMessage(chatId, messageId);
        } else if (update.hasCallbackQuery()) {
            final ChatId chatId = new ChatId(update.getCallbackQuery().getMessage().getChatId());

                activeChats.add(chatId);
                handleCommand(chatId, update.getCallbackQuery().getData());
            }
        } catch (Exception e) {
            log.error("Exception while handling a command: ", e);
        }
    }
}
