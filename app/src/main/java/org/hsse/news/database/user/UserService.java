package org.hsse.news.database.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hsse.news.api.schemas.request.user.UserRegisterRequest;
import org.hsse.news.database.user.exceptions.EmailConflictException;
import org.hsse.news.database.user.exceptions.InvalidCurrentPasswordException;
import org.hsse.news.database.user.exceptions.SameNewPasswordException;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
import org.hsse.news.database.user.models.AuthenticationCredentials;
import org.hsse.news.database.user.models.User;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.user.repositories.UserRepository;
import org.hsse.news.database.util.TransactionManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
@Slf4j
public final class UserService {
    private final UserRepository userRepository;
    private final TransactionManager transactionManager;
    private final PasswordEncoder passwordEncoder;

    public Optional<User> findById(final UserId userId) {
        return userRepository.findById(userId);
    }

    public Optional<UserId> authenticate(final AuthenticationCredentials credentials) {
        return userRepository.findByEmail(credentials.email())
                .filter((user) -> {
                    log.debug("Password hash on authentication is {}", user.passwordHash());
                    return passwordEncoder.matches(credentials.password(), user.passwordHash());
                })
                .map(User::id);
    }

    /**
     * @throws EmailConflictException if an email conflict occurs
     */
    public User register(final UserRegisterRequest userRegisterRequest) {
        final String passwordHash = passwordEncoder.encode(userRegisterRequest.password());
        log.debug("Password hash on registration is {}", passwordHash);

        return userRepository.create(
                new User(userRegisterRequest.email(),
                        passwordHash,
                        userRegisterRequest.username())
        );
    }

    /**
     * @throws UserNotFoundException  if the user does not exist
     * @throws EmailConflictException if an email conflict occurs
     */
    public void update(final UserId userId, final String email, final String username) {
        transactionManager.useTransaction(() -> {
            final User userToUpdate =
                    userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException(userId));

            userRepository.update(
                    userToUpdate
                            .withEmail(email)
                            .withUsername(username)
            );
        });
    }

    /**
     * @throws UserNotFoundException           if the user does not exist
     * @throws InvalidCurrentPasswordException if current passwordHash is invalid
     * @throws SameNewPasswordException        if current passwordHash matches new passwordHash
     */
    public void updatePassword(
            final UserId userId, final String currentPassword, final String newPassword
    ) {
        transactionManager.useTransaction(() -> {
            final User userToUpdate =
                    userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException(userId));

            if (!passwordEncoder.matches(currentPassword, userToUpdate.passwordHash())) {
                throw new InvalidCurrentPasswordException();
            }

            if (passwordEncoder.matches(newPassword, userToUpdate.passwordHash())) {
                throw new SameNewPasswordException();
            }

            userRepository.update(
                    userToUpdate
                            .withPasswordHash(passwordEncoder.encode(newPassword))
            );
        });
    }

    /**
     * @throws UserNotFoundException if the user does not exist
     */
    public void delete(final UserId userId) {
        userRepository.delete(userId);
    }
}
