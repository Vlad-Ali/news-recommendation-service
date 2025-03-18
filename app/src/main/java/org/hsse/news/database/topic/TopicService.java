package org.hsse.news.database.topic;

import lombok.AllArgsConstructor;
import org.hsse.news.api.schemas.request.topic.CreateCustomTopicRequest;
import org.hsse.news.database.topic.exceptions.TopicNotFoundException;
import org.hsse.news.database.topic.models.Topic;
import org.hsse.news.database.topic.models.TopicDto;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.topic.repositories.TopicRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class TopicService {
    private final TopicRepository repository;

    @Transactional
    public List<TopicDto> getAll() {
        return repository.findAll().stream().map(Topic::toDto).toList();
    }

    @Transactional
    public Optional<TopicDto> getById(final TopicId id) {
        return repository.findById(id.value()).map(Topic::toDto);
    }

    @Transactional
    public void create(final CreateCustomTopicRequest data) {
        repository.save(new Topic(data.name(), data.creatorId().value()));
    }

    @Transactional
    public void update(final TopicId id, final CreateCustomTopicRequest data) {
        Topic topic = repository.findById(id.value()).orElseThrow(() -> new TopicNotFoundException(id));
        topic.setName(data.name());
        topic.setCreatorId(data.creatorId().value());
        repository.save(topic);
    }

    @Transactional
    public void delete(final TopicId id) {
        repository.deleteById(id.value());
    }
}
