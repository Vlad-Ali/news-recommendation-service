package org.hsse.news.database.util;

import org.flywaydb.core.Flyway;
import org.hsse.news.database.user.models.User;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.website.models.Website;
import org.hsse.news.database.website.models.WebsiteId;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.testcontainers.containers.PostgreSQLContainer;

public final class TestcontainersUtil { // NOPMD - suppressed TestClassWithoutTestCases - "Testcontainers" is a tool name
    public static Jdbi prepareContainer(final PostgreSQLContainer<?> postgres) {
        final Flyway flyway =
                Flyway.configure()
                        .outOfOrder(true)
                        .locations("classpath:db/migrations")
                        .dataSource(
                                postgres.getJdbcUrl(),
                                postgres.getUsername(),
                                postgres.getPassword()
                        )
                        .load();
        flyway.migrate();

        final Jdbi jdbi =
                Jdbi.create(postgres.getJdbcUrl(), postgres.getUsername(), postgres.getPassword());

        jdbi.registerRowMapper(User.class, ConstructorMapper.of(User.class));
        jdbi.registerRowMapper(UserId.class, ConstructorMapper.of(UserId.class));
        jdbi.registerRowMapper(Website.class, ConstructorMapper.of(Website.class));
        jdbi.registerRowMapper(WebsiteId.class, ConstructorMapper.of(WebsiteId.class));

        return jdbi;
    }

    private TestcontainersUtil() {}
}
