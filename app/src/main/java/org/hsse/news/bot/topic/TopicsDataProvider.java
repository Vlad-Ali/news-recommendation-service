package org.hsse.news.bot.topic;

import lombok.extern.slf4j.Slf4j;
import org.hsse.news.api.schemas.shared.TopicInfo;
import org.hsse.news.database.topic.TopicService;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.user.UserService;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
import org.hsse.news.database.user.models.UserDto;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.topic.exceptions.QuantityLimitExceededTopicsPerUserException;
import org.hsse.news.database.topic.exceptions.TopicAlreadyExistsException;
import org.hsse.news.database.topic.models.TopicDto;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

@Slf4j
@Component
public class TopicsDataProvider { 
    private final UserService userService;
    private final TopicService topicService;

    public TopicsDataProvider(final UserService userService, final TopicService topicService) {
        this.userService = userService;
        this.topicService = topicService;
    }

    public List<TopicInfo> getSubbedTopics(final Long chatId) {
        final Optional<UserDto> userDto = userService.findByChatId(chatId);
        return topicService.getSubscribedTopicsByUserId(userDto.get().id());
    }

    public List<TopicInfo> getUnsubbedTopics(final Long chatId) {
        final Optional<UserDto> userDto = userService.findByChatId(chatId);
        return topicService.getUnSubscribedTopicsByUserId(userDto.get().id());
    }

    public Optional<TopicInfo> findTopic(final Long topicId) {
        return topicService.findById(new TopicId(topicId));
    }

    public boolean isSubbedTopic(final Long chatId, final Long topicId) {
        final Optional<UserDto> userDto = userService.findByChatId(chatId);
        final List<TopicInfo> topicInfos  = topicService.getSubscribedTopicsByUserId(userDto.get().id());
        for (final TopicInfo topicInfo : topicInfos){
            if (topicInfo.topicID().equals(topicId)){
                return true;
            }
        }
        return false;
    }

    public void createCustomTopic(final Long chatId, final String description) throws TopicAlreadyExistsException {
        final Optional<UserDto> userDto = userService.findByChatId(chatId);
        if (userDto.isEmpty()){
            throw new UserNotFoundException("user not found by chatId = "+ chatId);
        }
        final UserId userId = userDto.get().id();
        final TopicDto createdTopic = topicService.create(new TopicDto(null, description, userId));
        final List<TopicInfo> topicInfos = topicService.getSubscribedTopicsByUserId(userId);
        final List<TopicId> topicIds = new ArrayList<>();
        for (final TopicInfo topicInfo : topicInfos){
            topicIds.add(new TopicId(topicInfo.topicID()));
        }
        topicIds.add(createdTopic.id());
        try {
            topicService.tryUpdateSubscribedTopics(topicIds, userId);
        } catch (QuantityLimitExceededTopicsPerUserException e) {
            log.debug("Limit of subTopics for user {},{}", userId.value(), e.getMessage());
        }
    }

    public void deleteCustomTopic(final Long chatId, final Long topicId){
        final Optional<UserDto> userDto = userService.findByChatId(chatId);
        final UserId userId = userDto.get().id();
        if (userId != null) {
            topicService.delete(new TopicId(topicId), userId);
        }
    }

    public boolean isCustomCreatedTopicByUser(final Long chatId, final Long topicId){
        final Optional<UserDto> userDto = userService.findByChatId(chatId);
        if (userDto.isEmpty()){
            throw new RuntimeException();
        }
        final UserId userId = userDto.get().id();
        final Optional<TopicDto> optionalTopicDto = topicService.getTopicDtoById(new TopicId(topicId));
        if (optionalTopicDto.isEmpty()){
            throw new RuntimeException();
        }
        return userId.equals(optionalTopicDto.get().creatorId());
    }

    public boolean subTopic(final Long chatId, final Long topicId){
        final Optional<UserDto> userDto = userService.findByChatId(chatId);
        final List<TopicInfo> topicInfos  = topicService.getSubscribedTopicsByUserId(userDto.get().id());
        final List<TopicId> topicIds = new ArrayList<>();
        for (final TopicInfo topicInfo : topicInfos){
            topicIds.add(new TopicId(topicInfo.topicID()));
        }
        topicIds.add(new TopicId(topicId));
        try {
            topicService.tryUpdateSubscribedTopics(topicIds, userDto.get().id());
        } catch (QuantityLimitExceededTopicsPerUserException ex){
            return false;
        }
        return true;
    }

    public void unSubTopic(final Long chatId, final Long topicId){
        final Optional<UserDto> userDto = userService.findByChatId(chatId);
        final List<TopicInfo> topicInfos = topicService.getSubscribedTopicsByUserId(userDto.get().id());
        final List<TopicId> topicIds = new ArrayList<>();
        for (final TopicInfo topicInfo : topicInfos){
            if (!topicInfo.topicID().equals(topicId)) {
                topicIds.add(new TopicId(topicInfo.topicID()));
            }
        }
        topicService.tryUpdateSubscribedTopics(topicIds, userDto.get().id());
    }
}