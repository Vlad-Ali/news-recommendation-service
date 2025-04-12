package org.hsse.news.database.user.models;

import org.hsse.news.database.entity.UserEntity;
import org.hsse.news.database.user.exceptions.UserInitializationException;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record UserDto(
        @Nested @Nullable UserId id,
        @NotNull String email, @NotNull String password, @NotNull String username, @NotNull Long chatId
) {
    @JdbiConstructor
    public UserDto {}

    public UserDto(
            final @NotNull String email, final @NotNull String password,
            final @NotNull String username, final @NotNull Long chatId
    ) {
        this(null, email, password, username, chatId);
    }

    public UserDto initializeWithId(final @NotNull UserId newId) {
        if (id != null) {
            throw new UserInitializationException("User is already initialized");
        }

        return new UserDto(newId, email, password, username, chatId);
    }

    public UserDto withEmail(final @NotNull String newEmail) {
        return new UserDto(id, newEmail, password, username, chatId);
    }

    public UserDto withPassword(final @NotNull String newPassword) {
        return new UserDto(id, email, newPassword, username, chatId);
    }

    public UserDto withUsername(final @NotNull String newUsername) {
        return new UserDto(id, email, password, newUsername, chatId);
    }

    public UserEntity toUserEntity(){
        return new UserEntity(this.email(), this.password(), this.username(), this.chatId);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof UserDto userDto)) {
            return false;
        }

        return id != null && id.equals(userDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
