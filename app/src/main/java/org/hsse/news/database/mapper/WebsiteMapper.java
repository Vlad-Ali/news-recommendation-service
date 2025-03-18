package org.hsse.news.database.mapper;



import org.hsse.news.database.entity.UserEntity;
import org.hsse.news.database.entity.WebsiteEntity;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.website.models.WebsiteDto;
import org.hsse.news.database.website.models.WebsiteId;

import java.util.UUID;

final public class WebsiteMapper {

    private WebsiteMapper(){}

    public static WebsiteDto toWebsite(final WebsiteEntity websiteEntity){
        final Long websiteId = websiteEntity.getWebsiteId();
        final String url = websiteEntity.getUrl();
        final String description = websiteEntity.getDescription();
        final UUID userId = websiteEntity.getCreatorId();
        return new WebsiteDto(new WebsiteId(websiteId), url, description, new UserId(userId));
    }

    public static WebsiteEntity toWebsiteEntity(final WebsiteDto websiteDto, final UserEntity userEntity){
        final String url = websiteDto.url();
        final String description = websiteDto.description();
        return new WebsiteEntity(url, description, userEntity);
    }
}
