package org.hsse.news;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.hsse.news.api.schemas.shared.WebsiteInfo;
import org.hsse.news.database.article.models.Article;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.user.models.UserDto;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.util.JdbiProvider;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass")
public class Application {
    public static void main(final String[] args) {
        final ApplicationContext context = SpringApplication.run(Application.class, args);

        final TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(context.getBean("telegramBot", TelegramBot.class));
    }

}
