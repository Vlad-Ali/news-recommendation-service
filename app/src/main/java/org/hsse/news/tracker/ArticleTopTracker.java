package org.hsse.news.tracker;

import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hsse.news.bot.article.ArticlesBotHandlers;
import org.hsse.news.bot.BotMapping;
import org.hsse.news.bot.ChatId;
import org.hsse.news.bot.Message;
import org.hsse.news.bot.TelegramBot;
import org.hsse.news.database.article.ArticlesService;
import org.hsse.news.database.article.UserArticlesService;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.article.models.ArticleTopRecord;
import org.hsse.news.database.user.UserService;
import org.hsse.news.database.user.models.UserDto;
import org.hsse.news.dto.ArticleDto;
import org.hsse.news.dto.RequestUserArticleDto;
import org.hsse.news.util.Grade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@EnableScheduling
@RequiredArgsConstructor
public class ArticleTopTracker {
    private final static String LIKE_COMMAND = "/like-from-top";
    private final static String DISLIKE_COMMAND = "/dislike-from-top";
    private final static String REMOVE_GRADE_COMMAND = "/unlike-from-top";

    private final UserService userService;
    private final ArticlesService articlesService;
    private final UserArticlesService userArticlesService;
    private final ArticlesBotHandlers articlesBotHandlers;

    @Setter(onMethod_ = {@Lazy, @Autowired})
    private TelegramBot bot;

    private Message buildMessage(final int place, final ArticleTopRecord topRecord) {
        final ArticleDto articleDto = articlesService.findById(new ArticleId(topRecord.articleId()));
        final String defaultText = articlesBotHandlers.getArticleMessage(articleDto,
                userArticlesService.getArticleLikeCount(topRecord.articleId()),
                userArticlesService.getArticleDislikeCount(topRecord.articleId()));

        return Message.builder().text("#" + place + " на этой неделе:\n" + defaultText)
                .verticalKeyboard(List.of(
                        InlineKeyboardButton.builder().text("Поставить лайк")
                                .callbackData(LIKE_COMMAND + " " + topRecord.articleId()).build(),
                        InlineKeyboardButton.builder().text("Поставить дизлайк")
                                .callbackData(DISLIKE_COMMAND + " " + topRecord.articleId()).build(),
                        InlineKeyboardButton.builder().text("Убрать оценку")
                                .callbackData(REMOVE_GRADE_COMMAND + " " + topRecord.articleId()).build()
                ))
                .build();
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
    public void sendArticleTop() throws InterruptedException {
        final List<ArticleTopRecord> top = userArticlesService.findTopKArticles(5);
        for (int i = top.size() - 1; i >= 0; --i) {
            final Message message = buildMessage(i + 1, top.get(i));
            for (final ChatId chatId : bot.getActiveChats()) {
                bot.sendMessage(chatId, message);
            }
            Thread.sleep(1000);
        }
    }

    @BotMapping(LIKE_COMMAND)
    public void like(final ChatId chatId, final ArticleId articleId) {
        final UserDto user = userService.findByChatId(chatId.value())
                .orElseThrow(() -> new RuntimeException("User with chat id " + chatId + " not found"));
        if (user.id() == null) {
            throw new RuntimeException("User id can't be null");
        }

        userArticlesService.likeUserArticle(
                new RequestUserArticleDto(articleId.value(), user.id().value(), Grade.LIKE)
        );
    }

    @BotMapping(DISLIKE_COMMAND)
    public void dislike(final ChatId chatId, final ArticleId articleId) {
        final UserDto user = userService.findByChatId(chatId.value())
                .orElseThrow(() -> new RuntimeException("User with chat id " + chatId + " not found"));
        if (user.id() == null) {
            throw new RuntimeException("User id can't be null");
        }

        userArticlesService.likeUserArticle(
                new RequestUserArticleDto(articleId.value(), user.id().value(), Grade.LIKE)
        );
    }

    @BotMapping(REMOVE_GRADE_COMMAND)
    public void removeGrade(final ChatId chatId, final ArticleId articleId) {
        final UserDto user = userService.findByChatId(chatId.value())
                .orElseThrow(() -> new RuntimeException("User with chat id " + chatId + " not found"));
        if (user.id() == null) {
            throw new RuntimeException("User id can't be null");
        }

        userArticlesService.likeUserArticle(
                new RequestUserArticleDto(articleId.value(), user.id().value(), Grade.LIKE)
        );
    }
}
