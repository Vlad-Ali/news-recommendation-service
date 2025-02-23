package org.hsse.news;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.flywaydb.core.Flyway;
import org.hsse.news.database.article.models.Article;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.topic.models.Topic;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.user.models.User;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.website.models.Website;
import org.hsse.news.database.website.models.WebsiteId;
import org.hsse.news.util.JdbiProvider;
import org.hsse.news.util.SubApplication;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.List;

@SpringBootApplication
public class Application {
    private final List<SubApplication> subApplications;

    public Application(final List<SubApplication> subApplications) {
        this.subApplications = subApplications;
    }

    private void start() {
        subApplications.forEach(SubApplication::run);
    }

    public static void main(final String[] args) {
        initializeDatabase();
        SpringApplication.run(Application.class, args);
    }

    private static void initializeDatabase() {
        final Config config = ConfigFactory.load();

        final Flyway flyway =
            Flyway.configure()
                .outOfOrder(true)
                .locations("classpath:db/migrations")
                .dataSource(
                        config.getString("database.url"),
                        config.getString("database.user"),
                        config.getString("database.password")
                )
                .load();
        flyway.migrate();

        final Jdbi jdbi = Jdbi.create(
                config.getString("database.url"),
                config.getString("database.user"),
                config.getString("database.password")
        );

        jdbi.registerRowMapper(User.class, ConstructorMapper.of(User.class));
        jdbi.registerRowMapper(UserId.class, ConstructorMapper.of(UserId.class));
        jdbi.registerRowMapper(Topic.class, ConstructorMapper.of(Topic.class));
        jdbi.registerRowMapper(TopicId.class, ConstructorMapper.of(TopicId.class));
        jdbi.registerRowMapper(Website.class, ConstructorMapper.of(Website.class));
        jdbi.registerRowMapper(WebsiteId.class, ConstructorMapper.of(WebsiteId.class));
        jdbi.registerRowMapper(Article.class, ConstructorMapper.of(Article.class));
        jdbi.registerRowMapper(ArticleId.class, ConstructorMapper.of(ArticleId.class));

        JdbiProvider.initialize(jdbi);
    }
}
