package org.hsse.news.database.topic.models;

import org.hsse.news.util.AbstractId;
import org.jetbrains.annotations.NotNull;

public final class TopicId extends AbstractId<Long> {
    public TopicId(final @NotNull Long value) {
        super(value);
    }

    public TopicId fromString(final String string) {
        return new TopicId(Long.parseLong(string));
    }
}
