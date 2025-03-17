package org.hsse.news.database.article.models;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

import io.swagger.v3.oas.annotations.media.Schema;

@Entity
@Table(name = "articles")
@Schema(name = "Article", description = "Сущность статьи")
@Getter
@Setter
@NoArgsConstructor
public class Article {

    @Id
    @GeneratedValue
    @Schema(description = "ID", example = "1")
    private UUID articleId; // NOPMD

    @Schema(description = "title", example = "test-title")
    @NotNull()
    private String title; // NOPMD

    @Schema(description = "url", example = "https://test.ru")
    @NotNull()
    private String url; // NOPMD

    @Schema(description = "created_at")
    @NotNull()
    private Timestamp createdAt;

    @Schema(description = "topic_id", example = "1")
    @NotNull()
    private Long topicId; // NOPMD

    @Schema(description = "website_id", example = "1")
    @NotNull()
    private Long websiteId; // NOPMD

    public Article(final String title, final String url, final Long topicId, final Long websiteId) {
        this.title = title;
        this.url = url;
        this.topicId = topicId;
        this.websiteId = websiteId;
    }

    public static ArticleDto toDto(final Article article) {
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
