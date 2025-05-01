package org.hsse.news.bot.website;

import lombok.extern.slf4j.Slf4j;
import org.hsse.news.api.schemas.shared.TopicInfo;
import org.hsse.news.api.schemas.shared.WebsiteInfo;
import org.hsse.news.database.topic.TopicService;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.user.UserService;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
import org.hsse.news.database.user.models.UserDto;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.userrequest.UserRequestService;
import org.hsse.news.database.userrequest.exception.TimeLimitException;
import org.hsse.news.database.website.WebsiteService;
import org.hsse.news.database.website.exceptions.QuantityLimitExceededWebsitesPerUserException;
import org.hsse.news.database.website.exceptions.WebsiteAlreadyExistsException;
import org.hsse.news.database.website.exceptions.WebsiteRSSNotValidException;
import org.hsse.news.database.website.models.WebsiteDto;
import org.hsse.news.database.website.models.WebsiteId;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class WebsitesDataProvider {
    private final UserService userService;
    private final WebsiteService websiteService;
    private final TopicService topicService;
    private final UserRequestService userRequestService;

    public WebsitesDataProvider(final UserService userService, final WebsiteService websiteService, final TopicService topicService, final UserRequestService userRequestService) {
        this.userService = userService;
        this.websiteService = websiteService;
        this.topicService = topicService;
        this.userRequestService = userRequestService;
    }

    public List<WebsiteInfo> getSubbedWebsites(final Long chatId) {
        final Optional<UserDto> userDto = userService.findByChatId(chatId);
        return websiteService.getSubscribedWebsitesByUserId(userDto.get().id());
    }

    public List<WebsiteInfo> getUnsubbedWebsites(final Long chatId) {
        final Optional<UserDto> userDto = userService.findByChatId(chatId);
        return websiteService.getUnSubscribedWebsitesByUserId(userDto.get().id());
    }

    public Optional<WebsiteInfo> findWebsite(final Long websiteId) {
        return websiteService.findById(new WebsiteId(websiteId));
    }

    public boolean isSubbedWebsite(final Long chatId, final Long websiteId) {
        final Optional<UserDto> userDto = userService.findByChatId(chatId);
        final List<WebsiteInfo> websiteInfos  = websiteService.getSubscribedWebsitesByUserId(userDto.get().id());
        for (final WebsiteInfo websiteInfo : websiteInfos){
            if (websiteInfo.websiteId().equals(websiteId)){
                return true;
            }
        }
        return false;
    }

    public void createCustomWebsite(final Long chatId, final String url, final String description) throws WebsiteRSSNotValidException, WebsiteAlreadyExistsException {
        final Optional<UserDto> userDto = userService.findByChatId(chatId);
        if (userDto.isEmpty()){
            throw new UserNotFoundException("user not found by chatId = "+ chatId);
        }
        final UserId userId = userDto.get().id();
        final WebsiteDto createdWebsite = websiteService.create(new WebsiteDto(null,url, description, userId));
        final List<WebsiteInfo> websiteInfos = websiteService.getSubscribedWebsitesByUserId(userId);
        final List<WebsiteId> websiteIds = new ArrayList<>();
        for (final WebsiteInfo websiteInfo : websiteInfos){
            websiteIds.add(new WebsiteId(websiteInfo.websiteId()));
        }
        websiteIds.add(createdWebsite.id());
        try {
            websiteService.tryUpdateSubscribedWebsites(websiteIds, userId);
        } catch (QuantityLimitExceededWebsitesPerUserException e) {
            log.debug("Limit of subWebsites for user {},{}", userId.value(), e.getMessage());
        }
    }

    public void deleteCustomWebsite(final Long chatId, final Long websiteId){
        final Optional<UserDto> userDto = userService.findByChatId(chatId);
        final UserId userId = userDto.get().id();
        if (userId != null) {
            websiteService.delete(new WebsiteId(websiteId), userId);
        }
    }

    public boolean isCustomCreatedWebsiteByUser(final Long chatId, final Long websiteId){
        final Optional<UserDto> userDto = userService.findByChatId(chatId);
        if (userDto.isEmpty()){
            throw new RuntimeException();
        }
        final UserId userId = userDto.get().id();
        final Optional<WebsiteDto> optionalWebsiteDto = websiteService.getWebsiteDtoById(new WebsiteId(websiteId));
        if (optionalWebsiteDto.isEmpty()){
            throw new RuntimeException();
        }
        return userId.equals(optionalWebsiteDto.get().creatorId());
    }

    public boolean subWebsite(final Long chatId, final Long websiteId){
        final Optional<UserDto> userDto = userService.findByChatId(chatId);
        final List<WebsiteInfo> websiteInfos  = websiteService.getSubscribedWebsitesByUserId(userDto.get().id());
        final List<WebsiteId> websiteIds = new ArrayList<>();
        for (final WebsiteInfo websiteInfo : websiteInfos){
            websiteIds.add(new WebsiteId(websiteInfo.websiteId()));
        }
        websiteIds.add(new WebsiteId(websiteId));
        try {
            websiteService.tryUpdateSubscribedWebsites(websiteIds, userDto.get().id());
        } catch (QuantityLimitExceededWebsitesPerUserException ex){
            return false;
        }
        return true;
    }

    public void unSubWebsite(final Long chatId, final Long websiteId){
        final Optional<UserDto> userDto = userService.findByChatId(chatId);
        final List<WebsiteInfo> websiteInfos = websiteService.getSubscribedWebsitesByUserId(userDto.get().id());
        final List<WebsiteId> websiteIds = new ArrayList<>();
        for (final WebsiteInfo websiteInfo : websiteInfos){
            if (!websiteInfo.websiteId().equals(websiteId)) {
                websiteIds.add(new WebsiteId(websiteInfo.websiteId()));
            }
        }
        websiteService.tryUpdateSubscribedWebsites(websiteIds, userDto.get().id());
    }

    public List<WebsiteInfo> recommendWebsitesByTopic(final Long chatId,final Long topicId){
        final Optional<UserDto> optionalUserDto = userService.findByChatId(chatId);
        final UserDto userDto = optionalUserDto.orElseThrow(() -> new UserNotFoundException("User not found by chatId = "+chatId));
        return websiteService.getWebsitesByUserTopic(new TopicId(topicId), userDto.id());
    }

    public List<TopicInfo> getUserSubTopics(final Long chatId){
        final Optional<UserDto> optionalUserDto = userService.findByChatId(chatId);
        final UserDto userDto = optionalUserDto.orElseThrow(() -> new UserNotFoundException("User not found by chatId = "+chatId));
        return topicService.getSubscribedTopicsByUserId(userDto.id());
    }

    public void createUserRequest(final String url, final Long chatId){
        final Optional<UserDto> optionalUserDto = userService.findByChatId(chatId);
        final UserDto userDto = optionalUserDto.orElseThrow(() -> new UserNotFoundException("User not found by chatId = "+chatId));
        final UserId userId = userDto.id();
        final Instant lastTime = userRequestService.getLastRequestByUserId(userId);
        final long days = Duration.between(lastTime, Instant.now()).toDays();
        if (days < 2){
            throw new TimeLimitException("Ваше последнее сообщение было отправлено "+lastTime+". 2 дня с этого момента не прошло");
        }
        userRequestService.addUserRequest(userId, url);
    }

}
