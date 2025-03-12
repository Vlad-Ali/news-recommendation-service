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
                chatStates.put(chatId, ChatState.NO_POST);
                sendMessage(chatId, "Welcome!");
            } else if (state.equals(ChatState.AWAITING_URI)) {
                chatStates.put(chatId, ChatState.NO_POST);
                sendMessage(chatId, "Источник " + text + " добавлен");
            } else if (state.equals(ChatState.POST) && "like".equalsIgnoreCase(text)) {
                chatStates.put(chatId, ChatState.NO_POST);
                sendMessage(chatId, "Предстааавьте, что мы записал лайк");
            } else if (state.equals(ChatState.POST) && "dislike".equalsIgnoreCase(text)) {
                chatStates.put(chatId, ChatState.NO_POST);
                sendMessage(chatId, "Предстааавьте, что мы записал дизлайк");
            } else if ("next".equalsIgnoreCase(text)) {
                sendMessage(chatId, "Предстааавьте, что это пост с новостями");
            } else if ("add source".equalsIgnoreCase(text)) {
                chatStates.put(chatId, ChatState.AWAITING_URI);
                sendMessage(chatId, "Введите URI нового источника: ");
            } else {
                sendMessage(chatId, "Операция " + text + " не поддерживается");
            }
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getBotUsername() {
        return "HsseNewsTeam1Bot";
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

    private void sendMessage(long chatId, String text) throws TelegramApiException {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);

        message.setText(text);

        if (chatStates.get(chatId).equals(ChatState.NO_POST)) {
            message.setReplyMarkup(getNoPostKeyboard());
        } else if (chatStates.get(chatId).equals(ChatState.POST)) {
            message.setReplyMarkup(getPostKeyboard());
        }

        execute(message);
    }

}
