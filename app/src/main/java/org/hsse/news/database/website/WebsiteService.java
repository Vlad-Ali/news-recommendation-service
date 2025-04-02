package org.hsse.news.database.website;

import org.hsse.news.api.schemas.response.website.WebsitesResponse;
import org.hsse.news.api.schemas.shared.WebsiteInfo;
import org.hsse.news.database.entity.UserEntity;
import org.hsse.news.database.entity.WebsiteEntity;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.user.repositories.JpaUsersRepository;
import org.hsse.news.database.website.exceptions.QuantityLimitExceededWebsitesPerUserException;
import org.hsse.news.database.website.exceptions.WebsiteAlreadyExistsException;
import org.hsse.news.database.website.exceptions.WebsiteNotFoundException;
import org.hsse.news.database.website.exceptions.WebsiteRSSNotValidException;
import org.hsse.news.database.website.models.WebsiteDto;
import org.hsse.news.database.website.models.WebsiteId;
import org.hsse.news.database.website.repositories.JpaWebsitesRepository;
import org.hsse.news.util.RSSValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class WebsiteService {
    private static final int MAX_WEBSITES_PER_USER = 10;

    private static final Logger LOG = LoggerFactory.getLogger(WebsiteService.class);
    private final JpaWebsitesRepository websitesRepository;
    private final JpaUsersRepository usersRepository;

    public WebsiteService(final JpaWebsitesRepository websitesRepository,final JpaUsersRepository usersRepository) {
        this.websitesRepository = websitesRepository;
        this.usersRepository = usersRepository;
    }

    public Optional<WebsiteInfo> findById(final WebsiteId websiteId) {
        LOG.debug("Method findById called");
        final Optional<WebsiteEntity> optionalWebsite = websitesRepository.findById(websiteId.value());
        if (optionalWebsite.isEmpty()){
            throw new WebsiteNotFoundException("Website is not found with id = " + websiteId);
        }
        final WebsiteEntity websiteEntity = optionalWebsite.get();
        final WebsiteDto websiteDto = websiteEntity.toWebsiteDto();
        return Optional.of(new WebsiteInfo(websiteEntity.getWebsiteId(), websiteDto.url(), websiteDto.description()));
    }


    public List<WebsiteInfo> getSubscribedWebsitesByUserId(final UserId userId) {
        LOG.debug("Method getSubscribedWebsitesByUserId called");
        final ArrayList<WebsiteEntity> websiteEntityArrayList = new ArrayList<>(websitesRepository.findSubscribedWebsitesByUserId(userId.value()));
        final ArrayList<WebsiteInfo> websites = new ArrayList<>();
        for(final WebsiteEntity entity : websiteEntityArrayList){
            final WebsiteDto websiteDto = entity.toWebsiteDto();
            websites.add(new WebsiteInfo(websiteDto.id().value(), websiteDto.url(), websiteDto.description()));
        }
        return websites.stream().toList();
    }

    public List<WebsiteInfo> getUnSubscribedWebsitesByUserId(final UserId userId) {
        LOG.debug("Method getUnSubscribedWebsitesByUserId called");
        final ArrayList<WebsiteEntity> websiteEntityArrayList = new ArrayList<>(websitesRepository.findUnSubscribedWebsitesByUserId(userId.value()));
        final ArrayList<WebsiteInfo> websites = new ArrayList<>();
        for(final WebsiteEntity entity : websiteEntityArrayList){
            final WebsiteDto websiteDto = entity.toWebsiteDto();
            websites.add(new WebsiteInfo(websiteDto.id().value(), websiteDto.url(), websiteDto.description()));
        }
        return websites.stream().toList();
    }

    public WebsitesResponse getSubAndUnSubWebsites(final UserId userId){
        LOG.debug("Method getSubAndUnSubWebsites called");
        return new WebsitesResponse(getSubscribedWebsitesByUserId(userId), getUnSubscribedWebsitesByUserId(userId));
    }

    @Transactional
    public WebsiteDto create(final WebsiteDto websiteDto) {
        LOG.debug("Method create called");
        if (!RSSValidator.isRSSFeedValid(websiteDto.url())){
            throw new WebsiteRSSNotValidException("Not valid rss for website");
        }
        final Optional<WebsiteEntity> optionalWebsite = websitesRepository.findByUrl(websiteDto.url());
        if (optionalWebsite.isPresent()){
            throw new WebsiteAlreadyExistsException("Website already exists with url = " + websiteDto.url());
        }
        final Optional<UserEntity> optionalUser = usersRepository.findById(websiteDto.creatorId().value());
        final UserEntity userEntity = optionalUser.get();
        final WebsiteEntity websiteEntity = websiteDto.toWebsiteEntity(userEntity);
        userEntity.addWebsite(websiteEntity);
        final UserEntity savedUser = usersRepository.save(userEntity);
        final WebsiteEntity savedWebsite = savedUser.getCreatedWebsites().stream()
                .filter(web -> web.getUrl().equals(websiteDto.url()))
                .findFirst().get();
        return savedWebsite.toWebsiteDto();
    }

    @Transactional
    public void tryUpdateSubscribedWebsites(final List<WebsiteId> websites, final UserId userId) {
        LOG.debug("Method updateSubWebsites called");
        if (websites.size() > MAX_WEBSITES_PER_USER) {
            throw new QuantityLimitExceededWebsitesPerUserException();
        }

        final ArrayList<WebsiteEntity> websiteEntityArrayList = new ArrayList<>();
        for (final WebsiteId websiteId : websites){
            final Optional<WebsiteEntity> optionalWebsite = websitesRepository.findById(websiteId.value());
            if (optionalWebsite.isEmpty()){
                throw new WebsiteNotFoundException("Website is not found with id = " + websiteId.value());
            }
            websiteEntityArrayList.add(optionalWebsite.get());
        }
        final UserEntity userEntity = usersRepository.findById(userId.value()).get();
        userEntity.getSubscribedWebsites().clear();
        for (final WebsiteEntity websiteEntity : websiteEntityArrayList){
            userEntity.subscribeToWebsite(websiteEntity);
        }
        usersRepository.save(userEntity);
    }

    /**
     * @throws WebsiteNotFoundException if the website does not exist
     */
    @Transactional
    public void delete(final WebsiteId websiteId, final UserId creatorId) {
        LOG.debug("Method delete called");
        final Optional<UserEntity> optionalUser = usersRepository.findById(creatorId.value());
        final UserEntity userEntity = optionalUser.get();
        final Optional<WebsiteEntity> optionalWebsite = websitesRepository.findById(websiteId.value());
        if(optionalWebsite.isEmpty()){
            throw new WebsiteNotFoundException("Website is not found with id = " + websiteId.value());
        }
        final WebsiteEntity websiteEntity = optionalWebsite.get();
        if (websiteEntity.getCreatorId()==null || !websiteEntity.getCreatorId().equals(userEntity.getId())){
            throw new WebsiteNotFoundException("Website is not found with id = " + websiteId.value());
        }
        userEntity.removeWebsite(websiteEntity);
        usersRepository.save(userEntity);
        websitesRepository.deleteById(websiteId.value());
    }

    public List<WebsiteInfo> getWebsitesByUserTopic(final TopicId topicId, final UserId userId){
        LOG.debug("Method getWebsitesByUserTopic called");
        final List<WebsiteInfo> websiteInfos = new ArrayList<>();
        final List<WebsiteEntity> recommendedWebsites = websitesRepository.getWebsitesByUserTopic(topicId.value(), userId.value());
        for (final WebsiteEntity websiteEntity : recommendedWebsites){
            final WebsiteInfo websiteInfo = new WebsiteInfo(websiteEntity.getWebsiteId(), websiteEntity.getUrl(), websiteEntity.getDescription());
            websiteInfos.add(websiteInfo);
        }
        return websiteInfos;
    }

    public List<WebsiteInfo> getAllWebsites(){
        LOG.debug("Method getAllWebsites called");
        final List<WebsiteInfo> websiteInfos = new ArrayList<>();
        final List<WebsiteEntity> websiteEntities = websitesRepository.findAll();
        for (final WebsiteEntity websiteEntity : websiteEntities){
            final WebsiteInfo websiteInfo = new WebsiteInfo(websiteEntity.getWebsiteId(),websiteEntity.getUrl(), websiteEntity.getDescription());
            websiteInfos.add(websiteInfo);
        }
        return websiteInfos;
    }
}
