package org.hsse.news.article;

import org.hsse.news.Application;
import org.hsse.news.database.article.models.Article;
import org.hsse.news.database.article.repositories.ArticleRepository;
import org.hsse.news.dbsuite.DbSuite;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ContextConfiguration(classes = Application.class)
class JpaArticleRepositoryTest extends DbSuite {

  @Autowired
  private ArticleRepository articleRepository;

  private static Article ARTICLE_1 = new Article("test", "test", 1L, 1L); // NOPMD
  private static final Article ARTICLE_2 = new Article("test2", "test2", 2L, 2L);

  @BeforeEach
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void setup() {
    ARTICLE_1 = articleRepository.save(ARTICLE_1);
  }

  @Test
  void shouldCreateArticle() {
    Assertions.assertFalse(articleRepository.findAll().isEmpty());
  }

  @Test
  void shouldFindArticle() {
    final Optional<Article> article = articleRepository.findById(ARTICLE_1.getArticleId());

    Assertions.assertTrue(article.isPresent());
    Assertions.assertEquals(ARTICLE_1.getTitle(), article.get().getTitle());
    Assertions.assertEquals(ARTICLE_1.getUrl(), article.get().getUrl());
    Assertions.assertEquals(ARTICLE_1.getCreatedAt(), article.get().getCreatedAt());
    Assertions.assertEquals(ARTICLE_1.getTopicId(), article.get().getTopicId());
    Assertions.assertEquals(ARTICLE_1.getWebsiteId(), article.get().getWebsiteId());
  }

  @Test
  void shouldGetAllArticles() {
    articleRepository.save(ARTICLE_2);
    final List<Article> users = articleRepository.findAll();
    Assertions.assertEquals(2, users.size());
  }

  @Test
  void shouldDeleteArticle() {
    Assertions.assertTrue(articleRepository.findById(ARTICLE_1.getArticleId()).isPresent());
    articleRepository.deleteById(ARTICLE_1.getArticleId());
    Assertions.assertFalse(articleRepository.findById(ARTICLE_1.getArticleId()).isPresent());
  }


}