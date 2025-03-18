package org.hsse.news.api.controllers.topic;

import org.hsse.news.api.schemas.request.topic.CustomTopicCreateRequest;
import org.hsse.news.api.schemas.request.topic.SubTopicsUpdateRequest;
import org.hsse.news.api.schemas.response.topic.TopicsResponse;
import org.hsse.news.api.schemas.shared.TopicInfo;
import org.hsse.news.database.topic.TopicService;
import org.hsse.news.database.topic.exceptions.QuantityLimitExceededTopicsPerUserException;
import org.hsse.news.database.topic.exceptions.TopicAlreadyExistsException;
import org.hsse.news.database.topic.exceptions.TopicNotFoundException;
import org.hsse.news.database.topic.models.TopicDto;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.user.models.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/topics")
public class TopicsController implements TopicOperations{
    private static final Logger LOG = LoggerFactory.getLogger(TopicsController.class);
    private static final int MAX_TOPICS_PER_USER = 10;
    private final TopicService topicService;

    public TopicsController(final TopicService topicService) {
        this.topicService = topicService;
    }

    @Override
    public ResponseEntity<TopicInfo> get(final Long id) throws TopicNotFoundException {
        getCurrentUserId();
        final Optional<TopicInfo> topic = topicService.findById(new TopicId(id));
        if (topic.isEmpty()){
            throw new TopicNotFoundException(new TopicId(id));
        }
        LOG.debug("Topic found by id = {} for user ", id);
        return ResponseEntity.ok(topic.get());
    }

    @Override
    public ResponseEntity<TopicsResponse> getUsersTopics() {
        final UserId userId = getCurrentUserId();
        LOG.debug("Topics found by user with id = {}", userId.value());
        return ResponseEntity.ok(topicService.getSubAndUnSubTopics(userId));
    }

    @Override
    public ResponseEntity<String> updateSubTopics(final SubTopicsUpdateRequest subTopicsUpdateRequest) throws QuantityLimitExceededTopicsPerUserException, TopicNotFoundException {
        final UserId userId = getCurrentUserId();
        final List<TopicId> topicIds = subTopicsUpdateRequest.topicIds().stream().map(TopicId::new).toList();
        try{
            topicService.tryUpdateSubscribedTopics(topicIds, userId);
        } catch (QuantityLimitExceededTopicsPerUserException ex){
            throw new QuantityLimitExceededTopicsPerUserException(String.format("Chosen topics more than limit = %s", MAX_TOPICS_PER_USER));
        }
        LOG.debug("Successfully updated subTopics for user with id = {}", userId.value());
        return ResponseEntity.ok("SubWebsites updated");
    }

    @Override
    public ResponseEntity<TopicInfo> create(final CustomTopicCreateRequest customTopicCreateRequest) throws TopicAlreadyExistsException {
        final UserId userId = getCurrentUserId();
        final TopicDto topicDto = topicService.create(new TopicDto(null,customTopicCreateRequest.name(), userId));
        LOG.debug("Successfully created topic with id = {}", topicDto.id());
        return ResponseEntity.ok(new TopicInfo(topicDto.id().value(), topicDto.description()));
    }

    @Override
    public ResponseEntity<String> delete(final Long topicId) throws TopicNotFoundException {
        final UserId userId = getCurrentUserId();
        topicService.delete(new TopicId(topicId), userId);
        LOG.debug("Successfully deleted topic with id = {} by user with id = {}",topicId, userId);
        return ResponseEntity.ok("Topic deleted");
    }

    private UserId getCurrentUserId() {
        final Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if ("anonymousUser".equals(principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authorization required");
        }
        return (UserId) principal;
    }

    @ExceptionHandler(TopicNotFoundException.class)
    public ErrorResponse handleTopicNotFoundException(final TopicNotFoundException ex){
        LOG.debug("Topic not found by id = {}", ex.getMessage());
        return ErrorResponse.create(ex, HttpStatus.BAD_REQUEST, "Topic not found");
    }

    @ExceptionHandler(TopicAlreadyExistsException.class)
    public ErrorResponse handleTopicAlreadyExistsException(final TopicAlreadyExistsException ex){
        LOG.debug("Topic already exists: {}", ex.getMessage());
        return ErrorResponse.create(ex, HttpStatus.CONFLICT, "Topic already exists");
    }

    @ExceptionHandler(QuantityLimitExceededTopicsPerUserException.class)
    public ErrorResponse handleQuantityLimitExceededTopicsPerUserException(final QuantityLimitExceededTopicsPerUserException ex){
        LOG.debug("Limit of subscribed topics: {}", ex.getMessage());
        return ErrorResponse.create(ex, HttpStatus.LENGTH_REQUIRED, "Limit of chosen topics");
    }

}
