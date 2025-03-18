package org.hsse.news.database.user.repositories;

import org.hsse.news.database.user.exceptions.EmailConflictException;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
import org.hsse.news.database.user.models.AuthenticationCredentials;
import org.hsse.news.database.user.models.UserDto;
import org.hsse.news.database.user.models.UserId;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public interface UserRepository {
    Optional<UserDto> findById(@NotNull UserId userId);

    Optional<UserId> authenticate(@NotNull AuthenticationCredentials credentials);

    /**
     * @throws EmailConflictException if an email conflict occurs
     */
    @NotNull UserDto create(@NotNull UserDto userDto);

    /**
     * @throws UserNotFoundException if the user does not exist
     * @throws EmailConflictException if an email conflict occurs
     */
    void update(@NotNull UserDto userDto);

    /**
     * @throws UserNotFoundException if the user does not exist
     */
    void delete(@NotNull UserId userId);
}
