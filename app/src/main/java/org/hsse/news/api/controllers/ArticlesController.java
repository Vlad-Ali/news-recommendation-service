package org.hsse.news.api.controllers;

import lombok.extern.slf4j.Slf4j;
import org.hsse.news.api.operations.ArticleOperations;
import org.hsse.news.database.article.ArticlesService;
import org.hsse.news.database.article.models.*;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.userarticles.UserArticleDto;
import org.hsse.news.database.website.models.WebsiteId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/articles")
@Tag(name = "Article API", description = "Управление статьями")
public class ArticlesController implements ArticleOperations {
  private final ArticlesService articleService;

  public ArticlesController(final ArticlesService articleService) {
    this.articleService = articleService;
  }

  @Override
  public ResponseEntity<List<ArticleDto>> getAll() {
    final List<ArticleDto> articles = articleService.getAll();
    return new ResponseEntity<>(articles, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<ArticleDto> getArticle(final UUID articleId) {
    final ArticleDto article = articleService.findById(new ArticleId(articleId));
    return ResponseEntity.ok(article);
  }

  @Override
  public ResponseEntity<List<UserArticleDto>> getUserArticles(final UUID userId) {
    final List<UserArticleDto> articles = articleService.getUserArticles(userId);
    return new  ResponseEntity<>(articles, HttpStatus.OK);
  }

  @Override
  public ResponseEntity<String> createArticle(final ArticleDto articleData) {
    articleService.create(articleData);
    return new ResponseEntity<>("Article created", HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<String> updateArticle(final UUID articleId, final ArticleDto articleDto) {
    articleService.update(
        new ArticleId(articleId),
            articleDto.title(),
            articleDto.url(),
            articleDto.createdAt(),
            articleDto.topicId(),
            articleDto.websiteId()
    );

    return new ResponseEntity<>(articleId.toString(), HttpStatus.OK);
  }

  @Override
  public ResponseEntity<String> deleteArticle(final UUID articleId) {
    articleService.delete(new ArticleId(articleId));
    return new ResponseEntity<>(articleId.toString(), HttpStatus.NO_CONTENT);
  }
}
