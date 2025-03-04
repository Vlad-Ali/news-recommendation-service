package org.hsse.news.database.article.models;

import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.sql.Timestamp;

public record ArticleData(
    String title,
    String url,
    Timestamp createdAt,
    Long topicId,
    Long websiteId
) {
  @JdbiConstructor
  public ArticleData {}
}