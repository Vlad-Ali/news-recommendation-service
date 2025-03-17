package org.hsse.news.database.userarticles;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record UserArticleDto(
        @NotNull UUID articleId,
        @NotNull UUID userId,
        @NotNull Integer mark
) { }
