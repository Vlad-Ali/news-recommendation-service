package org.hsse.news.database.user.models;

import org.hsse.news.database.user.exceptions.UserInitializationException;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record User(
        @Nested @Nullable UserId id,
        @NotNull String email,
        @ColumnName("password") @NotNull String passwordHash,
        @NotNull String username
) {
    @JdbiConstructor
    public User {}

    public User(
            final @NotNull String email, final @NotNull String passwordHash,
            final @NotNull String username
    ) {
        this(null, email, passwordHash, username);
    }

    public User initializeWithId(final @NotNull UserId newId) {
        if (id != null) {
            throw new UserInitializationException("User is already initialized");
        }

        return new User(newId, email, passwordHash, username);
    }

    public User withEmail(final @NotNull String newEmail) {
        return new User(id, newEmail, passwordHash, username);
    }

    public User withPasswordHash(final @NotNull String newPasswordHash) {
        return new User(id, email, newPasswordHash, username);
    }

    public User withUsername(final @NotNull String newUsername) {
        return new User(id, email, passwordHash, newUsername);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof User user)) {
            return false;
        }

        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
