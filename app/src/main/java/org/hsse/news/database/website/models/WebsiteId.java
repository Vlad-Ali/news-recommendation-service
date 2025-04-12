package org.hsse.news.database.website.models;

import org.hsse.news.util.AbstractId;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;
import org.jetbrains.annotations.NotNull;

public final class WebsiteId extends AbstractId<Long> {
    @JdbiConstructor
    public WebsiteId(final @ColumnName("website_id") @NotNull Long value) {
        super(value);
    }
}
