package org.hsse.news.topic;

import org.hsse.news.Application;
import org.hsse.news.database.topic.models.Topic;
import org.hsse.news.database.topic.repositories.TopicRepository;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.dbsuite.DbSuite;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@DataJpaTest
@Transactional(propagation = Propagation.NOT_SUPPORTED)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
@ContextConfiguration(classes = Application.class)
class JpaTopicRepositoryTest extends DbSuite {

  @Autowired
  private TopicRepository topicRepository;

  private Topic TOPIC_1 = new Topic("test", new UserId(UUID.randomUUID()));
  private static final Topic TOPIC_2 = new Topic("test2", new UserId(UUID.randomUUID()));

  @BeforeEach
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void setup() {
    topicRepository.deleteAll();
    TOPIC_1 = topicRepository.save(TOPIC_1);
  }

  @Test
  void shouldCreateTopic() {
    Assertions.assertFalse(topicRepository.findAll().isEmpty());
  }

  @Test
  void shouldFindTopic() {
    final Optional<Topic> topic = topicRepository.findById(TOPIC_1.getTopicId());

    Assertions.assertTrue(topic.isPresent());
    Assertions.assertEquals(TOPIC_1.getName(), topic.get().getName());
    Assertions.assertEquals(TOPIC_1.getCreatorId(), topic.get().getCreatorId());
  }

  @Test
  void shouldNotFindTopic() {
    final Optional<Topic> topic = topicRepository.findById(TOPIC_1.getTopicId() + 100);
    Assertions.assertFalse(topic.isPresent());
  }

  @Test
  void shouldGetAllArticles() {
    topicRepository.save(TOPIC_2);
    final List<Topic> topics = topicRepository.findAll();
    Assertions.assertEquals(2, topics.size());
  }

  @Test
  void shouldDeleteArticle() {
    Assertions.assertTrue(topicRepository.findById(TOPIC_1.getTopicId()).isPresent());
    topicRepository.deleteById(TOPIC_1.getTopicId());
    Assertions.assertFalse(topicRepository.findById(TOPIC_1.getTopicId()).isPresent());
  }
}
