package org.hsse.news.database.entity;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.hsse.news.database.topic.models.TopicDto;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.user.models.UserId;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Getter
@Table(name = "topics", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
public class TopicEntity {
    @Id
    @Column(name = "topic_id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "1")
    private Long topicId;

    @Column(name = "name")
    @NotNull
    @Schema(description = "Название топика", example = "Новости")
    private String name;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "creator_id", nullable = true)
    @Schema(description = "Сущность автора")
    private UserEntity creator;

    @ManyToMany(mappedBy = "subscribedTopics")
    private Set<UserEntity> subscribers = new HashSet<>();

    @ManyToMany(mappedBy = "topics")
    private Set<ArticleEntity> articles = new HashSet<>();

    protected TopicEntity(){}

    public TopicEntity(final String name, final UserEntity creator){
        this.creator = creator;
        this.name = name;
    }


    public @NotNull String getName() {
        return name;
    }

    public void setName(final @NotNull String name) {
        this.name = name;
    }

    public Long getTopicId() {
        return topicId;
    }

    public UUID getCreatorId(){
        if (creator == null) {
            return null;
        }
        return creator.getId();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TopicEntity topic)) {
            return false;
        }
        return topicId!=null && topicId.equals(topic.topicId);
    }

    @Override
    public int hashCode(){
        return TopicEntity.class.hashCode();
    }

    public UserEntity getCreator() {
        return creator;
    }

    public void setCreator(final UserEntity creator) {
        this.creator = creator;
    }

    public TopicDto toTopicDto(){
        final Long topicId = this.getTopicId();
        final String name = this.getName();
        final UUID creatorId = this.getCreatorId();
        return new TopicDto(new TopicId(topicId), name, new UserId(creatorId));
    }

    public Set<UserEntity> getSubscribers() {
        return subscribers;
    }

    public Set<ArticleEntity> getArticles() {
        return articles;
    }
}
