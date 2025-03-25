package org.hsse.news.database.userarticles;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.hsse.news.database.article.models.Article;

import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@AllArgsConstructor
@Table(name = "user_articles",
uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "article_id"})})
@Schema(name = "User articles", description = "Связь пользователя и статьи")
public class UserArticle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull()
    private UUID userId;

    @NotNull()
    @JoinColumn(name = "article_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Article articleId;

    /**
     * Реакция пользователя на статью (лайк/дизлайк)
     */
    @NotNull()
    @Column(name = "grade", columnDefinition = "int default 0")
    private Integer grade;

    public UserArticle() {
        grade = 0;
    }

    public static UserArticleDto toDto(final UserArticle userArticle) {
        return new UserArticleDto(
                userArticle.getUserId(),
                userArticle.userId,
                userArticle.grade
        );
    }

    @Override
    public boolean equals(final Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final UserArticle that = (UserArticle) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
