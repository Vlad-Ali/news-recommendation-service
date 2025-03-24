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
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass")
public class Application {
    public static void main(final String[] args) throws TelegramApiException {
        initializeDatabase();

        final ApplicationContext context = SpringApplication.run(Application.class, args);

        final TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(context.getBean("telegramBot", TelegramBot.class));
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
