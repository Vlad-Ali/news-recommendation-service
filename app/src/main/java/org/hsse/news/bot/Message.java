package org.hsse.news.bot;

import lombok.Builder;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;

@Builder
public record Message(String text, InlineKeyboardMarkup keyboard,
                      Function<String, Message> onNextMessage,
                      Optional<MessageId> replace) {
    public static class MessageBuilder {
        public MessageBuilder verticalKeyboard(List<InlineKeyboardButton> buttons) {
            keyboard = new InlineKeyboardMarkup(buttons.stream().map(List::of).toList());
            return this;
        }

        public MessageBuilder singleButton(InlineKeyboardButton button) {
            keyboard = new InlineKeyboardMarkup(List.of(List.of(button)));
            return this;
        }
    }
}