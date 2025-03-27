package org.hsse.news;

import org.hsse.news.api.schemas.shared.WebsiteInfo;
import org.hsse.news.database.article.models.Article;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.topic.models.TopicDto;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.user.UserService;
import org.hsse.news.database.user.models.UserDto;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.website.WebsiteService;
import org.hsse.news.database.website.exceptions.QuantityLimitExceededWebsitesPerUserException;
import org.hsse.news.database.website.exceptions.WebsiteAlreadyExistsException;
import org.hsse.news.database.website.exceptions.WebsiteRSSNotValidException;
import org.hsse.news.database.website.models.WebsiteDto;
import org.hsse.news.database.website.models.WebsiteId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class StubDataProvider {
    private final UserService userService;
    private final WebsiteService websiteService;
    private final static String EXAMPLE_URI = "example.com";
    private final static UserId EXAMPLE_USER_ID =
            new UserId(UUID.fromString("027e71c2-f90b-43b9-8dbf-5e7f2da771ae"));
    private final static Logger LOG = LoggerFactory.getLogger(StubDataProvider.class);

    public StubDataProvider(UserService userService, WebsiteService websiteService) {
        this.userService = userService;
        this.websiteService = websiteService;
    }

    public void registerUser(final Long chatId){
        Optional<UserDto> optionalUserDto = userService.findByChatId(chatId);
        if (optionalUserDto.isEmpty()){
            userService.register(new UserDto(String.valueOf(UUID.randomUUID()), String.valueOf(UUID.randomUUID()), String.valueOf(UUID.randomUUID()), chatId));
        }
    }

    public List<WebsiteInfo> getSubbedWebsites(Long chatId) {
        Optional<UserDto> userDto = userService.findByChatId(chatId);
        return websiteService.getSubscribedWebsitesByUserId(userDto.get().id());
    }

    public List<WebsiteInfo> getUnsubbedWebsites(Long chatId) {
        /*return List.of(new WebsiteDto(new WebsiteId(1L), EXAMPLE_URI, "example2",
                EXAMPLE_USER_ID));*/
        Optional<UserDto> userDto = userService.findByChatId(chatId);
        return websiteService.getUnSubscribedWebsitesByUserId(userDto.get().id());
    }

    public Optional<WebsiteInfo> findWebsite(final Long websiteId) {
        return websiteService.findById(new WebsiteId(websiteId));
        /*return switch (id.value().intValue()) {
            case 0 -> Optional.of(new WebsiteDto(new WebsiteId(0L),
                    EXAMPLE_URI, "example", EXAMPLE_USER_ID));
            case 1 -> Optional.of(new WebsiteDto(new WebsiteId(1L),
                    EXAMPLE_URI, "example2", EXAMPLE_USER_ID));
            default -> Optional.empty();
        };*/
    }

    public boolean isSubbed(final Long chatId, final Long websiteId) {
        Optional<UserDto> userDto = userService.findByChatId(chatId);
        List<WebsiteInfo> websiteInfos  = websiteService.getSubscribedWebsitesByUserId(userDto.get().id());
        for (WebsiteInfo websiteInfo : websiteInfos){
            if (websiteInfo.websiteId().equals(websiteId)){
                return true;
            }
        }
        return false;
    }

    public void createCustomWebsite(final Long chatId, final String url, final String description) throws WebsiteRSSNotValidException, WebsiteAlreadyExistsException {
        Optional<UserDto> userDto = userService.findByChatId(chatId);
        final UserId userId = userDto.get().id();
        LOG.debug(url+" "+description+" "+userId);
        websiteService.create(new WebsiteDto(null,url, description, userId));
    }

    public boolean subWebsite(final Long chatId, final Long websiteId){
        Optional<UserDto> userDto = userService.findByChatId(chatId);
        List<WebsiteInfo> websiteInfos  = websiteService.getSubscribedWebsitesByUserId(userDto.get().id());
        List<WebsiteId> websiteIds = new ArrayList<>();
        for (WebsiteInfo websiteInfo : websiteInfos){
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
        Optional<UserDto> userDto = userService.findByChatId(chatId);
        List<WebsiteInfo> websiteInfos = websiteService.getSubscribedWebsitesByUserId(userDto.get().id());
        List<WebsiteId> websiteIds = new ArrayList<>();
        for (WebsiteInfo websiteInfo : websiteInfos){
            if (!websiteInfo.websiteId().equals(websiteId)) {
                websiteIds.add(new WebsiteId(websiteInfo.websiteId()));
            }
        }
        websiteService.tryUpdateSubscribedWebsites(websiteIds, userDto.get().id());
    }

    public List<TopicDto> getSubbedTopics() {
        return List.of(new TopicDto(new TopicId(0L), "test", null));
    }

    public List<TopicDto> getUnsubbedTopics() {
        return List.of(new TopicDto(new TopicId(1L), "test2", null));
    }

    public Optional<TopicDto> findTopic(final TopicId id) {
        return switch (id.value().intValue()) {
            case 0 -> Optional.of(new TopicDto(new TopicId(0L), "test", null));
            case 1 -> Optional.of(new TopicDto(new TopicId(1L), "test2", null));
            default -> Optional.empty();
        };
    }

    public boolean isSubbed(final TopicId id) {
        return id.value() == 0;
    }

    public Article getExampleArticle() {
        return new Article(
                new ArticleId(UUID.randomUUID()), "test", EXAMPLE_URI,
                Timestamp.from(Instant.now()), new TopicId(0L), new WebsiteId(0L));
    }
}
