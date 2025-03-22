package org.hsse.news;

import lombok.SneakyThrows;
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
import java.util.concurrent.ConcurrentHashMap;

@Component
public class NewsBot extends TelegramLongPollingBot {
    private final static String START_COMMAND = "/start";
    private final static String STOP_COMMAND = "/stop";
    private final static String NEXT_POST_COMMAND = "Следующий пост";
    private final static String ADD_SOURCE_COMMAND = "Добавить источник";
    private final static String LIKE_COMMAND = "Лайк";
    private final static String UNLIKE_COMMAND = "Убрать лайк";
    private final static String DISLIKE_COMMAND = "Дизлайк";
    private final static String UNDISLIKE_COMMAND = "Убрать дизлайк";

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

    private static final class AwaitingUriChatState implements ChatState {
    }

    @Autowired
    public NewsBot(final Environment environment) {
        super(environment.getProperty("bot-token"));
    }

    @Override
    public String getBotUsername() {
        return "HsseNewsTeam1Bot";
    }

    private static List<InlineKeyboardButton> getNoPostCommandsRow() {
        return List.of(
                InlineKeyboardButton.builder()
                        .text(NEXT_POST_COMMAND)
                        .callbackData(NEXT_POST_COMMAND).build(),
                InlineKeyboardButton.builder()
                        .text(ADD_SOURCE_COMMAND)
                        .callbackData(ADD_SOURCE_COMMAND).build());
    }

    private ReplyKeyboard getKeyboard(final ChatState state) {
        if (!(state instanceof final NormalChatState normalChatState)) {
            return null;
        }

        if (normalChatState.post().isEmpty()) {
            return new InlineKeyboardMarkup(List.of(getNoPostCommandsRow()));
        } else if (normalChatState.isLiked()) {
            return new InlineKeyboardMarkup(List.of(List.of(
                            InlineKeyboardButton.builder()
                                    .text(UNLIKE_COMMAND)
                                    .callbackData(UNLIKE_COMMAND).build(),
                            InlineKeyboardButton.builder()
                                    .text(DISLIKE_COMMAND)
                                    .callbackData(DISLIKE_COMMAND).build()),
                    getNoPostCommandsRow()));
        } else if (normalChatState.isDisliked()) {
            return new InlineKeyboardMarkup(List.of(List.of(
                            InlineKeyboardButton.builder()
                                    .text(LIKE_COMMAND)
                                    .callbackData(LIKE_COMMAND).build(),
                            InlineKeyboardButton.builder()
                                    .text(UNDISLIKE_COMMAND)
                                    .callbackData(UNDISLIKE_COMMAND).build()),
                    getNoPostCommandsRow()));
        } else {
            return new InlineKeyboardMarkup(List.of(List.of(
                            InlineKeyboardButton.builder()
                                    .text(LIKE_COMMAND)
                                    .callbackData(LIKE_COMMAND).build(),
                            InlineKeyboardButton.builder()
                                    .text(DISLIKE_COMMAND)
                                    .callbackData(DISLIKE_COMMAND).build()),
                    getNoPostCommandsRow()));
        }
    }

    private void sendMessage(final long chatId, final String text) throws TelegramApiException {
        final SendMessage message = new SendMessage();
        message.setChatId(chatId);

        message.setText(text);
        message.setReplyMarkup(getKeyboard(chatStates.get(chatId)));

        execute(message);
    }

    private boolean handleLikes(final long chatId, final NormalChatState state, final String text)
            throws TelegramApiException {
        if (state.post().isEmpty()) {
            return false;
        }
        final long postId = state.post().get();

        if (state.isLiked()) {
            if (UNLIKE_COMMAND.equalsIgnoreCase(text)) {
                chatStates.put(chatId, new NormalChatState(postId));
                sendMessage(chatId, "Предстааавьте, что мы убрали лайк для поста " + postId);
                return true;
            }
        } else {
            if (LIKE_COMMAND.equalsIgnoreCase(text)) {
                chatStates.put(chatId, new NormalChatState(postId, true));
                sendMessage(chatId, "Предстааавьте, что мы записали лайк для поста " + postId);
                return true;
            }
        }

        if (state.isDisliked()) {
            if (UNDISLIKE_COMMAND.equalsIgnoreCase(text)) {
                chatStates.put(chatId, new NormalChatState(postId, false));
                sendMessage(chatId, "Предстааавьте, что мы записал дизлайк для поста " + postId);
                return true;
            }
        } else {
            if (DISLIKE_COMMAND.equalsIgnoreCase(text)) {
                chatStates.put(chatId, new NormalChatState(postId));
                sendMessage(chatId, "Предстааавьте, что мы убрали дизлайк для поста " + postId);
                return true;
            }
        }
        return false;
    }

    private boolean handleNormalState(final long chatId, final ChatState state, final String text)
            throws TelegramApiException {
        if (!(state instanceof NormalChatState)) {
            return false;
        }

        if (handleLikes(chatId, (NormalChatState) state, text)) {
            return true;
        }

        if (NEXT_POST_COMMAND.equalsIgnoreCase(text)) {
            final long postId = (long) (Math.random() * 1000);
            chatStates.put(chatId, new NormalChatState(postId));
            sendMessage(chatId, "Предстааавьте, что это пост " + postId + " с новостями");
            return true;
        } else if (ADD_SOURCE_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, new AwaitingUriChatState());
            sendMessage(chatId, "Введите URI нового источника: ");
            return true;
        }
        return false;
    }

    private void handleCommand(long chatId, String text) throws TelegramApiException {
        final ChatState state = chatStates.get(chatId);

        if (STOP_COMMAND.equalsIgnoreCase(text)) {
            chatStates.remove(chatId);
        } else if (state == null || START_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, new NormalChatState());
            sendMessage(chatId, "Привет! " +
                    "Добавить источники RSS и ты сможешь смотреть ленту новостей в этом боте! " +
                    "Если ты в веб-версии и не видишь меню, наведи на символ рядом с прикреплением файла");
        } else if (state instanceof AwaitingUriChatState) {
            chatStates.put(chatId, new NormalChatState());
            sendMessage(chatId, "Источник " + text + " добавлен");
        } else if (!handleNormalState(chatId, state, text)) {
            sendMessage(chatId, "Операция " + text + " не поддерживается");
        }
    }

    @Override
    @SneakyThrows
    public void onUpdateReceived(final Update update) {
        if (update.hasMessage()) {
            handleCommand(update.getMessage().getChatId(), update.getMessage().getText());
        } else if (update.hasCallbackQuery()) {
            handleCommand(update.getCallbackQuery().getMessage().getChatId(),
                    update.getCallbackQuery().getData());
        }
    }
}
