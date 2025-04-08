package org.hsse.news;

import lombok.SneakyThrows;
import org.hsse.news.bot.TelegramBot;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
@SuppressWarnings("PMD.UseUtilityClass")
public class Application {
    @SneakyThrows
    public static void main(final String[] args) {
        final ApplicationContext context = SpringApplication.run(Application.class, args);

        final TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
        botsApi.registerBot(context.getBean("telegramBot", TelegramBot.class));
    }

}
