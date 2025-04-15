package org.hsse.news.tracker;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hsse.news.bot.ChatId;
import org.hsse.news.bot.Message;
import org.hsse.news.bot.TelegramBot;
import org.hsse.news.database.article.UserArticlesService;
import org.hsse.news.database.article.models.ArticleTopRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class ArticleTopTracker {
    private final UserArticlesService userArticlesService;

    @Setter(onMethod_ = {@Lazy, @Autowired})
    private TelegramBot bot;

    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void sendArticleTop() {
        final StringBuilder text = new StringBuilder("Самые популярные статьи за неделю: ");
        for (final ArticleTopRecord topRecord : userArticlesService.findTopKArticles(5)) {
            text.append('\n').append(topRecord.articleId()).append(" — ");
            if (topRecord.grade() > 0) {
                text.append('+');
            }
            text.append(topRecord.grade()).append(" \uD83D\uDC4D");
        }

        for (final ChatId chatId : bot.getActiveChats()) {
            bot.sendMessage(chatId, Message.builder().text(text.toString()).build());
        }
    }
}
