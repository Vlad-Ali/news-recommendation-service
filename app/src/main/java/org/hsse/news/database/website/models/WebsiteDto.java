package org.hsse.news.database.website.models;

import org.hsse.news.database.user.exceptions.UserInitializationException;
import org.hsse.news.database.user.models.UserId;
import org.jdbi.v3.core.mapper.Nested;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record WebsiteDto(
        @Nested @Nullable WebsiteId id,
         @NotNull String url, @NotNull String description, @Nullable UserId creatorId
) {
    @JdbiConstructor
    public WebsiteDto {}

    public WebsiteDto(
            final @NotNull String url, final @NotNull String description,
            final @Nullable UserId creatorId
    ) {
        this(null, url, description, creatorId);
    }

    public WebsiteDto initializeWithId(final @NotNull WebsiteId newId) {
        if (id != null) {
            throw new UserInitializationException("Website is already initialized");
        }

        return new WebsiteDto(newId, url, description, creatorId);
    }

    public WebsiteDto withUrl(final @NotNull String newUrl) {
        return new WebsiteDto(id, newUrl, description, creatorId);
    }

    public WebsiteDto withDescription(final @NotNull String newDescription) {
        return new WebsiteDto(id, url, newDescription, creatorId);
    }

    public WebsiteDto withCreators(final @NotNull UserId newCreator) {
        return new WebsiteDto(id, url, description, newCreator);
    }

    @Override
    public boolean equals(final Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof WebsiteDto websiteDto)) {
            return false;
        }

        return id != null && id.equals(websiteDto.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
