package org.hsse.news.dto;

import java.sql.Timestamp;
import java.util.UUID;

public record ResponseArticleDto(UUID articleId,
                                 String title,
                                 String url,
                                 Timestamp createdAt,
                                 ResponseWebsiteDto website) {
}
