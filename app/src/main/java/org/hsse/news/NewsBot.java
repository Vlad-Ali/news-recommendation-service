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

@Component
public class NewsBot extends TelegramLongPollingBot {
    enum ChatState {
        NO_POST,
        POST,
        AWAITING_URI
    }

    private final Map<Long, ChatState> chatStates = new HashMap<>();

    @Autowired
    public NewsBot(Environment environment) {
        super(environment.getProperty("bot-token"));
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
                replyToStarted(chatId);
            } else if (state.equals(ChatState.AWAITING_URI)) {
                replyToAwaitingUri(chatId, text);
            } else if (state.equals(ChatState.POST) && "like".equalsIgnoreCase(text)) {
                sendLikeRecorded(chatId);
            } else if (state.equals(ChatState.POST) && "dislike".equalsIgnoreCase(text)) {
                sendDislikeRecorded(chatId);
            } else if ("next".equalsIgnoreCase(text)) {
                sendPost(chatId);
            } else if ("add source".equalsIgnoreCase(text)) {
                sendAddSource(chatId);
            } else {
                sendUnsupported(chatId, text);
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return "HsseNewsTeam1Bot";
    }

    private void replyToStarted(long chatId) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        message.setText("Welcome!");
        message.setReplyMarkup(getNoPostKeyboard());

        execute(message);

        chatStates.put(chatId, ChatState.NO_POST);
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

    private void sendPost(long chatId) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        sendMessage.setText("Предстааавьте, что это пост с новостями");
        sendMessage.setReplyMarkup(getPostKeyboard());

        execute(sendMessage);

        chatStates.put(chatId, ChatState.POST);
    }

    private void sendLikeRecorded(long chatId) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        sendMessage.setText("Предстааавьте, что мы записал лайк");
        sendMessage.setReplyMarkup(getNoPostKeyboard());

        execute(sendMessage);

        chatStates.put(chatId, ChatState.NO_POST);
    }

    private void sendDislikeRecorded(long chatId) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        sendMessage.setText("Предстааавьте, что мы записал дизлайк");
        sendMessage.setReplyMarkup(getNoPostKeyboard());

        execute(sendMessage);

        chatStates.put(chatId, ChatState.NO_POST);
    }

    private void sendAddSource(long chatId) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        sendMessage.setText("Введите URI нового источника: ");

        execute(sendMessage);

        chatStates.put(chatId, ChatState.AWAITING_URI);
    }

    private void sendUnsupported(long chatId, String operation) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        sendMessage.setText("Операция " + operation + " не поддерживается");
        if (chatStates.get(chatId).equals(ChatState.POST)) {
            sendMessage.setReplyMarkup(getPostKeyboard());
        } else {
            sendMessage.setReplyMarkup(getNoPostKeyboard());
        }

        execute(sendMessage);
    }

    private void replyToAwaitingUri(long chatId, String message) throws TelegramApiException {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);

        sendMessage.setText("Источник " + message + " добавлен");
        sendMessage.setReplyMarkup(getNoPostKeyboard());

        execute(sendMessage);

        chatStates.put(chatId, ChatState.NO_POST);
    }
}
