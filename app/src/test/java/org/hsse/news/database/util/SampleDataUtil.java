package org.hsse.news.database.util;

import org.hsse.news.api.schemas.shared.WebsiteInfo;
import org.hsse.news.database.user.models.UserDto;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.website.models.WebsiteDto;
import org.hsse.news.database.website.models.WebsiteId;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.UUID;

public final class SampleDataUtil {
    public static final UserDto DEFAULT_USER_DTO =
            new UserDto(
                    new UserId(UUID.fromString("11111111-1111-1111-1111-111111111111")),
                    "test@example.com",
                    "test_password",
                    "TestUser",
                    111_111L
            );
    public static final UserDto NEW_USER_DTO =
            new UserDto(
                    new UserId(UUID.fromString("22222222-2222-2222-2222-222222222222")),
                    "new@example.com",
                    "new_password",
                    "NewUser",
                    222_222L
            );
    public static final WebsiteDto DEFAULT_WEBSITE_DTO = new WebsiteDto(new WebsiteId(1L), "https://alex.com/RSS", "xxxx", new UserId(UUID.fromString("11111111-1111-1111-1111-111111111111")));
    public static final WebsiteInfo DEFAULT_WEBSITE_INFO = new WebsiteInfo(1L, "https://alex.com/RSS", "xxxx");
    public static final WebsiteDto NEW_WEBSITE_DTO = new WebsiteDto(new WebsiteId(2L), "https://mark.com/RSS","yyyy",new UserId(UUID.fromString("11111111-1111-1111-1111-111111111111")));
    public static final WebsiteInfo NEW_WEBSITE_INFO = new WebsiteInfo(2L, "https://mark.com/RSS","yyyy");
    @SuppressWarnings("SqlWithoutWhere")
    public static void clearDatabase(final Jdbi jdbi) {
        jdbi.useHandle(handle -> {
            final List<String> tables =
                    handle.createQuery(
                            "SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'"
                            )
                            .mapTo(String.class)
                            .list();
            tables.remove("flyway_schema_history");

            // noinspection SqlSourceToSinkFlow
            tables.forEach(table -> handle.execute("DELETE FROM " + table));
        });
    }

    public static void prepareUsers(final Jdbi jdbi) {
        assert DEFAULT_USER_DTO.id() != null;
        jdbi.useTransaction(handle ->
                handle.createUpdate(
                        "INSERT INTO users (user_id, email, password, username) " +
                                "VALUES (:user_id, :email, :password, :username)"
                        )
                        .bind("user_id", DEFAULT_USER_DTO.id().value())
                        .bind("email", DEFAULT_USER_DTO.email())
                        .bind("password", DEFAULT_USER_DTO.password())
                        .bind("username", DEFAULT_USER_DTO.username())
                        .execute()
        );
    }

    private SampleDataUtil() {}
}
