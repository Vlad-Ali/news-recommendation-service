package org.hsse.news.parser;

import java.time.Instant;

public record ParsedArticle(
        String name,
        String description,
        Instant date,
        String link,
        String author,
        String websiteUrl) {
}
