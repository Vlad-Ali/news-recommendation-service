package org.hsse.news.database.user;

import org.hsse.news.database.entity.UserEntity;
import org.hsse.news.database.user.exceptions.EmailConflictException;
import org.hsse.news.database.user.exceptions.InvalidCurrentPasswordException;
import org.hsse.news.database.user.exceptions.SameNewPasswordException;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
import org.hsse.news.database.user.models.AuthenticationCredentials;
import org.hsse.news.database.user.models.UserDto;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.user.repositories.JpaUsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    private final JpaUsersRepository usersRepository;
    private static final Logger LOG = LoggerFactory.getLogger(UserService.class);

    public UserService(final JpaUsersRepository usersRepository) {
        this.usersRepository = usersRepository;
    }

    public Optional<UserDto> findById(final UserId userId) {
        LOG.debug("Method findById called");
        final Optional<UserEntity> optionalUser= usersRepository.findById(userId.value());
        if (optionalUser.isEmpty()){
            throw new UserNotFoundException(userId);
        }
        return Optional.of(optionalUser.get().toUserDto());
    }

    public Optional<UserId> authenticate(final AuthenticationCredentials credentials) {
        LOG.debug("Method authenticate called");
        final Optional<UserEntity> optionalUser = usersRepository.findByEmailAndPassword(credentials.email(), credentials.password());
        return optionalUser.map(userEntity -> new UserId(userEntity.getId()));
    }

    /**
     * @throws EmailConflictException if an email conflict occurs
     */
    public UserDto register(final UserDto userDto) {
        LOG.debug("Method register called");
        final Optional<UserEntity> optionalUser = usersRepository.findByEmail(userDto.email());
        if (optionalUser.isPresent()){
            throw new EmailConflictException("Email is already used");
        }
        final UserEntity userEntity = userDto.toUserEntity();
        final UserEntity savedUser = usersRepository.save(userEntity);
        return savedUser.toUserDto();
    }

    /**
     * @throws UserNotFoundException if the user does not exist
     * @throws EmailConflictException if an email conflict occurs
     */
    @Transactional
    public void update(final UserId userId, final String email, final String username) {
        LOG.debug("Method update called");
        final Optional<UserEntity> optionalUser = usersRepository.findById(userId.value());
        if (optionalUser.isEmpty()) {
            throw new UserNotFoundException(userId);
        }
        final Optional<UserEntity> optionalUserEntity = usersRepository.findByEmailAndNotId(userId.value(), email);
        if (optionalUserEntity.isPresent()){
            throw new EmailConflictException("Email " + email + " is already taken");
        }
        final UserEntity userEntity = optionalUser.get();
        userEntity.setEmail(email);
        userEntity.setUsername(username);
        usersRepository.save(userEntity);
    }

    /**
     * @throws UserNotFoundException if the user does not exist
     * @throws InvalidCurrentPasswordException if current password is invalid
     * @throws SameNewPasswordException if current password matches new password
     */
    @Transactional
    public void updatePassword(
            final UserId userId, final String currentPassword, final String newPassword
    )  {
        final UserEntity userToUpdate =
                usersRepository.findById(userId.value())
                            .orElseThrow(() -> new UserNotFoundException(userId));

            if (!userToUpdate.getPassword().equals(currentPassword)) {
                throw new InvalidCurrentPasswordException();
            }

            if (currentPassword.equals(newPassword)) {
                throw new SameNewPasswordException();
            }

            userToUpdate.setPassword(newPassword);
            usersRepository.save(userToUpdate);
    }

    /**
     * @throws UserNotFoundException if the user does not exist
     */
    @Transactional
    public void delete(final UserId userId) {
        LOG.debug("Method delete called");
        final Optional<UserEntity> optionalUser = usersRepository.findById(userId.value());
        if (optionalUser.isEmpty()){
            throw new UserNotFoundException(userId);
        }
        usersRepository.deleteById(userId.value());
    }
}
