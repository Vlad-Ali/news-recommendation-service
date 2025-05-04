package org.hsse.news.database.topic;

import org.hsse.news.api.schemas.response.topic.TopicsResponse;
import org.hsse.news.api.schemas.shared.TopicInfo;
import org.hsse.news.database.entity.TopicEntity;
import org.hsse.news.database.entity.UserEntity;
import org.hsse.news.database.topic.exceptions.QuantityLimitExceededTopicsPerUserException;
import org.hsse.news.database.topic.exceptions.TopicAlreadyExistsException;
import org.hsse.news.database.topic.exceptions.TopicNotFoundException;
import org.hsse.news.database.topic.models.TopicDto;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.topic.repositories.JpaTopicsRepository;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.user.repositories.JpaUsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class TopicService {
    private static final int MAX_TOPICS_PER_USER = 10;

    private static final Logger LOG = LoggerFactory.getLogger(TopicService.class);
    private final JpaTopicsRepository topicsRepository;
    private final JpaUsersRepository usersRepository;

    public TopicService(final JpaTopicsRepository topicsRepository, final JpaUsersRepository usersRepository) {
        this.topicsRepository = topicsRepository;
        this.usersRepository = usersRepository;
    }

    @Transactional(readOnly = true)
    public Optional<TopicInfo> findById(final TopicId topicId){
        LOG.debug("Method findById called");
        final Optional<TopicEntity> optionalTopic = topicsRepository.findById(topicId.value());
        if (optionalTopic.isEmpty()){
            throw new TopicNotFoundException(topicId);
        }
        final TopicEntity topicEntity = optionalTopic.get();
        final TopicDto topicDto = topicEntity.toTopicDto();
        return Optional.of(new TopicInfo(topicEntity.getTopicId(), topicDto.description()));
    }

    public Optional<TopicDto> getTopicDtoById(final TopicId topicId){
        LOG.debug("Method findById called");
        final Optional<TopicEntity> optionalTopic = topicsRepository.findById(topicId.value());
        if(optionalTopic.isEmpty()){
            throw new TopicNotFoundException("Topic is not found with id = " + topicId);
        }
        final TopicEntity topicEntity = optionalTopic.get();
        final TopicDto topicDto = topicEntity.toTopicDto();
        return Optional.of(topicDto);
    }
    
    @Transactional(readOnly = true)
    public List<TopicInfo> getSubscribedTopicsByUserId(final UserId userId){
        LOG.debug("Method getSubscribedTopicsByUserId called");
        final ArrayList<TopicEntity> topicEntityArrayList = new ArrayList<>(topicsRepository.findSubscribedTopicsByUserId(userId.value()));
        final ArrayList<TopicInfo> topics = new ArrayList<>();
        for (final TopicEntity entity : topicEntityArrayList){
            final TopicDto topicDto = entity.toTopicDto();
            topics.add(new TopicInfo(topicDto.id().value(), topicDto.description()));
        }
        return topics.stream().toList();
    }

    @Transactional(readOnly = true)
    public List<TopicInfo> getUnSubscribedTopicsByUserId(final UserId userId){
        LOG.debug("Method getUnSubscribedTopicsByUserId called");
        final ArrayList<TopicEntity> topicEntityArrayList = new ArrayList<>(topicsRepository.findUnSubscribedTopicsByUserId(userId.value()));
        final ArrayList<TopicInfo> topics = new ArrayList<>();
        for (final TopicEntity entity : topicEntityArrayList){
            final TopicDto topicDto = entity.toTopicDto();
            topics.add(new TopicInfo(topicDto.id().value(), topicDto.description()));
        }
        return topics.stream().toList();
    }

    @Transactional(readOnly = true)
    public TopicsResponse getSubAndUnSubTopics(final UserId userId){
        LOG.debug("Method getSubAndUnSubTopics called");
        return new TopicsResponse(getSubscribedTopicsByUserId(userId), getUnSubscribedTopicsByUserId(userId));
    }

    @Transactional
    public TopicDto create(final TopicDto topicDto){
        LOG.debug("Method create called");
        final Optional<TopicEntity> optionalTopic = topicsRepository.findByName(topicDto.description());
        if (optionalTopic.isPresent()){
            throw new TopicAlreadyExistsException("Topic already exists with name = "+ topicDto.description());
        }

        final Optional<UserEntity> optionalUser = usersRepository.findById(topicDto.creatorId().value());
        final UserEntity userEntity = optionalUser.orElseThrow(() -> new UserNotFoundException(topicDto.creatorId()));
        final TopicEntity topicEntity = topicDto.toTopicEntity(userEntity);
        userEntity.addTopic(topicEntity);
        final UserEntity savedUser = usersRepository.save(userEntity);
        final TopicEntity savedTopic = savedUser.getCreatedTopics().stream()
                .filter(topic -> topic.getName().equals(topicDto.description()))
                .findFirst().get();
        return savedTopic.toTopicDto();
    }

    @Transactional
    public void tryUpdateSubscribedTopics(final List<TopicId> topics, final UserId userId){
        LOG.debug("Method updateSubTopics called");
        if (topics.size() > MAX_TOPICS_PER_USER){
            throw new QuantityLimitExceededTopicsPerUserException();
        }

        final ArrayList<TopicEntity> topicEntityArrayList = new ArrayList<>();
        for (final TopicId topicId : topics){
            final Optional<TopicEntity> optionalTopic = topicsRepository.findById(topicId.value());
            if (optionalTopic.isEmpty()){
                throw new TopicNotFoundException("Topic is not found by id = "+topicId.value());
            }
            topicEntityArrayList.add(optionalTopic.get());
        }
        final UserEntity userEntity = usersRepository.findById(userId.value()).orElseThrow(() -> new UserNotFoundException(userId));
        userEntity.getSubscribedTopics().clear();
        for (final TopicEntity topicEntity : topicEntityArrayList){
            userEntity.subscribeToTopic(topicEntity);
        }
        usersRepository.save(userEntity);
    }

    @Transactional
    public void delete(final TopicId topicId, final UserId creatorId){
        LOG.debug("Method delete called");
        final Optional<UserEntity> optionalUser = usersRepository.findById(creatorId.value());
        final UserEntity userEntity = optionalUser.orElseThrow(() -> new UserNotFoundException(creatorId));
        final Optional<TopicEntity> optionalTopic = topicsRepository.findById(topicId.value());
        if (optionalTopic.isEmpty()){
            throw new TopicNotFoundException("Topic is not found with id = "+topicId.value());
        }
        final TopicEntity topicEntity = optionalTopic.get();
        if (topicEntity.getCreatorId() == null || !topicEntity.getCreatorId().equals(userEntity.getId())){
            throw new TopicNotFoundException("Topic is not found with id = "+topicId.value());
        }
        userEntity.removeTopic(topicEntity);
        usersRepository.save(userEntity);
        topicsRepository.deleteById(topicId.value());
    }

    @Transactional(readOnly = true)
    public List<TopicInfo> getAllTopics(){
        LOG.debug("Method getAllTopics called");
        final List<TopicEntity> topicEntities = topicsRepository.findAll();
        final List<TopicInfo> topicInfos = new ArrayList<>();
        for (final TopicEntity topicEntity : topicEntities){
            final TopicInfo topicInfo = new TopicInfo(topicEntity.getTopicId(), topicEntity.getName());
            topicInfos.add(topicInfo);
        }
        return topicInfos;
    }

}
