package org.hsse.news.dto;

import org.jetbrains.annotations.NotNull;

public record ResponseUserArticleDto(
        @NotNull ArticleDto article, // NOPMD
        @NotNull ResponseUserDto user, // NOPMD
        @NotNull Integer mark // NOPMD
) { }
