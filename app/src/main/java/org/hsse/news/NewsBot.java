package org.hsse.news;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class NewsBot extends TelegramLongPollingBot {
    private sealed interface ChatState {
    }

    private record NormalChatState(Optional<Long> post, Optional<Boolean> liked) implements ChatState {
        public NormalChatState() {
            this(Optional.empty(), Optional.empty());
        }

        public NormalChatState(long post) {
            this(Optional.of(post), Optional.empty());
        }

        public NormalChatState(long post, boolean liked) {
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

    private final Map<Long, ChatState> chatStates = new HashMap<>();

    @Autowired
    public NewsBot(Environment environment) {
        super(environment.getProperty("bot-token"));
    }

    @Override
    public String getBotUsername() {
        return "HsseNewsTeam1Bot";
    }

    private final static String NEXT_POST_COMMAND = "Следующий пост";
    private final static String ADD_SOURCE_COMMAND = "Добавить источник";
    private final static String LIKE_COMMAND = "Лайк";
    private final static String UNLIKE_COMMAND = "Убрать лайк";
    private final static String DISLIKE_COMMAND = "Дизлайк";
    private final static String UNDISLIKE_COMMAND = "Убрать дизлайк";

    private static KeyboardRow getNoPostCommandsRow() {
        KeyboardRow commandsRow = new KeyboardRow();
        commandsRow.add(NEXT_POST_COMMAND);
        commandsRow.add(ADD_SOURCE_COMMAND);
        return commandsRow;
    }

    private ReplyKeyboard getKeyboard(ChatState state) {
        if (!(state instanceof NormalChatState normalChatState)) {
            return null;
        }

        if (normalChatState.post().isEmpty()) {
            return new ReplyKeyboardMarkup(List.of(getNoPostCommandsRow()));
        } else if (normalChatState.isLiked()) {
            KeyboardRow likeRow = new KeyboardRow();
            likeRow.add(UNLIKE_COMMAND);
            likeRow.add(DISLIKE_COMMAND);
            return new ReplyKeyboardMarkup(List.of(likeRow, getNoPostCommandsRow()));
        } else if (normalChatState.isDisliked()) {
            KeyboardRow likeRow = new KeyboardRow();
            likeRow.add(LIKE_COMMAND);
            likeRow.add(UNDISLIKE_COMMAND);
            return new ReplyKeyboardMarkup(List.of(likeRow, getNoPostCommandsRow()));
        } else {
            KeyboardRow likeRow = new KeyboardRow();
            likeRow.add(LIKE_COMMAND);
            likeRow.add(DISLIKE_COMMAND);
            return new ReplyKeyboardMarkup(List.of(likeRow, getNoPostCommandsRow()));
        }
    }

    private void sendMessage(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        message.setText(text);
        message.setReplyMarkup(getKeyboard(chatStates.get(chatId)));

        execute(message);
    }

    private boolean handleLikes(long chatId, NormalChatState state, String text)
            throws TelegramApiException {
        if (state.post().isEmpty()) {
            return false;
        }
        long postId = state.post().get();

        if (!state.isLiked() && LIKE_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, new NormalChatState(postId, true));
            sendMessage(chatId, "Предстааавьте, что мы записали лайк для поста " + postId);
            return true;
        } else if (state.isLiked() && UNLIKE_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, new NormalChatState(postId));
            sendMessage(chatId, "Предстааавьте, что мы убрали лайк для поста " + postId);
            return true;
        } else if (!state.isDisliked() && DISLIKE_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, new NormalChatState(postId, false));
            sendMessage(chatId, "Предстааавьте, что мы записал дизлайк для поста " + postId);
            return true;
        } else if (state.isDisliked() && UNDISLIKE_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, new NormalChatState(postId));
            sendMessage(chatId, "Предстааавьте, что мы убрали дизлайк для поста " + postId);
            return true;
        } else {
            return false;
        }
    }

    private boolean handleNormalState(long chatId, ChatState state, String text)
            throws TelegramApiException {
        if (!(state instanceof NormalChatState)) {
            return false;
        }

        if (handleLikes(chatId, (NormalChatState) state, text)) {
            return true;
        }

        if (NEXT_POST_COMMAND.equalsIgnoreCase(text)) {
            long postId = (long) (Math.random() * 1000);
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

    @Override
    public void onUpdateReceived(Update update) {
        try {
            long chatId = update.getMessage().getChatId();
            String text = update.getMessage().getText();
            ChatState state = chatStates.get(chatId);

            if ("/stop".equalsIgnoreCase(text)) {
                chatStates.remove(chatId);
            } else if (state == null || "/start".equalsIgnoreCase(text)) {
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
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }
}
