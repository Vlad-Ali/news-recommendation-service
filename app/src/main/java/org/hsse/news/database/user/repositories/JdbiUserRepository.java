package org.hsse.news.database.user.repositories;

import org.hsse.news.database.user.exceptions.EmailConflictException;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
import org.hsse.news.database.user.models.User;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.util.JdbiProvider;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public final class JdbiUserRepository implements UserRepository {
    private final Jdbi jdbi;

    public JdbiUserRepository(final @NotNull Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public JdbiUserRepository() {
        this(JdbiProvider.get());
    }

    @Override
    public Optional<User> findById(final @NotNull UserId userId) {
        return jdbi.inTransaction(handle ->
                handle.createQuery("SELECT * FROM users WHERE user_id = :user_id")
                        .bind("user_id", userId.value()) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                        .mapTo(User.class)
                        .findFirst()
        );
    }

    @Override
    public Optional<User> findByEmail(final @NotNull String email) {
        return jdbi.inTransaction(handle ->
                handle.createQuery("SELECT * FROM users WHERE email = :email")
                        .bind("email", email)
                        .mapTo(User.class)
                        .findFirst()
        );
    }

    @Override
    public @NotNull User create(final @NotNull User user) {
        return jdbi.inTransaction(handle -> {
            try {
                return user.initializeWithId(
                        handle.createUpdate(
                                "INSERT INTO users (email, password, username) " +
                                        "VALUES (:email, :passwordHash, :username)"
                                )
                                .bind("email", user.email())
                                .bind("passwordHash", user.passwordHash())
                                .bind("username", user.username())
                                .executeAndReturnGeneratedKeys("user_id")
                                .mapTo(UserId.class)
                                .one()
                );
            } catch (UnableToExecuteStatementException e) {
                throw new EmailConflictException(e);
            }
        });
    }

    @Override
    public void update(final @NotNull User user) {
        if (user.id() == null) {
            throw new UserNotFoundException(null);
        }

        jdbi.useTransaction(handle -> {
            try {
                final Optional<User> userOptional = findById(user.id());

                if (userOptional.isEmpty()) {
                    throw new UserNotFoundException(user.id());
                }

                handle.createUpdate("UPDATE users SET email = :email, passwordHash = :passwordHash, username = :username WHERE user_id = :user_id")
                        .bind("email", user.email())
                        .bind("passwordHash", user.passwordHash())
                        .bind("username", user.username())
                        .bind("user_id", user.id().value())
                        .execute();
            } catch (UnableToExecuteStatementException e) {
                throw new EmailConflictException(e);
            }
        });
    }

    @Override
    public void delete(final @NotNull UserId userId) {
        jdbi.useTransaction(handle -> {
            final Optional<User> userOptional = findById(userId);

            if (userOptional.isEmpty()) {
                throw new UserNotFoundException(userId);
            }

            handle.createUpdate("DELETE FROM users WHERE user_id = :user_id")
                    .bind("user_id", userId.value())
                    .execute();
        });
    }
}
