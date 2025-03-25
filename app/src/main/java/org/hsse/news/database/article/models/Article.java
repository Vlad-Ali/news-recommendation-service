package org.hsse.news.database.article.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.sql.Timestamp;
import java.util.Objects;
import java.util.UUID;

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

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        final Class<?> oEffectiveClass = o instanceof HibernateProxy ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass() : o.getClass();
        final Class<?> thisEffectiveClass = this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass() : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) {
            return false;
        }
        final Article article = (Article) o;
        return getArticleId() != null && Objects.equals(getArticleId(), article.getArticleId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }
}
