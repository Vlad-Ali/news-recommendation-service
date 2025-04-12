package org.hsse.news.dto;

import java.sql.Timestamp;

public record RequestArticleDto(
        String title,
        String url,
        Timestamp createdAt,
        Long websiteId
) {
}
