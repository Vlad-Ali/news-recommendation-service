package org.hsse.news.bot;

public record MessageId(int value) {
    public MessageId fromString(String string) {
        return new MessageId(Integer.parseInt(string));
    }
}
