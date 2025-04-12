package org.hsse.news.dto;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.hsse.news.database.entity.WebsiteEntity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Getter
@Builder(toBuilder = true)
@EqualsAndHashCode
public class ResponseWebsiteDto {

    private Long id;
    private @NotNull String url;
    private @NotNull String description;
    private UUID creatorId;

    public ResponseWebsiteDto(final Long id,final @NotNull String url,final @NotNull String description,final UUID creatorId){
        this.url = url;
        this.description = description;
        this.id = id;
        this.creatorId = creatorId;

    }

    public static ResponseWebsiteDto fromWebsite(final WebsiteEntity website) {
        return new ResponseWebsiteDto(
                website.getWebsiteId(),
                website.getUrl(),
                website.getDescription(),
                website.getCreatorId()
        );
    }

    public Long getId() {
        return id;
    }

    public @NotNull String getUrl() {
        return url;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public @NotNull String getDescription() {
        return description;
    }
}
