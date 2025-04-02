package org.hsse.news.database.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.proxy.HibernateProxy;
import org.hsse.news.database.topic.models.TopicDto;
import org.hsse.news.dto.ArticleDto;
import org.hsse.news.dto.ResponseUserArticleDto;
import org.hsse.news.dto.ResponseUserDto;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "user_articles")
@Schema(name = "User articles", description = "Связь пользователя и статьи")
public class UserArticlesEntity {

    @NotNull()
    @EmbeddedId
    private Id id;

    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private UserEntity user;

    @MapsId("articleId")
    @JoinColumn(name = "article_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private ArticleEntity article;


    /**
     * Реакция пользователя на статью (лайк/дизлайк)
     */
    @NotNull()
    @Column(name = "grade")
    private Integer grade;

    public UserArticlesEntity() {}

    public UserArticlesEntity(final UserEntity userEntity,final ArticleEntity articleEntity,final Integer grade){
        this.user = userEntity;
        this.article = articleEntity;
        this.id = new Id(userEntity, articleEntity);
        this.grade = grade;
    }

    public @NotNull() Integer getGrade() {
        return grade;
    }

    public void setGrade(final @NotNull() Integer grade) {
        this.grade = grade;
    }

    @Embeddable
    @Getter
    @Setter
    public static class Id implements Serializable{

        @Column(name = "user_id")
        private UUID userId;

        @Column(name = "article_id")
        private UUID articleId;

        protected Id() {}

        public Id(final UserEntity userEntity,final ArticleEntity articleEntity){
            this.userId = userEntity.getId();
            this.articleId = articleEntity.getArticleId();
        }

        public UUID getArticleId() {
            return articleId;
        }

        public UUID getUserId() {
            return userId;
        }

        @Override
        public final boolean equals(final Object o) {
            if (this == o){
                return true;
            }
            if (o == null || getClass() != o.getClass()){
                return false;
            }
            final Id id = (Id) o;
            return Objects.equals(userId, id.userId) &&
                    Objects.equals(articleId, id.articleId);
        }

        @Override
        public final int hashCode() {
            return Objects.hash(userId, articleId);
        }
    }

    public void setId(final @NotNull() Id id) {
        this.id = id;
    }

    public static ResponseUserArticleDto toDto(final UserArticlesEntity userArticle) {
        return new ResponseUserArticleDto(
                ArticleDto.fromArticle(userArticle.article, TopicDto.getTopicDtoList(userArticle.article.getTopics().stream().toList())),
                ResponseUserDto.fromUserEntity(userArticle.user),
                userArticle.grade
        );
    }

    public @NotNull() Id getId() {
        return id;
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
        final UserArticlesEntity that = (UserArticlesEntity) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(id);
    }
}
