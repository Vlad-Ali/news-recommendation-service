package org.hsse.news.database.topic.repositories;

import org.hsse.news.database.topic.models.TopicDto;
import org.hsse.news.database.topic.models.TopicId;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public interface TopicRepository {
    Optional<TopicDto> findById(@NotNull TopicId topicId);

    @NotNull TopicDto create(@NotNull TopicDto topic);

    @NotNull List<TopicDto> getAll();

    void delete(@NotNull TopicId topicId);
}
