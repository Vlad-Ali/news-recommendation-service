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

  private Topic topic1 = new Topic("test", UUID.randomUUID());
  private final Topic topic2 = new Topic("test2", UUID.randomUUID());

  @BeforeEach
  @Transactional(propagation = Propagation.NOT_SUPPORTED)
  public void setup() {
    topicRepository.deleteAll();
    topic1 = topicRepository.save(topic1);
  }

  @Test
  void shouldCreateTopic() {
    Assertions.assertFalse(topicRepository.findAll().isEmpty());
  }

  @Test
  void shouldFindTopic() {
    final Optional<Topic> topic = topicRepository.findById(topic1.getTopicId());

    Assertions.assertTrue(topic.isPresent());
    Assertions.assertEquals(topic1.getName(), topic.get().getName());
    Assertions.assertEquals(topic1.getCreatorId(), topic.get().getCreatorId());
  }

  @Test
  void shouldNotFindTopic() {
    final Optional<Topic> topic = topicRepository.findById(topic1.getTopicId() + 100);
    Assertions.assertFalse(topic.isPresent());
  }

  @Test
  void shouldGetAllArticles() {
    topicRepository.save(topic2);
    final List<Topic> topics = topicRepository.findAll();
    Assertions.assertEquals(2, topics.size());
  }

  @Test
  void shouldDeleteArticle() {
    Assertions.assertTrue(topicRepository.findById(topic1.getTopicId()).isPresent());
    topicRepository.deleteById(topic1.getTopicId());
    Assertions.assertFalse(topicRepository.findById(topic1.getTopicId()).isPresent());
  }
}
