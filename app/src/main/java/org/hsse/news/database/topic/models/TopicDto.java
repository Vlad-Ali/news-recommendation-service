package org.hsse.news.database.topic.models;

import org.hsse.news.database.entity.TopicEntity;
import org.hsse.news.database.entity.UserEntity;
import org.hsse.news.database.user.exceptions.UserInitializationException;
import org.hsse.news.database.user.models.UserId;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record TopicDto(
        @Nullable TopicId id, @NotNull String description, @Nullable UserId creatorId
        ) {

    public TopicDto(final @NotNull String description, final @Nullable UserId creatorId){
        this(null, description, creatorId);
    }

    public TopicDto initializeWithId(final @NotNull TopicId newId) {
        if (id != null) {
            throw new UserInitializationException("Topic is already initialized");
        }

        return new TopicDto(newId, description, creatorId);
    }

    public TopicDto withCreators(final @NotNull  UserId creatorId){
        return new TopicDto(id, description, creatorId);
    }

    public TopicDto withDescription(final @NotNull String newDescription) {
        return new TopicDto(id, newDescription, creatorId);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof TopicDto topic)) {
            return false;
        }

        return id != null && id.equals(topic.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public TopicEntity toTopicEntity(final UserEntity user){
        final String name = this.description();
        return new TopicEntity(name, user);
    }

    public static List<TopicDto> getTopicDtoList(final List<TopicEntity> topicEntities){
        final List<TopicDto> topicDtoList = new ArrayList<>();
        for (final TopicEntity topicEntity : topicEntities){
            topicDtoList.add(new TopicDto(new TopicId(topicEntity.getTopicId()), topicEntity.getName(), new UserId(topicEntity.getCreatorId())));
        }
        return topicDtoList;
    }
}
