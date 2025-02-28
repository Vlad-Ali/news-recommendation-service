package org.hsse.news.database.user;

import lombok.AllArgsConstructor;
import org.hsse.news.database.user.exceptions.EmailConflictException;
import org.hsse.news.database.user.exceptions.InvalidCurrentPasswordException;
import org.hsse.news.database.user.exceptions.SameNewPasswordException;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
import org.hsse.news.database.user.models.AuthenticationCredentials;
import org.hsse.news.database.user.models.User;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.user.repositories.UserRepository;
import org.hsse.news.database.util.TransactionManager;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@AllArgsConstructor
public final class UserService {
    private UserRepository userRepository;
    private TransactionManager transactionManager;

    /*public UserService(UserRepository userRepository, TransactionManager transactionManager) {
        this.userRepository = userRepository;
        this.transactionManager = transactionManager;
    }*/

    public Optional<User> findById(final UserId userId) {
        return userRepository.findById(userId);
    }

    public Optional<UserId> authenticate(final AuthenticationCredentials credentials) {
        return userRepository.authenticate(credentials);
    }

    /**
     * @throws EmailConflictException if an email conflict occurs
     */
    public User register(final User user) {
        return userRepository.create(user);
    }

    /**
     * @throws UserNotFoundException if the user does not exist
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
     * @throws UserNotFoundException if the user does not exist
     * @throws InvalidCurrentPasswordException if current password is invalid
     * @throws SameNewPasswordException if current password matches new password
     */
    public void updatePassword(
            final UserId userId, final String currentPassword, final String newPassword
    )  {
        transactionManager.useTransaction(() -> {
            final User userToUpdate =
                    userRepository.findById(userId)
                            .orElseThrow(() -> new UserNotFoundException(userId));

            if (!userToUpdate.password().equals(currentPassword)) {
                throw new InvalidCurrentPasswordException();
            }

            if (currentPassword.equals(newPassword)) {
                throw new SameNewPasswordException();
            }

            userRepository.update(
                    userToUpdate
                            .withPassword(newPassword)
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
