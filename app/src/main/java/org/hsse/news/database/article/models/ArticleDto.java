package org.hsse.news.database.article.models;

import java.sql.Timestamp;

public record ArticleDto(
    String title,
    String url,
    Timestamp createdAt,
    Long topicId,
    Long websiteId
) {

  public static ArticleDto fromArticle(final Article article) {
    return new ArticleDto(
        article.getTitle(),
        article.getUrl(),
        article.getCreatedAt(),
        article.getTopicId(),
        article.getWebsiteId()
    );
  }
}