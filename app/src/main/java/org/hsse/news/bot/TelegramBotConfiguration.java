package org.hsse.news.bot;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class TelegramBotConfiguration {
    @Bean
    TelegramBot telegramBot(final @Value("${tg-bot.token}") String token,
                            List<BotCustomizer> customizers) throws Exception {
        final var telegramBot = new TelegramBot(token);
        for (BotCustomizer customizer : customizers) {
            customizer.customize(telegramBot);
        }
        return telegramBot;
    }
}
