package org.hsse.news.database.website;

import lombok.AllArgsConstructor;
import org.hsse.news.api.schemas.response.website.WebsitesResponse;
import org.hsse.news.api.schemas.shared.WebsiteInfo;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.util.TransactionManager;
import org.hsse.news.database.website.exceptions.QuantityLimitExceededWebsitesPerUserException;
import org.hsse.news.database.website.exceptions.WebsiteAlreadyExistsException;
import org.hsse.news.database.website.exceptions.WebsiteNotFoundException;
import org.hsse.news.database.website.models.Website;
import org.hsse.news.database.website.models.WebsiteId;
import org.hsse.news.database.website.repositories.WebsiteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public final class WebsiteService {
    private static final int MAX_WEBSITES_PER_USER = 10;

    private WebsiteRepository websiteRepository;
    private TransactionManager transactionManager;

    /*public WebsiteService(
            final WebsiteRepository websiteRepository,
            final TransactionManager transactionManager
    ) {
        this.websiteRepository = websiteRepository;
        this.transactionManager = transactionManager;
    }*/

    public Optional<WebsiteInfo> findById(final WebsiteId websiteId) {
        return websiteRepository.findById(websiteId);
    }

    public List<Website> getAll() {
        return websiteRepository.getAll();
    }

    public List<WebsiteInfo> getSubscribedWebsitesByUserId(final UserId userId) {
        return websiteRepository.findSubscribedWebsitesByUserId(userId);
    }

    public List<WebsiteInfo> getUnSubscribedWebsitesByUserId(final UserId userId) {
        return websiteRepository.findUnSubscribedWebsitesByUserId(userId);
    }

    public WebsitesResponse getSubAndUnSubWebsites(final UserId userId){
        return new WebsitesResponse(getSubscribedWebsitesByUserId(userId), getUnSubscribedWebsitesByUserId(userId));
    }

    public Website create(final Website website) {
        return websiteRepository.create(website);
    }

    /**
     * @throws WebsiteNotFoundException if the website does not exist
     * @throws WebsiteAlreadyExistsException if the website exist
     */
    public void update(final WebsiteId websiteId, final String url, final String description) {
        transactionManager.useTransaction(() -> {
            final WebsiteInfo websiteInfoToUpdate =
                    websiteRepository.findById(websiteId)
                            .orElseThrow(() -> new WebsiteNotFoundException(websiteId));
            final Website websiteToUpdate = new Website(websiteId, websiteInfoToUpdate.url(), websiteInfoToUpdate.description(), null);

            websiteRepository.update(
                    websiteToUpdate
                            .withUrl(url)
                            .withDescription(description)
            );
        });
    }

    public void tryUpdateSubscribedWebsites(final List<WebsiteId> websites, final UserId userId) {
        if (websites.size() > MAX_WEBSITES_PER_USER) {
            throw new QuantityLimitExceededWebsitesPerUserException(userId);
        }

        try{
            websiteRepository.updateSubscribedWebsites(websites, userId);
        }
        catch (Exception ex){
            throw new WebsiteNotFoundException("Incorrect website IDs");
        }
    }

    /**
     * @throws WebsiteNotFoundException if the website does not exist
     */
    public void delete(final WebsiteId websiteId, final UserId creatorId) {
        if (findById(websiteId).isEmpty()){
            throw new WebsiteNotFoundException(websiteId);
        }
        websiteRepository.delete(websiteId, creatorId);
    }
}
