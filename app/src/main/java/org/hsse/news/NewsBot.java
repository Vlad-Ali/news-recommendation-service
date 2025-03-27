package org.hsse.news;

import jakarta.annotation.PostConstruct;
import org.hsse.news.telegram.WebsitesProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class NewsBot extends TelegramLongPollingBot {
    private final static String START_COMMAND = "/start";
    private final static String STOP_COMMAND = "/stop";
    private final static String NEXT_POST_COMMAND = "Следующий пост";
    private final static String ADD_SOURCE_COMMAND = "Добавить источник";
    private final static String LIKE_COMMAND = "Лайк";
    private final static String UNLIKE_COMMAND = "Убрать лайк";
    private final static String DISLIKE_COMMAND = "Дизлайк";
    private final static String UNDISLIKE_COMMAND = "Убрать дизлайк";
    private final static String WEBSITES_MENU_COMMAND = "Меню с сайтами";

    private final Map<Long, ChatState> chatStates = new ConcurrentHashMap<>();

    @Autowired
    private WebsitesProcessor websitesProcessor;

    @PostConstruct
    public void init(){
        websitesProcessor.registerBot(this);
    }

    private sealed interface ChatState {
    }

    public record NormalChatState(Optional<Long> post, Optional<Boolean> liked) implements ChatState {
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

    public record WebsitesMenuChatState(Optional<Boolean> getWebsite, Optional<Boolean> updateSub, Optional<Boolean> createCustom, Optional<Boolean> deleteCustom) implements ChatState{
        public WebsitesMenuChatState(){
            this(Optional.empty(),Optional.empty(), Optional.empty(), Optional.empty());
        }

        public static WebsitesMenuChatState withGetWebsite(){
            return new WebsitesMenuChatState(Optional.of(true),Optional.empty(), Optional.empty(), Optional.empty());
        }

        public static WebsitesMenuChatState withUpdateSub(){
            return new WebsitesMenuChatState(Optional.empty(),Optional.of(true), Optional.empty(), Optional.empty());
        }

        public static WebsitesMenuChatState withCreateCustom(){
            return new WebsitesMenuChatState(Optional.empty(),Optional.empty(), Optional.of(true), Optional.empty());
        }

        public static WebsitesMenuChatState withDeleteCustom(){
            return new WebsitesMenuChatState(Optional.empty(),Optional.empty(), Optional.empty(), Optional.of(true));
        }
    }

    @Autowired
    public NewsBot(final @Value("${tg-bot.token}") String token) {
        super(token);
    }

    @Override
    public String getBotUsername() {
        return "HsseNewsTeam2Bot";
    }

    private static KeyboardRow getNoPostCommandsRow() {
        final KeyboardRow commandsRow = new KeyboardRow();
        commandsRow.add(NEXT_POST_COMMAND);
        commandsRow.add(WEBSITES_MENU_COMMAND);
        return commandsRow;
    }

    public ReplyKeyboard getKeyboard(final ChatState state) {
        if (!(state instanceof final NormalChatState normalChatState)) {
            return null;
        }

        if (normalChatState.post().isEmpty()) {
            return new ReplyKeyboardMarkup(List.of(getNoPostCommandsRow()));
        } else if (normalChatState.isLiked()) {
            final KeyboardRow likeRow = new KeyboardRow();
            likeRow.add(UNLIKE_COMMAND);
            likeRow.add(DISLIKE_COMMAND);
            return new ReplyKeyboardMarkup(List.of(likeRow, getNoPostCommandsRow()));
        } else if (normalChatState.isDisliked()) {
            final KeyboardRow likeRow = new KeyboardRow();
            likeRow.add(LIKE_COMMAND);
            likeRow.add(UNDISLIKE_COMMAND);
            return new ReplyKeyboardMarkup(List.of(likeRow, getNoPostCommandsRow()));
        } else {
            final KeyboardRow likeRow = new KeyboardRow();
            likeRow.add(LIKE_COMMAND);
            likeRow.add(DISLIKE_COMMAND);
            return new ReplyKeyboardMarkup(List.of(likeRow, getNoPostCommandsRow()));
        }
    }

    public void sendMessage(final long chatId, final String text) throws TelegramApiException {
        final SendMessage message = new SendMessage();
        message.setChatId(chatId);

        message.setText(text);
        message.setReplyMarkup(getKeyboard(chatStates.get(chatId)));

        execute(message);
    }

    public void sendMessage(final long chatId, final String text, final ReplyKeyboard keyboard) throws TelegramApiException {
        final SendMessage message = new SendMessage();
        message.setChatId(chatId);

        message.setText(text);
        message.setReplyMarkup(keyboard);

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
        } else if (WEBSITES_MENU_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, new WebsitesMenuChatState());
            sendMessage(chatId, "Вы попали в меню сайтов", websitesProcessor.getKeyboard());
            return true;
        }
        return false;
    }

    public void setChatState(final long chatId, final ChatState state){
        chatStates.put(chatId, state);
    }

    @Override
    public void onUpdateReceived(final Update update){
        final long chatId = update.getMessage().getChatId();
        final String text = update.getMessage().getText();
        final ChatState state = chatStates.get(chatId);

        if (STOP_COMMAND.equalsIgnoreCase(text)) {
            chatStates.remove(chatId);
        } else if (state == null || START_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, new NormalChatState());
            try {
                sendMessage(chatId, "Привет! " +
                        "Добавить источники RSS и ты сможешь смотреть ленту новостей в этом боте! " +
                        "Если ты в веб-версии и не видишь меню, наведи на символ рядом с прикреплением файла");
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } else if (state instanceof WebsitesMenuChatState) {
            try {
                if(!websitesProcessor.handleWebsitesMenuState(chatId, text)){
                    sendMessage(chatId, "Операция " + text + " не поддерживается");
                }
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                if (!handleNormalState(chatId, state, text)) {
                    try {
                        sendMessage(chatId, "Операция " + text + " не поддерживается");
                    } catch (TelegramApiException e) {
                        throw new RuntimeException(e);
                    }
                }
            } catch (TelegramApiException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
