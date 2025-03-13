package org.hsse.news.telegram;

import org.hsse.news.NewsBot;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class WebsitesProcessor {
    private NewsBot telegramBot;

    private final static String CANCEL_COMMAND = "/Закрыть";
    private final static String GET_COMMAND = "/Получить сайт\nпо ID";
    private final static String SUB_WEBSITES_COMMAND = "/Получить\n выбранные сайты";
    private final static String UN_SUB_WEBSITES_COMMAND = "/Получить\n другие сайты";
    private final static String UPDATE_SUB_WEBSITES_COMMAND = "/Обновить\n выбранные сайты";
    private final static String CREATE_CUSTOM_COMMAND = "/Создать\n собственный сайт";
    private final static String DELETE_CUSTOM_COMMAND = "/Удалить\n собственный сайт";
    private final static String INFO_COMMAND = "/Получить информацию\n о меню сайтов";

    private final Map<Long, NewsBot.WebsitesMenuChatState> chatStates = new ConcurrentHashMap<>();

    public void registerBot(final NewsBot telegramBot){
        this.telegramBot = telegramBot;
    }

    @Autowired
    public WebsitesProcessor(final @Lazy NewsBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    public ReplyKeyboard getKeyboard(){
        final KeyboardRow keyboard = new KeyboardRow();
        keyboard.add(CANCEL_COMMAND);
        keyboard.add(GET_COMMAND);
        keyboard.add(SUB_WEBSITES_COMMAND);
        keyboard.add(UN_SUB_WEBSITES_COMMAND);
        keyboard.add(UPDATE_SUB_WEBSITES_COMMAND);
        keyboard.add(CREATE_CUSTOM_COMMAND);
        keyboard.add(DELETE_CUSTOM_COMMAND);
        keyboard.add(INFO_COMMAND);
        return new ReplyKeyboardMarkup(List.of(keyboard));
    }

    public boolean handleWebsitesMenuState(final long chatId, final String text) throws TelegramApiException {
        if (INFO_COMMAND.equalsIgnoreCase(text)){
            chatStates.put(chatId, new NewsBot.WebsitesMenuChatState());
            telegramBot.sendMessage(chatId, "Это меню сайтов, здесь вы можете выполнять следующие команды", getKeyboard());
            return true;
        } else if (GET_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, NewsBot.WebsitesMenuChatState.withGetWebsite());
            telegramBot.sendMessage(chatId, "Введите ID сайта", getKeyboard());
            return true;
        } else if (SUB_WEBSITES_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, new NewsBot.WebsitesMenuChatState());
            telegramBot.sendMessage(chatId, "Ваши выбранные сайты: "+ UUID.randomUUID(), getKeyboard());
            return true;
        } else if (UN_SUB_WEBSITES_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, new NewsBot.WebsitesMenuChatState());
            telegramBot.sendMessage(chatId, "Другие сайты: "+UUID.randomUUID(), getKeyboard());
            return true;
        } else if (UPDATE_SUB_WEBSITES_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, NewsBot.WebsitesMenuChatState.withUpdateSub());
            telegramBot.sendMessage(chatId, "Введите ID сайтов через пробел", getKeyboard());
            return true;
        }else if (CREATE_CUSTOM_COMMAND.equalsIgnoreCase(text)){
            chatStates.put(chatId, NewsBot.WebsitesMenuChatState.withCreateCustom());
            telegramBot.sendMessage(chatId, "Выведите ссылку на RSS сайта", getKeyboard());
            return true;
        } else if (DELETE_CUSTOM_COMMAND.equalsIgnoreCase(text)) {
            chatStates.put(chatId, NewsBot.WebsitesMenuChatState.withDeleteCustom());
            telegramBot.sendMessage(chatId, "Введите ID вашего сайта для удаления", getKeyboard());
            return true;
        } else if (CANCEL_COMMAND.equals(text)) {
            chatStates.remove(chatId);
            telegramBot.setChatState(chatId, new NewsBot.NormalChatState());
            telegramBot.sendMessage(chatId, "Вы вернулись в стартовое меню");
            return true;
        } else if (chatStates.get(chatId).getWebsite().isPresent()) {
            chatStates.put(chatId,new NewsBot.WebsitesMenuChatState());
            telegramBot.sendMessage(chatId, "Вот сайт с ID = "+text+" : "+UUID.randomUUID(), getKeyboard());
            return true;
        } else if (chatStates.get(chatId).updateSub().isPresent()) {
            chatStates.put(chatId, new NewsBot.WebsitesMenuChatState());
            telegramBot.sendMessage(chatId, "Ваши выбранные сайты обновлены : "+text, getKeyboard());
            return true;
        } else if (chatStates.get(chatId).createCustom().isPresent()){
            chatStates.put(chatId, new NewsBot.WebsitesMenuChatState());
            telegramBot.sendMessage(chatId, "Сайт с таким RSS сохранен : "+text, getKeyboard());
            return true;
        } else if(chatStates.get(chatId).deleteCustom().isPresent()){
            chatStates.put(chatId, new NewsBot.WebsitesMenuChatState());
            telegramBot.sendMessage(chatId, "Ваш созданный сайт удален", getKeyboard());
            return true;
        }
        return false;
    }

}
