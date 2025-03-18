package org.hsse.news.database.topic.models;

import org.hsse.news.database.user.models.UserId;
import org.jetbrains.annotations.NotNull;

public record TopicDto(@NotNull TopicId id, @NotNull String name, @NotNull UserId creator) {
}
