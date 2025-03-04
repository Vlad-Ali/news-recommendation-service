package org.hsse.news.api.controllers;

import org.hsse.news.api.operations.ArticleOperations;
import org.hsse.news.database.article.ArticlesService;
import org.hsse.news.database.article.models.*;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.website.models.WebsiteId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.UUID;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/articles")
@Tag(name = "Article API", description = "Управление статьями")
public final class ArticlesController implements ArticleOperations {
  private static final Logger LOG = LoggerFactory.getLogger(ArticlesController.class);

  private final ArticlesService articleService;

  public ArticlesController(final ArticlesService articleService) {
    this.articleService = articleService;
  }

  @Override
  public ResponseEntity<ArticleData> getArticle(final UUID articleId) {
    final ArticleData article = articleService.findById(new ArticleId(articleId));
    return ResponseEntity.ok(article);
  }

  @Override
  public ResponseEntity<ArticleListData> getUserArticles(final UUID userId) {
    return null;
  }

  @Override
  public ResponseEntity<String> createArticle(final ArticleData articleData) {
    final Article article = articleService.create(
        Article.builder()
            .title(articleData.title())
            .url(articleData.url())
            .createdAt(articleData.createdAt())
            .websiteId(new WebsiteId(articleData.websiteId()))
            .topicId(new TopicId(articleData.topicId()))
            .build()
    );

    LOG.debug("Article`s title with id={} was updated", article.getId().value());
    return new ResponseEntity<>(article.getId().value().toString(), HttpStatus.CREATED);
  }

  @Override
  public ResponseEntity<String> updateArticle(final UUID articleId, final ArticleData articleData) {
    articleService.update(
        new ArticleId(articleId),
        articleData.title(),
        articleData.url(),
        articleData.createdAt(),
        new TopicId(articleData.topicId()),
        new  WebsiteId(articleData.websiteId())
    );

    return new ResponseEntity<>(articleId.toString(), HttpStatus.OK);
  }
}
