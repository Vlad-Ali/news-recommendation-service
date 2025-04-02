package org.hsse.news.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hsse.news.database.entity.TopicEntity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
public class ResponseTopicDto {

    private Long id;
    private @NotNull String description;
    private UUID creatorId;

    public ResponseTopicDto(final Long id, final @NotNull String description,final UUID creatorId){
        this.id = id;
        this.description = description;
        this.creatorId = creatorId;
    }

    public static ResponseTopicDto fromTopic(final TopicEntity topic) {
        return new ResponseTopicDto(
                topic.getTopicId(),
                topic.getName(),
                topic.getCreatorId()
        );
    }

    public @NotNull String getDescription() {
        return description;
    }

    public Long getId() {
        return id;
    }

    public UUID getCreatorId() {
        return creatorId;
    }
}
