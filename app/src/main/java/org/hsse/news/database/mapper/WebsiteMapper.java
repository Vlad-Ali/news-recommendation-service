package org.hsse.news.database.mapper;



import org.hsse.news.database.entity.UserEntity;
import org.hsse.news.database.entity.WebsiteEntity;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.website.models.Website;
import org.hsse.news.database.website.models.WebsiteId;

import java.util.UUID;

final public class WebsiteMapper {

    private WebsiteMapper(){}

    public static Website toWebsite(final WebsiteEntity websiteEntity){
        final Long websiteId = websiteEntity.getWebsiteId();
        final String url = websiteEntity.getUrl();
        final String description = websiteEntity.getDescription();
        final UUID userId = websiteEntity.getCreatorId();
        return new Website(new WebsiteId(websiteId), url, description, new UserId(userId));
    }

    public static WebsiteEntity toWebsiteEntity(final Website website,final UserEntity userEntity){
        final String url = website.url();
        final String description = website.description();
        return new WebsiteEntity(url, description, userEntity);
    }
}
