package org.hsse.news.dto;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public record RequestUserArticleDto(
        @NotNull UUID articleId,
        @NotNull UUID userId,
        @NotNull Integer grade
) {}
