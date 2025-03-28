package org.hsse.news.bot;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hsse.news.util.ReflectionUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.reflect.Method;
import java.util.ArrayList;
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
public class TelegramBot extends TelegramLongPollingBot implements ApplicationContextAware {
    @Setter
    private ApplicationContext applicationContext;

    private final Set<ChatId> activeChats = new HashSet<>();
    private final Map<ChatId, MessageId> latestMenuMessageId = new ConcurrentHashMap<>();

    private final Map<String, BiFunction<List<String>, ChatId, Optional<Message>>> commands
            = new ConcurrentHashMap<>();

    private final Map<ChatId, Function<String, Message>> onNextMessage = new ConcurrentHashMap<>();

    @Autowired
    public TelegramBot(final @Value("${bot-token}") String token) {
        super(token);
    }

    @SneakyThrows
    private Optional<Message> runMethod(
            final Object object, final Method method,
            final List<String> args, final ChatId chatId) {
        final List<String> mutableArgs = new ArrayList<>(args);
        final List<Object> methodArgs = new ArrayList<>();
        for (int i = 0; i < method.getParameterCount(); i++) {
            final Class<?> parameterType = ReflectionUtil.parameterType(method, i);
            if (parameterType == TelegramBot.class) {
                methodArgs.add(this);
            } else if (parameterType == ChatId.class) {
                methodArgs.add(chatId);
            } else {
                methodArgs.add(ReflectionUtil.parseArg(mutableArgs.remove(0),
                        parameterType));
            }
        }

        Object result;
        result = method.invoke(object, methodArgs.toArray());

        if (result instanceof Message) {
            return Optional.of((Message) result);
        } else {
            return Optional.empty();
        }
    }

    @PostConstruct
    public void findBotMappings() {
        ReflectionUtil.forEachAnnotatedMethod(applicationContext,
                List.of("telegramBot"), BotMapping.class, (annotation, object, method) -> {
                    commands.put(annotation.value(), (args, chatId) ->
                            runMethod(object, method, args, chatId));
                });
    }

    @Override
    public String getBotUsername() {
        return "HsseNewsTeam1Bot";
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

    private void sendMenuMessage(final ChatId chatId, final Message message)
            throws TelegramApiException {
        final MessageId messageId = latestMenuMessageId.get(chatId);
        if (messageId == null) {
            final SendMessage send = new SendMessage();
            send.setChatId(chatId.value());

            send.setText(message.text());
            send.setReplyMarkup(message.keyboard());

            final MessageId id = new MessageId(execute(send).getMessageId());
            latestMenuMessageId.put(chatId, id);
        } else {
            editMessage(chatId, message, messageId);
        }

        if (message.onNextMessage() != null) {
            onNextMessage.put(chatId, message.onNextMessage());
        } else {
            onNextMessage.remove(chatId);
        }
    }

    private void sendMessage(final ChatId chatId, final Message message) throws TelegramApiException {
        if (message.replace().isPresent()) {
            editMessage(chatId, message, message.replace().get());
        } else {
            sendMenuMessage(chatId, message);
        }
    }

    private void sendArticleTo(final ChatId chatId, final Function<MessageId, Message> messageIdToMessage)
            throws TelegramApiException {
        if (latestMenuMessageId.containsKey(chatId)) {
            deleteMessage(chatId, latestMenuMessageId.get(chatId));
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
            sendMessage(chatId, message.get());
        }
    }

    private void handleInput(final ChatId chatId, final String text, final MessageId messageId)
            throws TelegramApiException {
        final Function<String, Message> callback = onNextMessage.get(chatId);
        if (callback != null) {
            final Message message = callback.apply(text);
            sendMessage(chatId, message);
            deleteMessage(chatId, messageId);
        } else {
            handleCommand(chatId, text);
        }
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(final Update update) {
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
    }
}
