package org.hsse.news.database.user.repositories;

import org.hsse.news.database.user.exceptions.EmailConflictException;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
import org.hsse.news.database.user.models.AuthenticationCredentials;
import org.hsse.news.database.user.models.UserDto;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.util.JdbiProvider;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class JdbiUserRepository implements UserRepository {
    private final Jdbi jdbi;

    public JdbiUserRepository(final @NotNull Jdbi jdbi) {
        this.jdbi = jdbi;
    }

    public JdbiUserRepository() {
        this(JdbiProvider.get());
    }

    @Override
    public Optional<UserDto> findById(final @NotNull UserId userId) {
        return jdbi.inTransaction(handle ->
                handle.createQuery("SELECT * FROM users WHERE user_id = :user_id")
                        .bind("user_id", userId.value()) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                        .mapTo(UserDto.class)
                        .findFirst()
        );
    }

    @Override
public Optional<UserId> authenticate(final @NotNull AuthenticationCredentials credentials) {
        return jdbi.inTransaction(handle ->
                handle.createQuery("SELECT * FROM users WHERE email = :email")
                        .bind("email", credentials.email())
                        .mapTo(UserDto.class)
                        .findFirst()
                        .filter(user -> user.password().equals(credentials.password()))
                        .map(UserDto::id)
        );
    }

    @Override
    public @NotNull UserDto create(final @NotNull UserDto userDto) {
        return jdbi.inTransaction(handle -> {
            try {
                return userDto.initializeWithId(
                        handle.createUpdate(
                                "INSERT INTO users (email, password, username) " +
                                        "VALUES (:email, :password, :username)"
                                )
                                .bind("email", userDto.email())
                                .bind("password", userDto.password())
                                .bind("username", userDto.username())
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
    public void update(final @NotNull UserDto userDto) {
        if (userDto.id() == null) {
            throw new UserNotFoundException(null);
        }

        jdbi.useTransaction(handle -> {
            try {
                final Optional<UserDto> userOptional = findById(userDto.id());

                if (userOptional.isEmpty()) {
                    throw new UserNotFoundException(userDto.id());
                }

                handle.createUpdate("UPDATE users SET email = :email, password = :password, username = :username WHERE user_id = :user_id")
                        .bind("email", userDto.email())
                        .bind("password", userDto.password())
                        .bind("username", userDto.username())
                        .bind("user_id", userDto.id().value())
                        .execute();
            } catch (UnableToExecuteStatementException e) {
                throw new EmailConflictException(e);
            }
        });
    }

    @Override
    public void delete(final @NotNull UserId userId) {
        jdbi.useTransaction(handle -> {
            final Optional<UserDto> userOptional = findById(userId);

            if (userOptional.isEmpty()) {
                throw new UserNotFoundException(userId);
            }

            handle.createUpdate("DELETE FROM users WHERE user_id = :user_id")
                    .bind("user_id", userId.value())
                    .execute();
        });
    }
}
