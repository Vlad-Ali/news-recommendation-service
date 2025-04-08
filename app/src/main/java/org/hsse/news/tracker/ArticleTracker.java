package org.hsse.news.tracker;

import org.hsse.news.bot.BotMapping;
import org.hsse.news.bot.ChatId;
import org.hsse.news.bot.Message;
import org.hsse.news.bot.NewsBotHandlers;
import org.hsse.news.bot.TelegramBot;
import org.hsse.news.database.article.ArticlesService;
import org.hsse.news.database.article.UserArticlesService;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.user.UserService;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
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
public class ArticleTracker {
    private final static String LIKE_COMMAND = "/like-scheduled";
    private final static String DISLIKE_COMMAND = "/dislike-scheduled";
    private final static String UNLIKE_COMMAND = "/unlike-scheduled";

    private final UserService userService;
    private final ArticlesService articlesService;
    private final UserArticlesService userArticlesService;
    private final NewsBotHandlers handlers; // TODO убрать этот костыль когда NewsBotHandlers отрефакторят

    private TelegramBot bot;

    public ArticleTracker(final UserService userService,
                          final ArticlesService articlesService,
                          final UserArticlesService userArticlesService,
                          final NewsBotHandlers handlers) {
        this.userService = userService;
        this.articlesService = articlesService;
        this.userArticlesService = userArticlesService;
        this.handlers = handlers;
    }

    @Autowired
    @Lazy
    public void setBot(final TelegramBot bot) {
        this.bot = bot;
    }

    private static List<InlineKeyboardButton> getArticleButtons() {
        return List.of(
                InlineKeyboardButton.builder()
                        .text("Поставить лайк")
                        .callbackData(LIKE_COMMAND)
                        .build(),
                InlineKeyboardButton.builder()
                        .text("Поставить дизлайк")
                        .callbackData(DISLIKE_COMMAND)
                        .build(),
                InlineKeyboardButton.builder()
                        .text("Убрать оценку")
                        .callbackData(UNLIKE_COMMAND)
                        .build()
        );
    }

    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
    public void sendArticles() {
        for (final ChatId chatId : bot.getActiveChats()) {
            final UserDto user = userService.findByChatId(chatId.value())
                    .orElseThrow(() -> new UserNotFoundException(
                            "User with chat id " + chatId.value() + " not found"));

            if (user.id() == null) {
                throw new RuntimeException();
            }

            for (final ArticleDto article : articlesService.getAllUnknown(user.id().value())) {
                final Integer likes = userArticlesService.getArticleLikeCount(article.articleId());
                final Integer dislikes = userArticlesService.getArticleDislikeCount(article.articleId());

                articlesService.addToKnown(user.id().value(), article.articleId());

                bot.sendArticleTo(chatId, (messageId) ->
                    Message.builder()
                            .text(handlers.getArticleMessage(article, likes, dislikes))
                            .verticalKeyboard(getArticleButtons())
                            .build()
                );
            }
        }
    }

    @BotMapping(LIKE_COMMAND)
    public void like(final ChatId chatId, final ArticleId articleId) {
        final UserDto user = userService.findByChatId(chatId.value())
                .orElseThrow(() -> new UserNotFoundException(
                        "User with chat id " + chatId.value() + " not found"));
        if (user.id() == null) {
            throw new IllegalStateException("User id can't be null");
        }

        userArticlesService.likeUserArticle(
                new RequestUserArticleDto(articleId.value(), user.id().value(), Grade.LIKE)
        );
    }

    @BotMapping(DISLIKE_COMMAND)
    public void dislike(final ChatId chatId, final ArticleId articleId) {
        final UserDto user = userService.findByChatId(chatId.value())
                .orElseThrow(() -> new UserNotFoundException(
                        "User with chat id " + chatId.value() + " not found"));
        if (user.id() == null) {
            throw new IllegalStateException("User id can't be null");
        }

        userArticlesService.dislikeUserArticle(
                new RequestUserArticleDto(articleId.value(), user.id().value(), Grade.DISLIKE)
        );
    }

    @BotMapping(UNLIKE_COMMAND)
    public void unlike(final ChatId chatId, final ArticleId articleId) {
        final UserDto user = userService.findByChatId(chatId.value())
                .orElseThrow(() -> new UserNotFoundException(
                        "User with chat id " + chatId.value() + " not found"));
        if (user.id() == null) {
            throw new IllegalStateException("User id can't be null");
        }

        userArticlesService.removeMarkFromUserArticle(
                new RequestUserArticleDto(articleId.value(), user.id().value(), Grade.NONE)
        );
    }
}
