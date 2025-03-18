package org.hsse.news.database.website.repositories;

import org.hsse.news.api.schemas.shared.WebsiteInfo;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.website.exceptions.WebsiteAlreadyExistsException;
import org.hsse.news.database.website.exceptions.WebsiteNotFoundException;
import org.hsse.news.database.website.models.WebsiteDto;
import org.hsse.news.database.website.models.WebsiteId;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

public interface WebsiteRepository {
    Optional<WebsiteInfo> findById(@NotNull WebsiteId websiteId);

    /**
     * @throws WebsiteAlreadyExistsException if the website exist
     */
    @NotNull WebsiteDto create(@NotNull WebsiteDto websiteDto);

    @NotNull List<WebsiteDto> getAll();

    @NotNull List<WebsiteInfo> findSubscribedWebsitesByUserId(@NotNull UserId creatorId);

    @NotNull List<WebsiteInfo> findUnSubscribedWebsitesByUserId(@NotNull UserId creatorId);

    /**
     * @throws WebsiteNotFoundException if the website does not exist
     * @throws WebsiteAlreadyExistsException if the website exist
     */
    void update(@NotNull WebsiteDto websiteDto);

    /**
     * @throws WebsiteNotFoundException if the website does not exist
     */
    void delete(@NotNull WebsiteId websiteId, @NotNull UserId creatorId);

    void updateSubscribedWebsites(@NotNull List<WebsiteId> websites, @NotNull UserId userId);
}
