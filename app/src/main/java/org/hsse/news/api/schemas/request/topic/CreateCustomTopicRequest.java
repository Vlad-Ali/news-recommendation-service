package org.hsse.news.api.schemas.request.topic;

import org.hsse.news.database.user.models.UserId;
import org.jetbrains.annotations.NotNull;

public record CreateCustomTopicRequest(
        @NotNull String name, @NotNull UserId creatorId
) {
}
