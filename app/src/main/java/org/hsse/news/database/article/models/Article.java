package org.hsse.news.database.article.models;

import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.website.models.WebsiteId;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

import java.sql.Timestamp;
import java.util.Objects;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.NonNull;
import lombok.Data;

@Schema(name = "Article", description = "Сущность статьи")
@Data
@Builder(toBuilder = true)
public class Article {
    @Schema(description = "ID", example = "1")
    @Nested
    private ArticleId id;

    @Schema(description = "title", example = "test-title")
    @NonNull
    private final String title;

    @Schema(description = "url", example = "https://test.ru")
    @NonNull
    private final String url;

    @Schema(description = "created_at")
    @NonNull
    private final Timestamp createdAt;

    @Schema(description = "topic_id", example = "1")
    private TopicId topicId;

    @Schema(description = "website_id", example = "1")
    private WebsiteId websiteId;

    @JdbiConstructor
    public Article(ArticleId id,
                   @NonNull String title,
                   @NonNull String url,
                   @NonNull Timestamp createdAt,
                   TopicId topicId,
                   WebsiteId websiteId
    ) {
      this.id = id;
      this.title = title;
      this.url = url;
      this.createdAt = createdAt;
      this.topicId = topicId;
      this.websiteId = websiteId;
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Article article = (Article) o;
        return id != null && id.equals(article.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
