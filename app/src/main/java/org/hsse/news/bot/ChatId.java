package org.hsse.news.bot;

public record ChatId(long value) {
    public ChatId fromString(final String string) {
        return new ChatId(Long.parseLong(string));
    }
}
