package org.hsse.news.database.userarticles;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record UserArticleDto(
        @NotNull UUID articleId, // NOPMD
        @NotNull UUID userId, // NOPMD
        @NotNull Integer mark // NOPMD
) { }
