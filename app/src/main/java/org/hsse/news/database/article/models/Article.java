package org.hsse.news.database.article.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hsse.news.database.topic.models.Topic;
import org.hsse.news.database.website.models.Website;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "articles")
@Schema(name = "Article", description = "Сущность статьи")
@Getter
@Setter
public class Article {

    @Id
    @GeneratedValue
    @Schema(description = "ID", example = "1")
    private UUID articleId;

    @Schema(description = "title", example = "test-title")
    @NotNull()
    private String title;

    @Schema(description = "url", example = "https://test.ru")
    @NotNull()
    private String url;

    @Schema(description = "created_at")
    @NotNull()
    private Timestamp createdAt;

    @Schema(description = "topic_id", example = "1")
    @NotNull()
    private Long topicId;

    @Schema(description = "website_id", example = "1")
    @NotNull()
    private Long websiteId;

    public Article() {
        this.articleId = UUID.randomUUID();
    }

    public  Article(@NonNull String title, @NonNull String url, @NonNull Long topicId, @NonNull Long websiteId) {
        this();
        this.title = title;
        this.url = url;
        this.topicId = topicId;
        this.websiteId = websiteId;
    }

    public static ArticleDto toDto(Article article) {
        return new ArticleDto(
                article.getTitle(),
                article.getUrl(),
                article.getCreatedAt(),
                article.getTopicId(),
                article.getWebsiteId()
        );
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Article article = (Article) o;
        return articleId != null && articleId.equals(article.articleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(articleId);
    }
}
