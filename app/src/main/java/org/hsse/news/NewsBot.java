package org.hsse.news;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.List;
import java.util.Map;

import static org.telegram.abilitybots.api.objects.Locality.USER;
import static org.telegram.abilitybots.api.objects.Privacy.PUBLIC;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

@Component
public class NewsBot extends AbilityBot {
    enum ChatState {
        NO_POST,
        POST,
        AWAITING_URI
    }

    private final Map<Long, ChatState> chatStates;

    @Autowired
    public NewsBot(Environment environment) {
        super(environment.getProperty("bot-token"), "HsseNewsTeam1Bot");
        chatStates = db.getMap("chatStates");
    }

    @Override
    public long creatorId() {
        return 1;
    }

    public Ability startBot() {
        return Ability
                .builder()
                .name("start")
                .info("Starts working with a bot")
                .locality(USER)
                .privacy(PUBLIC)
                .action(context -> {
                    SendMessage message = new SendMessage();
                    message.setChatId(context.chatId());

                    message.setText("Welcome!");
                    message.setReplyMarkup(getNoPostKeyboard());

                    silent.execute(message);

                    chatStates.put(context.chatId(), ChatState.NO_POST);
                })
                .build();
    }

    private static KeyboardRow getCommandsRow() {
        KeyboardRow commandsRow = new KeyboardRow();
        commandsRow.add("next");
        commandsRow.add("add source");
        return commandsRow;
    }

    private static ReplyKeyboard getPostKeyboard() {
        KeyboardRow likeRow = new KeyboardRow();
        likeRow.add("like");
        likeRow.add("dislike");
        return new ReplyKeyboardMarkup(List.of(likeRow, getCommandsRow()));
    }

    private static ReplyKeyboard getNoPostKeyboard() {
        return new ReplyKeyboardMarkup(List.of(getCommandsRow()));
    }

    private void sendPost(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        sendMessage.setText("Предстааавьте, что это пост с новостями");
        sendMessage.setReplyMarkup(getPostKeyboard());

        silent.execute(sendMessage);

        chatStates.put(chatId, ChatState.POST);
    }

    private void sendLikeRecorded(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        sendMessage.setText("Предстааавьте, что мы записал лайк");
        sendMessage.setReplyMarkup(getNoPostKeyboard());

        silent.execute(sendMessage);

        chatStates.put(chatId, ChatState.NO_POST);
    }

    private void sendDislikeRecorded(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        sendMessage.setText("Предстааавьте, что мы записал дизлайк");
        sendMessage.setReplyMarkup(getNoPostKeyboard());

        silent.execute(sendMessage);

        chatStates.put(chatId, ChatState.NO_POST);
    }

    private void sendAddSource(long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        sendMessage.setText("Введите URI нового источника: ");

        silent.execute(sendMessage);

        chatStates.put(chatId, ChatState.AWAITING_URI);
    }

    private void sendUnsupported(long chatId, Message operation) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        sendMessage.setText("Операция " + operation.getText() + " не поддерживается");
        if (chatStates.get(chatId).equals(ChatState.POST)) {
            sendMessage.setReplyMarkup(getPostKeyboard());
        } else {
            sendMessage.setReplyMarkup(getNoPostKeyboard());
        }

        silent.execute(sendMessage);
    }

    private void replyToStarted(long chatId, Message message) {
        if ("next".equalsIgnoreCase(message.getText())) {
            sendPost(chatId);
        } else if ("add source".equalsIgnoreCase(message.getText())) {
            sendAddSource(chatId);
        } else {
            sendUnsupported(chatId, message);
        }
    }

    private void replyToScrolling(long chatId, Message message) {
        if ("next".equalsIgnoreCase(message.getText())) {
            sendPost(chatId);
        } else if ("like".equalsIgnoreCase(message.getText())) {
            sendLikeRecorded(chatId);
        } else if ("dislike".equalsIgnoreCase(message.getText())) {
            sendDislikeRecorded(chatId);
        } else if ("add source".equalsIgnoreCase(message.getText())) {
            sendAddSource(chatId);
        } else {
            sendUnsupported(chatId, message);
        }
    }

    private void replyToAwaitingUri(long chatId, Message message) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        sendMessage.setText("Источник " + message.getText() + " добавлен");
        sendMessage.setReplyMarkup(getNoPostKeyboard());

        silent.execute(sendMessage);

        chatStates.put(chatId, ChatState.NO_POST);
    }

    private void replyToButtons(long chatId, Message message) {
        if ("/stop".equalsIgnoreCase(message.getText())) {
            chatStates.remove(chatId);
            return;
        }

        switch (chatStates.get(chatId)) {
            case NO_POST -> replyToStarted(chatId, message);
            case POST -> replyToScrolling(chatId, message);
            case AWAITING_URI -> replyToAwaitingUri(chatId, message);
            default -> throw new IllegalStateException("Unexpected chat state");
        }
    }

    public Reply replyToButtons() {
        return Reply.of((bot, update) -> replyToButtons(getChatId(update), update.getMessage()),
                Flag.TEXT, update -> chatStates.containsKey(getChatId(update)));
    }
}
