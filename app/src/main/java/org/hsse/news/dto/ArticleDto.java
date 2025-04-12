package org.hsse.news.dto;

import org.hsse.news.database.entity.ArticleEntity;
import org.hsse.news.database.topic.models.TopicDto;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

public record ArticleDto(
        UUID articleId,
        String title,
        String url,
        Timestamp createdAt,
        List<TopicDto> topics,
        ResponseWebsiteDto website
) {

    public static ArticleDto fromArticle(final ArticleEntity article, final List<TopicDto> topics) {
        return new ArticleDto(
                article.getArticleId(),
                article.getTitle(),
                article.getUrl(),
                article.getCreatedAt(),
                topics,
                ResponseWebsiteDto.fromWebsite(article.getWebsite())
        );
    }
}
