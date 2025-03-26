package org.hsse.news.database.user.repositories;

import org.hsse.news.database.user.models.AuthenticationCredentials;
import org.hsse.news.database.user.models.UserDto;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.util.ComparisonUtil;
import org.hsse.news.database.util.SampleDataUtil;
import org.hsse.news.database.util.TestcontainersUtil;
import org.jdbi.v3.core.Jdbi;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class JdbiUserDtoRepositoryTest {
    @Container
    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16.4-alpine");
    private static Jdbi jdbi;

    private JdbiUserRepository repository;

    @BeforeAll
    static void beforeAll() {
        jdbi = TestcontainersUtil.prepareContainer(POSTGRES);
    }

    @BeforeEach
    void setUp() {
        SampleDataUtil.prepareUsers(jdbi);
        repository = new JdbiUserRepository(jdbi);
    }

    @AfterEach
    void tearDown() {
        SampleDataUtil.clearDatabase(jdbi);
    }

    @Test
    void testFindByIdSuccess() {
        assert SampleDataUtil.DEFAULT_USER_DTO.id() != null;
        final Optional<UserDto> userOptional = repository.findById(SampleDataUtil.DEFAULT_USER_DTO.id());

        assertTrue(userOptional.isPresent(), "userOptional should be present");
        ComparisonUtil.assertDeepEquals(SampleDataUtil.DEFAULT_USER_DTO, userOptional.get());
    }

    @Test
    void testFindByIdFail() {
        assert SampleDataUtil.NEW_USER_DTO.id() != null;
        final Optional<UserDto> userOptional = repository.findById(SampleDataUtil.NEW_USER_DTO.id());

        assertTrue(userOptional.isEmpty(), "userOptional should be empty");
    }

    /*@Test
    void testAuthenticateSuccess() { // NOPMD
        final AuthenticationCredentials credentials =
                new AuthenticationCredentials(
                        SampleDataUtil.DEFAULT_USER.email(),
                        SampleDataUtil.DEFAULT_USER.password()
                );
        final Optional<UserId> userIdOptional = repository.authenticate(credentials);

        assertTrue(userIdOptional.isPresent(), "userIdOptional should be present");
        assertEquals(SampleDataUtil.DEFAULT_USER.id(), userIdOptional.get(), "ids should be equal");
    }*/

    @Test
    void testAuthenticateIncorrectPassword() {
        final AuthenticationCredentials credentials =
                new AuthenticationCredentials(
                        SampleDataUtil.DEFAULT_USER_DTO.email(),
                        "wrong_password"
                );
        final Optional<UserId> userIdOptional = repository.authenticate(credentials);

        assertTrue(userIdOptional.isEmpty(), "userIdOptional should be empty");
    }

    @Test
    void testAuthenticateNonExistentEmail() {
        final AuthenticationCredentials credentials =
                new AuthenticationCredentials(
                        "non_existent_email@example.com",
                        SampleDataUtil.DEFAULT_USER_DTO.password()
                );
        final Optional<UserId> userIdOptional = repository.authenticate(credentials);

        assertTrue(userIdOptional.isEmpty(), "userIdOptional should be empty");
    }

    /*@Test
    void testCreateSuccess() {
        final UserDto newUserDto = new UserDto(
                SampleDataUtil.NEW_USER_DTO.email(),
                SampleDataUtil.NEW_USER_DTO.password(),
                SampleDataUtil.NEW_USER_DTO.username()
        );
        final UserDto createdUserDto = repository.create(newUserDto);

        assertNotNull(createdUserDto.id(), "createdUser should be initialized");
        ComparisonUtil.assertDeepEquals(
                newUserDto.initializeWithId(createdUserDto.id()),
                createdUserDto
        );
    }

    @Test
    void testCreateEmailConflict() {
        final UserDto newUserDto = new UserDto(
                SampleDataUtil.DEFAULT_USER_DTO.email(),
                SampleDataUtil.NEW_USER_DTO.password(),
                SampleDataUtil.NEW_USER_DTO.username()
        );

        assertThrows(
                EmailConflictException.class,
                () -> repository.create(newUserDto)
        );
    }

    @Test
    void testUpdateSuccess() {
        final UserDto userDtoToUpdate = new UserDto(
                SampleDataUtil.DEFAULT_USER_DTO.id(),
                SampleDataUtil.NEW_USER_DTO.email(),
                SampleDataUtil.NEW_USER_DTO.password(),
                SampleDataUtil.NEW_USER_DTO.username()
        );
        repository.update(userDtoToUpdate);

        assert userDtoToUpdate.id() != null;
        final Optional<UserDto> updatedUserOptional = repository.findById(userDtoToUpdate.id());

        assertTrue(updatedUserOptional.isPresent(), "updatedUserOptional should be present");
        ComparisonUtil.assertDeepEquals(userDtoToUpdate, updatedUserOptional.get());
    }

    @Test
    void testUpdateUserNotFound() {
        assertThrows(
                UserNotFoundException.class,
                () -> repository.update(SampleDataUtil.NEW_USER_DTO)
        );
    }

    @Test
    void testUpdateEmailConflict() {
        repository.create(
                new UserDto(
                        SampleDataUtil.NEW_USER_DTO.email(),
                        SampleDataUtil.NEW_USER_DTO.password(),
                        SampleDataUtil.NEW_USER_DTO.username()
                )
        );

        final UserDto userDtoToUpdate = new UserDto(
                SampleDataUtil.DEFAULT_USER_DTO.id(),
                SampleDataUtil.NEW_USER_DTO.email(),
                SampleDataUtil.NEW_USER_DTO.password(),
                SampleDataUtil.NEW_USER_DTO.username()
        );

        assertThrows(
                EmailConflictException.class,
                () -> repository.update(userDtoToUpdate)
        );
    }

    @Test
    void testDeleteSuccess() {
        assert SampleDataUtil.DEFAULT_USER_DTO.id() != null;
        repository.delete(SampleDataUtil.DEFAULT_USER_DTO.id());

        assertTrue(
                repository.findById(SampleDataUtil.DEFAULT_USER_DTO.id()).isEmpty(),
                "DEFAULT_USER should not be present"
        );
    }

    @Test
    void testDeleteUserNotFound() { // NOPMD
        assert SampleDataUtil.NEW_USER_DTO.id() != null;
        assertThrows(
                UserNotFoundException.class,
                () -> repository.delete(SampleDataUtil.NEW_USER_DTO.id())
        );

        assert SampleDataUtil.DEFAULT_USER_DTO.id() != null;
        assertTrue(
                repository.findById(SampleDataUtil.DEFAULT_USER_DTO.id()).isPresent(),
                "DEFAULT_USER should be present"
        );
    }*/
}
