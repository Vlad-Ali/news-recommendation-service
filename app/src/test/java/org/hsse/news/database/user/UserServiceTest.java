package org.hsse.news.database.user;

import org.hsse.news.api.schemas.request.user.UserRegisterRequest;
import org.hsse.news.database.user.exceptions.EmailConflictException;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
import org.hsse.news.database.user.models.AuthenticationCredentials;
import org.hsse.news.database.user.models.User;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.user.repositories.UserRepository;
import org.hsse.news.database.util.ComparisonUtil;
import org.hsse.news.database.util.SampleDataUtil;
import org.hsse.news.database.util.TransactionManager;
import org.hsse.news.util.MockitoUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.mockito.ArgumentMatchers.refEq;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository repositoryMock;

    @Mock
    private TransactionManager transactionManagerMock;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService service;

    @Test
    void testFindByIdSuccess() {
        assert SampleDataUtil.DEFAULT_USER.id() != null;
        Mockito.when(repositoryMock.findById(SampleDataUtil.DEFAULT_USER.id()))
                .thenReturn(Optional.of(SampleDataUtil.DEFAULT_USER));

        final Optional<User> userOptional = service.findById(SampleDataUtil.DEFAULT_USER.id());

        Mockito.verify(repositoryMock).findById(SampleDataUtil.DEFAULT_USER.id());
        assertTrue(userOptional.isPresent(), "userOptional should be present");
        ComparisonUtil.assertDeepEquals(SampleDataUtil.DEFAULT_USER, userOptional.get());
    }

    @Test
    void testFindByIdFail() {
        assert SampleDataUtil.DEFAULT_USER.id() != null;
        Mockito.when(repositoryMock.findById(SampleDataUtil.DEFAULT_USER.id()))
                .thenReturn(Optional.empty());

        final Optional<User> userOptional = service.findById(SampleDataUtil.DEFAULT_USER.id());

        Mockito.verify(repositoryMock).findById(SampleDataUtil.DEFAULT_USER.id());
        assertTrue(userOptional.isEmpty(), "userOptional should be empty");
    }

    @Test
    void testRegisterSuccess() {
        assert SampleDataUtil.DEFAULT_USER.id() != null;
        final User newUser = new User(
                SampleDataUtil.DEFAULT_USER.email(),
                SampleDataUtil.DEFAULT_PASSWORD_HASH,
                SampleDataUtil.DEFAULT_USER.username()
        );
        Mockito.when(repositoryMock.create(refEq(newUser)))
                .thenReturn(newUser.initializeWithId(SampleDataUtil.DEFAULT_USER.id()));
        Mockito.when(passwordEncoder.encode(SampleDataUtil.DEFAULT_PASSWORD))
                .thenReturn(SampleDataUtil.DEFAULT_PASSWORD_HASH);

        final User registeredUser = service.register(new UserRegisterRequest(
                SampleDataUtil.DEFAULT_USER.email(),
                SampleDataUtil.DEFAULT_PASSWORD,
                SampleDataUtil.DEFAULT_USER.username()));

        Mockito.verify(repositoryMock).create(refEq(newUser));
        Mockito.verify(passwordEncoder).encode(SampleDataUtil.DEFAULT_PASSWORD);

        ComparisonUtil.assertDeepEquals(
                SampleDataUtil.DEFAULT_USER.withPasswordHash(SampleDataUtil.DEFAULT_PASSWORD_HASH),
                registeredUser);
    }

    @Test
    void testRegisterEmailConflict() {
        assert SampleDataUtil.DEFAULT_USER.id() != null;
        final User newUser = new User(
                SampleDataUtil.DEFAULT_USER.email(),
                SampleDataUtil.DEFAULT_PASSWORD_HASH,
                SampleDataUtil.DEFAULT_USER.username()
        );
        Mockito.when(repositoryMock.create(refEq(newUser)))
               .thenThrow(EmailConflictException.class);
        Mockito.when(passwordEncoder.encode(SampleDataUtil.DEFAULT_PASSWORD))
                .thenReturn(SampleDataUtil.DEFAULT_PASSWORD_HASH);

        assertThrows(EmailConflictException.class, () -> service.register(new UserRegisterRequest(
                SampleDataUtil.DEFAULT_USER.email(),
                SampleDataUtil.DEFAULT_PASSWORD,
                SampleDataUtil.DEFAULT_USER.username())));

        Mockito.verify(repositoryMock).create(refEq(newUser));
        Mockito.verify(passwordEncoder).encode(SampleDataUtil.DEFAULT_PASSWORD);
    }

    @Test
    void testUpdateSuccess() {
        assert SampleDataUtil.DEFAULT_USER.id() != null;
        final User userToUpdate = new User(
                SampleDataUtil.DEFAULT_USER.id(),
                SampleDataUtil.NEW_USER.email(),
                SampleDataUtil.DEFAULT_USER.passwordHash(),
                SampleDataUtil.NEW_USER.username()
        );
        MockitoUtil.setupUseTransaction(transactionManagerMock);
        Mockito.when(repositoryMock.findById(SampleDataUtil.DEFAULT_USER.id()))
                .thenReturn(Optional.of(SampleDataUtil.DEFAULT_USER));
        Mockito.doNothing()
                .when(repositoryMock).update(userToUpdate);

        service.update(
                SampleDataUtil.DEFAULT_USER.id(),
                SampleDataUtil.NEW_USER.email(),
                SampleDataUtil.NEW_USER.username()
        );

        final ArgumentCaptor<User> argument = ArgumentCaptor.forClass(User.class);
        Mockito.verify(repositoryMock).update(argument.capture());
        ComparisonUtil.assertDeepEquals(userToUpdate, argument.getValue());
    }

    @Test
    void testUpdateUserNotFound() {
        assert SampleDataUtil.DEFAULT_USER.id() != null;
        MockitoUtil.setupUseTransaction(transactionManagerMock);
        Mockito.when(repositoryMock.findById(SampleDataUtil.DEFAULT_USER.id()))
                        .thenReturn(Optional.empty());

        assertThrows(
                UserNotFoundException.class,
                () -> service.update(
                        SampleDataUtil.DEFAULT_USER.id(),
                        SampleDataUtil.NEW_USER.email(),
                        SampleDataUtil.NEW_USER.username()
                )
        );
    }

    @Test
    void testUpdateEmailConflict() {
        assert SampleDataUtil.DEFAULT_USER.id() != null;
        final User userToUpdate = new User(
                SampleDataUtil.DEFAULT_USER.id(),
                SampleDataUtil.NEW_USER.email(),
                SampleDataUtil.DEFAULT_USER.passwordHash(),
                SampleDataUtil.NEW_USER.username()
        );
        MockitoUtil.setupUseTransaction(transactionManagerMock);
        Mockito.when(repositoryMock.findById(SampleDataUtil.DEFAULT_USER.id()))
                .thenReturn(Optional.of(SampleDataUtil.DEFAULT_USER));
        Mockito.doThrow(EmailConflictException.class)
                .when(repositoryMock).update(userToUpdate);

        assertThrows(
                EmailConflictException.class,
                () -> service.update(
                        SampleDataUtil.DEFAULT_USER.id(),
                        SampleDataUtil.NEW_USER.email(),
                        SampleDataUtil.NEW_USER.username()
                )
        );
    }

    @Test
    void testAuthenticateSuccess() { // NOPMD
        final AuthenticationCredentials credentials =
                new AuthenticationCredentials(
                        SampleDataUtil.DEFAULT_USER.email(),
                        SampleDataUtil.DEFAULT_PASSWORD
                );

        Mockito.when(repositoryMock.findByEmail(SampleDataUtil.DEFAULT_USER.email()))
                .thenReturn(Optional.of(SampleDataUtil.DEFAULT_USER
                        .withPasswordHash(SampleDataUtil.DEFAULT_PASSWORD_HASH)));
        Mockito.when(passwordEncoder.matches(SampleDataUtil.DEFAULT_PASSWORD, SampleDataUtil.DEFAULT_PASSWORD_HASH))
                .thenReturn(true);

        final Optional<UserId> userIdOptional = service.authenticate(credentials);

        assertTrue(userIdOptional.isPresent(), "userIdOptional should be present");
        assertEquals(SampleDataUtil.DEFAULT_USER.id(), userIdOptional.get(), "ids should be equal");
    }

    @Test
    void testAuthenticateIncorrectPassword() {
        final AuthenticationCredentials credentials =
                new AuthenticationCredentials(
                        SampleDataUtil.DEFAULT_USER.email(),
                        "wrong password"
                );

        Mockito.when(repositoryMock.findByEmail(SampleDataUtil.DEFAULT_USER.email()))
                .thenReturn(Optional.of(SampleDataUtil.DEFAULT_USER
                        .withPasswordHash(SampleDataUtil.DEFAULT_PASSWORD_HASH)));
        Mockito.when(passwordEncoder.matches("wrong password", SampleDataUtil.DEFAULT_PASSWORD_HASH))
                .thenReturn(false);

        final Optional<UserId> userIdOptional = service.authenticate(credentials);

        assertTrue(userIdOptional.isEmpty(), "userIdOptional should be empty");
    }

    @Test
    void testAuthenticateNonExistentEmail() {
        final AuthenticationCredentials credentials =
                new AuthenticationCredentials(
                        "non_existent_email@example.com",
                        SampleDataUtil.DEFAULT_PASSWORD
                );

        Mockito.when(repositoryMock.findByEmail("non_existent_email@example.com"))
                .thenReturn(Optional.empty());

        final Optional<UserId> userIdOptional = service.authenticate(credentials);

        assertTrue(userIdOptional.isEmpty(), "userIdOptional should be empty");
    }
}
