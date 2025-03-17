package org.hsse.news.database.article.models;

import java.sql.Timestamp;

public record ArticleDto(
    String title,
    String url,
    Timestamp createdAt,
    Long topicId,
    Long websiteId
) {}