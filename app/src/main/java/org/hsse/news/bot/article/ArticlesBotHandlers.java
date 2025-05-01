package org.hsse.news.bot.article;

import lombok.extern.slf4j.Slf4j;
import org.hsse.news.bot.BotMapping;
import org.hsse.news.bot.ChatId;
import org.hsse.news.bot.Message;
import org.hsse.news.bot.UserState;
import org.hsse.news.database.article.ArticlesService;
import org.hsse.news.database.article.UserArticlesService;
import org.hsse.news.database.topic.TopicService;
import org.hsse.news.database.topic.models.TopicDto;
import org.hsse.news.database.user.UserService;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
import org.hsse.news.database.user.models.UserDto;
import org.hsse.news.database.website.WebsiteService;
import org.hsse.news.dto.ArticleDto;
import org.hsse.news.dto.RequestUserArticleDto;
import org.hsse.news.dto.ResponseUserArticleDto;
import org.hsse.news.util.Grade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ArticlesBotHandlers {

    private final static String MENU_COMMAND = "/menu";
    private final static String ARTICLES_INFO = "/articles-info";
    private final static String ARTICLES_MENU_COMMAND = "/articles";
    private final static String VIEW_UNWATCHED_ARTICLES_COMMAND = "/view-unwatched-articles";
    private final static String VIEW_WATCHED_ARTICLES_COMMAND = "/view-watched-articles";

    private final static String LIKE_COMMAND = "/like";
    private final static String REMOVE_GRADE_COMMAND = "/unlike";
    private final static String DISLIKE_COMMAND = "/dislike";

    private final static String BACK_TEXT = "Назад";
    private static final String INCREASE_VIEW_WATCHED_ARTICLES_COMMAND = "/increase-view-watched-articles";
    private static final String DECREASE_VIEW_WATCHED_ARTICLES_COMMAND = "/decrease-view-watched-articles";

    private final String articlesInfo = "📚 Твоя лента статей\n\n" +
            "Здесь ты можешь:\n\n" +
            "✨ Читать новые материалы из подписок\n" +
            "📖 Возвращаться к прочитанным статьям\n" +
            "👍 Отмечать понравившиеся публикации\n" +
            "👎 Оценивать нерелевантный контент\n\n" +
            "Каждую пятницу мы присылаем:\n" +
            "🏆 Подборку самых популярных статей недели\n\n";

    @Autowired
    private ArticlesService articlesService;

    @Autowired
    private UserService userService;

    @Autowired
    private WebsiteService websiteService;

    @Autowired
    private TopicService topicService;

    @Autowired
    private UserArticlesService userArticlesService;

    private final ConcurrentHashMap<ChatId, UserState> tempUserStates = new ConcurrentHashMap<>();

    @BotMapping(ARTICLES_INFO)
    public Message sendArticleInfo(final ChatId chatId){
        return Message.builder().text(articlesInfo).keyboard(articlesMenu(chatId).keyboard()).build();
    }

    @BotMapping(VIEW_UNWATCHED_ARTICLES_COMMAND)
    public Message viewUnwatchedArticles(final ChatId chatId) {
        final UserDto user = userService.findByChatId(chatId.value())
                .orElseThrow(() -> new UserNotFoundException("User with chat id " + chatId.value() + " not found"));

        if (user.id() == null) {
            throw new RuntimeException();
        }

        final List<ArticleDto> articles = tempUserStates.get(chatId).getUnknownArticles();

        if (articles.isEmpty()) {
            return Message.builder()
                    .text("У вас нет новых статей")
                    .verticalKeyboard(
                            List.of(
                                    InlineKeyboardButton.builder()
                                            .text(BACK_TEXT)
                                            .callbackData(ARTICLES_MENU_COMMAND)
                                            .build()
                            ))
                    .build();
        }

        final ArticleDto article = articles.remove(0);
        final Integer likes = userArticlesService.getArticleLikeCount(article.articleId());
        final Integer dislikes = userArticlesService.getArticleDislikeCount(article.articleId());
        tempUserStates.get(chatId).setArticle(article);

        articlesService.addToKnown(user.id().value(), article.articleId());

        return Message.builder()
                .text(getArticleMessage(article, likes, dislikes))
                .verticalKeyboard(getArticleButtons(articles.size()))
                .build();
    }

    @BotMapping(VIEW_WATCHED_ARTICLES_COMMAND)
    public Message viewWatchedArticles(final ChatId chatId) {
        final List<ResponseUserArticleDto> userArticles = tempUserStates.get(chatId).getKnownArticles();
        Integer userIndex = tempUserStates.get(chatId).getCurrentIndex();

        if (userArticles.isEmpty()) {
            return Message.builder()
                    .text("У вас нет статей")
                    .verticalKeyboard(
                            List.of(
                                    InlineKeyboardButton.builder()
                                            .text(BACK_TEXT)
                                            .callbackData(ARTICLES_MENU_COMMAND)
                                            .build()
                            ))
                    .build();
        }

        final List<ArticleDto> articles = userArticles.stream().map(ResponseUserArticleDto::article).toList();

        if (userIndex == null) {
            tempUserStates.get(chatId).setCurrentIndex(0);
            userIndex = 0;
        }

        final ArticleDto article = articles.get(userIndex);
        tempUserStates.get(chatId).setArticle(article);

        final Integer likes = userArticlesService.getArticleLikeCount(article.articleId());
        final Integer dislikes = userArticlesService.getArticleDislikeCount(article.articleId());

        return Message.builder()
                .text(getArticleMessage(article, likes, dislikes))
                .verticalKeyboard(getArticleButtons(articles.size(), userIndex, article, tempUserStates.get(chatId).getUser()))
                .build();
    }

    @BotMapping(INCREASE_VIEW_WATCHED_ARTICLES_COMMAND)
    public Message increaseViewWatchedArticles(final ChatId chatId) {
        tempUserStates.get(chatId).setCurrentIndex(tempUserStates.get(chatId).getCurrentIndex() + 1);
        return viewWatchedArticles(chatId);
    }

    @BotMapping(DECREASE_VIEW_WATCHED_ARTICLES_COMMAND)
    public Message decreaseViewWatchedArticles(final ChatId chatId) {
        tempUserStates.get(chatId).setCurrentIndex(tempUserStates.get(chatId).getCurrentIndex() - 1);
        return viewWatchedArticles(chatId);
    }

    @BotMapping(ARTICLES_MENU_COMMAND)
    public Message articlesMenu(final ChatId chatId) {
        final UserDto user = userService.findByChatId(chatId.value())
                .orElseThrow(() -> new UserNotFoundException(String.valueOf(chatId.value())));

        if (user.id() == null) {
            throw new RuntimeException("UserId is null");
        }

        if (!tempUserStates.containsKey(chatId)) {
            tempUserStates.put(chatId, new UserState());
        }

        tempUserStates.get(chatId).setKnownArticles(articlesService.getUserArticles(user.id().value()));
        tempUserStates.get(chatId).setUnknownArticles(articlesService.getAllUnknown(user.id().value()));
        tempUserStates.get(chatId).setUser(user);

        return Message.builder().text("Статьи").keyboard(
                articleMenuKeyboard(
                        tempUserStates.get(chatId).getKnownArticles().size(),
                        tempUserStates.get(chatId).getUnknownArticles().size()
                )
        ).build();
    }

    private List<InlineKeyboardButton> getArticleButtons(final int size) {
        final List<InlineKeyboardButton> buttons = new ArrayList<>(List.of(
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
                        .callbackData(REMOVE_GRADE_COMMAND)
                        .build()
        ));

        if (size > 0) {
            buttons.add(
                    InlineKeyboardButton.builder()
                            .text("Далее (%d)".formatted(size))
                            .callbackData(VIEW_UNWATCHED_ARTICLES_COMMAND)
                            .build()
            );
        }

        buttons.add(
                InlineKeyboardButton.builder()
                        .text(BACK_TEXT)
                        .callbackData(ARTICLES_MENU_COMMAND)
                        .build()
        );

        return buttons;
    }

    private List<InlineKeyboardButton> getArticleButtons(final int size,final int index,final ArticleDto articleDto,final UserDto userDto) {
        final List<InlineKeyboardButton> buttons = new ArrayList<>(List.of(
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
                        .callbackData(REMOVE_GRADE_COMMAND)
                        .build()
        ));

        if (size - 1 > index) {
            buttons.add(
                    InlineKeyboardButton.builder()
                            .text("Следующая (%d)".formatted(size - index - 1))
                            .callbackData(INCREASE_VIEW_WATCHED_ARTICLES_COMMAND)
                            .build()
            );
        }

        if (index > 0) {
            buttons.add(
                    InlineKeyboardButton.builder()
                            .text("Предыдущая (%d)".formatted(index))
                            .callbackData(DECREASE_VIEW_WATCHED_ARTICLES_COMMAND)
                            .build()
            );
        }

        buttons.add(
                InlineKeyboardButton.builder()
                        .text(BACK_TEXT)
                        .callbackData(ARTICLES_MENU_COMMAND)
                        .build()
        );

        return buttons;
    }

    private static InlineKeyboardMarkup articleMenuKeyboard(final int knownCount,final int unknownCount) {
        log.debug("Article menu method called");

        return new InlineKeyboardMarkup(List.of(
                List.of(InlineKeyboardButton.builder()
                        .text(String.format("Просмотренные статьи (%d)", knownCount))
                        .callbackData(VIEW_WATCHED_ARTICLES_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text(String.format("Непросмотренные статьи (%d)", unknownCount))
                        .callbackData(VIEW_UNWATCHED_ARTICLES_COMMAND).build()),
                List.of(InlineKeyboardButton.builder()
                        .text("Информация")
                        .callbackData(ARTICLES_INFO).build()),
                List.of(InlineKeyboardButton.builder()
                        .text(BACK_TEXT)
                        .callbackData(MENU_COMMAND).build())
        ));
    }

    private String getArticleMessage(final ArticleDto article,final Integer likes,final Integer dislikes) {
        final String articleTitle = article.title().toUpperCase(Locale.ROOT) + "\n\n";
        final List<String> topics = article.topics().stream().map(TopicDto::description).toList();
        final String articleUrl = article.url();
        final String grades = "Лайки \uD83D\uDC4D:    %d\nДизы \uD83D\uDCA9:   %d".formatted(likes, dislikes);
        return articleTitle + String.join("   ", topics) + "\n\n" + articleUrl + "\n\n" + grades;
    }

    @BotMapping(LIKE_COMMAND)
    public void like(final ChatId chatId) {
        final UUID userId = tempUserStates.get(chatId).getUser().id().value();
        final UUID articleId = tempUserStates.get(chatId).getArticle().articleId();
        userArticlesService.likeUserArticle(
                new RequestUserArticleDto(articleId, userId, Grade.LIKE)
        );
    }

    @BotMapping(DISLIKE_COMMAND)
    public void dislike(final ChatId chatId) {
        final UUID userId = tempUserStates.get(chatId).getUser().id().value();
        final UUID articleId = tempUserStates.get(chatId).getArticle().articleId();
        userArticlesService.dislikeUserArticle(
                new RequestUserArticleDto(articleId, userId, Grade.DISLIKE)
        );
    }

    @BotMapping(REMOVE_GRADE_COMMAND)
    public void removeGrade(final ChatId chatId) {
        final UUID userId = tempUserStates.get(chatId).getUser().id().value();
        final UUID articleId = tempUserStates.get(chatId).getArticle().articleId();
        userArticlesService.removeMarkFromUserArticle(
                new RequestUserArticleDto(articleId, userId, Grade.NONE)
        );
    }
}
