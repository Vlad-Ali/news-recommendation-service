package org.hsse.news.bot;

import lombok.extern.slf4j.Slf4j;
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
  private final static String ARTICLES_MENU_COMMAND = "/articles";
  private final static String VIEW_UNWATCHED_ARTICLES_COMMAND = "/view-unwatched-articles";
  private final static String VIEW_WATCHED_ARTICLES_COMMAND = "/view-watched-articles";

  private final static String LIKE_COMMAND = "/like";
  private final static String REMOVE_GRADE_COMMAND = "/unlike";
  private final static String DISLIKE_COMMAND = "/dislike";

  private final static String BACK_TEXT = "Назад";
  private static final String INCREASE_VIEW_WATCHED_ARTICLES_COMMAND = "/increase-view-watched-articles";
  private static final String DECREASE_VIEW_WATCHED_ARTICLES_COMMAND = "/decrease-view-watched-articles";

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

  @BotMapping(VIEW_UNWATCHED_ARTICLES_COMMAND)
  public Message viewUnwatchedArticles(final ChatId chatId) {
    UserDto user = userService.findByChatId(chatId.value())
        .orElseThrow(() -> new UserNotFoundException("User with chat id " + chatId.value() + " not found"));

    if (user.id() == null) {
      throw new RuntimeException();
    }

    List<ArticleDto> articles = tempUserStates.get(chatId).getUnknownArticles();;

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

    ArticleDto article = articles.remove(0);
    Integer likes = userArticlesService.getArticleLikeCount(article.articleId());
    Integer dislikes = userArticlesService.getArticleDislikeCount(article.articleId());
    tempUserStates.get(chatId).setArticle(article);

    articlesService.addToKnown(user.id().value(), article.articleId());

    return Message.builder()
        .text(getArticleMessage(article, likes, dislikes))
        .verticalKeyboard(getArticleButtons(articles.size()))
        .build();
  }

  @BotMapping(VIEW_WATCHED_ARTICLES_COMMAND)
  public Message viewWatchedArticles(final ChatId chatId) {
    List<ResponseUserArticleDto> userArticles = tempUserStates.get(chatId).getKnownArticles();
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

    List<ArticleDto> articles = userArticles.stream().map(ResponseUserArticleDto::article).toList();

    if (userIndex == null) {
      tempUserStates.get(chatId).setCurrentIndex(0);
      userIndex = 0;
    }

    ArticleDto article = articles.get(userIndex);
    tempUserStates.get(chatId).setArticle(article);

    Integer likes = userArticlesService.getArticleLikeCount(article.articleId());
    Integer dislikes = userArticlesService.getArticleDislikeCount(article.articleId());

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
    UserDto user = userService.findByChatId(chatId.value())
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

  private List<InlineKeyboardButton> getArticleButtons(int size) {
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

  private List<InlineKeyboardButton> getArticleButtons(int size, int index, ArticleDto articleDto, UserDto userDto) {
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

  private static InlineKeyboardMarkup articleMenuKeyboard(int knownCount, int unknownCount) {
    log.debug("Article menu method called");

    return new InlineKeyboardMarkup(List.of(
        List.of(InlineKeyboardButton.builder()
            .text(java.lang.String.format("Просмотренные статьи (%d)", knownCount))
            .callbackData(VIEW_WATCHED_ARTICLES_COMMAND).build()),
        List.of(InlineKeyboardButton.builder()
            .text(java.lang.String.format("Непросмотренные статьи (%d)", unknownCount))
            .callbackData(VIEW_UNWATCHED_ARTICLES_COMMAND).build()),
        List.of(InlineKeyboardButton.builder()
            .text(BACK_TEXT)
            .callbackData(MENU_COMMAND).build())
    ));
  }

  public String getArticleMessage(ArticleDto article, Integer likes, Integer dislikes) {
    String articleTitle = article.title().toUpperCase(Locale.ROOT) + "\n\n";
    List<String> topics = article.topics().stream().map(TopicDto::description).toList();
    String articleUrl = article.url();
    String grades = "Лайки \uD83D\uDC4D:    %d\nДизы \uD83D\uDCA9:   %d".formatted(likes, dislikes);
    return articleTitle + String.join("   ", topics) + "\n\n" + articleUrl + "\n\n" + grades;
  }

  @BotMapping(LIKE_COMMAND)
  public void like(final ChatId chatId) {
    UUID userId = tempUserStates.get(chatId).getUser().id().value();
    UUID articleId = tempUserStates.get(chatId).getArticle().articleId();
    userArticlesService.likeUserArticle(
        new RequestUserArticleDto(articleId, userId, Grade.LIKE)
    );
  }

  @BotMapping(DISLIKE_COMMAND)
  public void dislike(final ChatId chatId) {
    UUID userId = tempUserStates.get(chatId).getUser().id().value();
    UUID articleId = tempUserStates.get(chatId).getArticle().articleId();
    userArticlesService.dislikeUserArticle(
        new RequestUserArticleDto(articleId, userId, Grade.DISLIKE)
    );
  }

  @BotMapping(REMOVE_GRADE_COMMAND)
  public void removeGrade(final ChatId chatId) {
    UUID userId = tempUserStates.get(chatId).getUser().id().value();
    UUID articleId = tempUserStates.get(chatId).getArticle().articleId();
    userArticlesService.removeMarkFromUserArticle(
        new RequestUserArticleDto(articleId, userId, Grade.NONE)
    );
  }
}
