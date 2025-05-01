package org.hsse.news.database.userrequest.model;

import org.hsse.news.database.entity.UserEntity;
import org.hsse.news.database.entity.UserRequestEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public record UserRequestDto(@Nullable Long requestId, @NotNull UUID userId, @NotNull String url, @NotNull Instant requestTime) {
    public UserRequestEntity toEntity(final UserEntity user){
        return new UserRequestEntity(user, url, requestTime);
    }
}
