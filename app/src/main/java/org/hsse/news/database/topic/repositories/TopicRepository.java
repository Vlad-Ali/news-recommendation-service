package org.hsse.news.database.topic.repositories;

import org.hsse.news.database.topic.models.Topic;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TopicRepository extends JpaRepository<Topic, Long> {
}
