package org.hsse.news.bot;

import jakarta.annotation.PostConstruct;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.website.models.WebsiteId;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.util.ReflectionUtils;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot implements ApplicationContextAware {
    @Setter
    private ApplicationContext applicationContext;

    private final Set<Long> activeChats = new HashSet<>();
    private final Map<Long, Integer> latestMenuMessageId = new ConcurrentHashMap<>();

    private final Map<String, Function<List<String>, Optional<Message>>> commands = new ConcurrentHashMap<>();

    private final Map<Long, Function<String, Message>> onNextMessage = new ConcurrentHashMap<>();

    public record Message(String text, InlineKeyboardMarkup keyboard,
                          Function<String, Message> onNextMessage) {
        Message(final String text, final InlineKeyboardMarkup keyboard) {
            this(text, keyboard, null);
        }
    }

    @FunctionalInterface
    public interface ArticleCommand {
        Message apply(ArticleId id, int messageId);
    }

    @Autowired
    public TelegramBot(final Environment environment) {
        super(environment.getProperty("bot-token"));
    }

    @PostConstruct
    public void findBotMappings() {
        for (final String beanName : applicationContext.getBeanDefinitionNames()) {
            if (beanName.equals("telegramBot")) continue;

            final Object bean = applicationContext.getBean(beanName);
            final Class<?> beanClass = AopUtils.getTargetClass(bean);
            ReflectionUtils.doWithMethods(beanClass, (method) -> {
                BotMapping annotation = AnnotationUtils.findAnnotation(method, BotMapping.class);
                if (annotation == null) {
                    return;
                }

                String mapping = annotation.value();
                commands.put(mapping, (args) -> {
                    int currentArg = 0;
                    final List<Object> methodArgs = new ArrayList<>();
                    for (int i = 0; i < method.getParameterCount(); i++) {
                        MethodParameter param = new MethodParameter(method, i);
                        if (param.getParameterType() == WebsiteId.class) {
                            methodArgs.add(new WebsiteId(Long.parseLong(args.get(currentArg))));
                            ++currentArg;
                        } else if (param.getParameterType() == ArticleId.class) {
                            methodArgs.add(new ArticleId(UUID.fromString(args.get(currentArg))));
                            ++currentArg;
                        } else if (param.getParameterType() == TopicId.class) {
                            methodArgs.add(new TopicId(Long.parseLong(args.get(currentArg))));
                            ++currentArg;
                        }
                    }

                    Object result;
                    try {
                        result = method.invoke(bean, methodArgs.toArray());
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        throw new RuntimeException(e);
                    }

                    if (result instanceof Message) {
                        return Optional.of((Message) result);
                    } else {
                        return Optional.empty();
                    }
                });
            });
        }
    }

    @Override
    public String getBotUsername() {
        return "HsseNewsTeam1Bot";
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
        final SendMessage send = new SendMessage();
        final EditMessageText edit = new EditMessageText();

        for (final long chatId : activeChats) {
            if (latestMenuMessageId.containsKey(chatId)) {
                deleteMessage(chatId, latestMenuMessageId.get(chatId));
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

    private void handleCommand(final long chatId, final String text) {
        onNextMessage.remove(chatId);

        String largestPrefix = commands.keySet().stream()
                .filter(prefix -> text.toLowerCase(Locale.US).startsWith(prefix.toLowerCase(Locale.US)))
                .max(Comparator.comparing(String::length)).orElseThrow();

        final List<String> args = Arrays.stream(text.substring(largestPrefix.length()).split(" "))
                .filter(string -> !string.isBlank()).toList();

        commands.get(largestPrefix).apply(args)
                .ifPresent((message) -> {
                    try {
                        sendMenuMessage(chatId, message);
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                });
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
