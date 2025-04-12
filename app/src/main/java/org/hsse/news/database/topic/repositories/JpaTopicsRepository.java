package org.hsse.news.database.topic.repositories;

import org.hsse.news.database.entity.TopicEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaTopicsRepository extends JpaRepository<TopicEntity, Long> {
    @Query(value = "SELECT topics.topic_id, name, topics.creator_id FROM topics INNER JOIN user_topics ON topics.topic_id = user_topics.topic_id\n"
            + "WHERE user_topics.user_id = :user_id", nativeQuery = true)
    List<TopicEntity> findSubscribedTopicsByUserId(@Param("user_id") UUID userId);

    @Query(value = "SELECT t.*\n"
            + "FROM topics t\n"
            + "LEFT JOIN user_topics ut ON t.topic_id = ut.topic_id AND ut.user_id = :user_id\n"
            + "WHERE ut.topic_id IS NULL", nativeQuery = true)
    List<TopicEntity> findUnSubscribedTopicsByUserId(@Param("user_id") UUID userId);
    Optional<TopicEntity> findByName(String name);
}
