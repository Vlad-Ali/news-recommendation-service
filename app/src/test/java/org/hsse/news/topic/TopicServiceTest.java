package org.hsse.news.topic;

import org.hsse.news.api.schemas.request.topic.CreateCustomTopicRequest;
import org.hsse.news.database.topic.TopicService;
import org.hsse.news.database.topic.exceptions.TopicNotFoundException;
import org.hsse.news.database.topic.models.Topic;
import org.hsse.news.database.topic.models.TopicDto;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.topic.repositories.TopicRepository;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.util.SampleDataUtil;
import org.hsse.news.database.util.TransactionManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.refEq;

@ExtendWith(MockitoExtension.class)
class TopicServiceTest {
    @Mock
    private TopicRepository repositoryMock;

    @Mock
    private TransactionManager transactionManagerMock;

    @InjectMocks
    private TopicService service;

    private static final Topic TOPIC_1 = new Topic(0L, "test", UUID.randomUUID());
    private static final Topic TOPIC_2 = new Topic(1L, "test2", UUID.randomUUID());

    @Test
    void testFindByIdSuccess() {
        Mockito.when(repositoryMock.findById(TOPIC_1.getTopicId()))
                .thenReturn(Optional.of(TOPIC_1));

        final Optional<TopicDto> topicOptional = service.getById(new TopicId(TOPIC_1.getTopicId()));

        Mockito.verify(repositoryMock).findById(TOPIC_1.getTopicId());
        assertTrue(topicOptional.isPresent(), "userOptional should be present");
        assertEquals(TOPIC_1.toDto(), topicOptional.get());
    }

    @Test
    void testFindByIdFail() {
        final long id = 0;

        assert SampleDataUtil.DEFAULT_USER.id() != null;
        Mockito.when(repositoryMock.findById(id))
                .thenReturn(Optional.empty());

        final Optional<TopicDto> topicOptional = service.getById(new TopicId(id));

        Mockito.verify(repositoryMock).findById(id);
        assertTrue(topicOptional.isEmpty(), "topicOptional should be empty");
    }

    @Test
    void testCreate() {
        final Topic savedTopic = new Topic(TOPIC_1.getName(), TOPIC_1.getCreatorId());

        Mockito.when(repositoryMock.save(refEq(savedTopic))).thenReturn(TOPIC_1);

        service.create(new CreateCustomTopicRequest(TOPIC_1.getName(), new UserId(TOPIC_1.getCreatorId())));

        Mockito.verify(repositoryMock).save(refEq(savedTopic));
    }

    @Test
    void testUpdateSuccess() {
        final Topic modifiedTopic = new Topic(
                TOPIC_1.getTopicId(), "new name", UUID.randomUUID());

        Mockito.when(repositoryMock.findById(TOPIC_1.getTopicId()))
                .thenReturn(Optional.of(TOPIC_1));
        Mockito.when(repositoryMock.save(modifiedTopic)).thenReturn(modifiedTopic);

        service.update(
                new TopicId(TOPIC_1.getTopicId()),
                new CreateCustomTopicRequest(
                        modifiedTopic.getName(),
                        new UserId(modifiedTopic.getCreatorId()))
        );

        Mockito.verify(repositoryMock).save(modifiedTopic);
    }

    @Test
    void testUpdateUserNotFound() {
        Mockito.when(repositoryMock.findById(404L))
                .thenReturn(Optional.empty());

        assertThrows(
                TopicNotFoundException.class,
                () -> service.update(
                        new TopicId(404L),
                        new CreateCustomTopicRequest("", new UserId(UUID.randomUUID()))
                )
        );
    }
}
