package org.hsse.news.database.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.proxy.HibernateProxy;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "articles", uniqueConstraints = {@UniqueConstraint(columnNames = {"url"})})
@Schema(name = "Article", description = "Сущность статьи")
@Getter
@Setter
public class ArticleEntity implements Serializable {

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
    private Timestamp createdAt; // NOPMD

    @Schema(description = "topic_ids")
    @JoinTable(name = "article_topics", joinColumns = @JoinColumn(name = "article_id"), inverseJoinColumns = @JoinColumn(name = "topic_id"))
    @ManyToMany
    private Set<TopicEntity> topics = new HashSet<>(); // NOPMD

    @Schema(description = "website_id")
    @JoinColumn(name = "website_id")
    @ManyToOne
    private WebsiteEntity website; // NOPMD

    @OneToMany(mappedBy = "article", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserArticlesEntity> userArticles = new HashSet<>();

    public ArticleEntity() {}

    public ArticleEntity(final String title, final String url, final Timestamp createdAt, final WebsiteEntity websiteEntity) {
        this.title = title;
        this.url = url;
        this.createdAt = createdAt;
        this.website = websiteEntity;
    }

    public void assignArticle(final UserEntity userEntity,final Integer grade){
        userArticles.add(new UserArticlesEntity(userEntity, this, grade));
    }

    public void setCreatedAt(final @NotNull() Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public void setTitle(final @NotNull() String title) {
        this.title = title;
    }

    public void setUrl(final @NotNull() String url) {
        this.url = url;
    }

    public void addTopic(final TopicEntity topicEntity){
        topics.add(topicEntity);
        topicEntity.getArticles().add(this);
    }

    public void removeTopic(final TopicEntity topicEntity){
        topics.remove(topicEntity);
        topicEntity.getArticles().remove(this);
    }

    public Set<TopicEntity> getTopics() {return topics;}

    public void setWebsite(final WebsiteEntity website) {
        this.website = website;
    }

    public UUID getArticleId() {
        return articleId;
    }

    public WebsiteEntity getWebsite() {
        return website;
    }

    public @NotNull() Timestamp getCreatedAt() {
        return createdAt;
    }

    public String getUrl(){
        return url;
    }

    public String getTitle(){
        return title;
    }

    @Override
    public boolean equals(final Object o) {
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
        final ArticleEntity article = (ArticleEntity) o;
        return getArticleId() != null && Objects.equals(getArticleId(), article.getArticleId());
    }

    @Override
    public int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass().hashCode() : getClass().hashCode();
    }

    public Set<UserArticlesEntity> getUserArticles() {
        return userArticles;
    }
}