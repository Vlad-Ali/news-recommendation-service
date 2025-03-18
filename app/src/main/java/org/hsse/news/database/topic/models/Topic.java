package org.hsse.news.database.topic.models;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hsse.news.database.user.models.UserId;

import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "topics")
@Schema(name = "Topic", description = "Сущность темы")
@Getter
@Setter
@NoArgsConstructor
public class Topic {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Schema(description = "1")
    Long topicId;

    @Schema(description = "Название топика", example = "Новости")
    @NotNull
    String name;

    @Schema(description = "ID автора", example = "5014e384-d3de-4804-bb93-2502e02894c6")
    @NotNull
    UUID creatorId;

    public Topic(final @NotNull String name, final @NotNull UserId creator) {
        this.name = name;
        creatorId = creator.value();
    }

    public TopicDto toDto() {
        return new TopicDto(new TopicId(topicId), name, new UserId(creatorId));
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof Topic topic)) {
            return false;
        }

        return topicId != null && topicId.equals(topic.topicId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(topicId);
    }
}
