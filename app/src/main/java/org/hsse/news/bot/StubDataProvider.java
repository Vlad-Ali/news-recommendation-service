package org.hsse.news.bot;

import org.hsse.news.database.article.models.Article;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.topic.models.Topic;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.website.models.Website;
import org.hsse.news.database.website.models.WebsiteId;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class StubDataProvider {
    private final static String EXAMPLE_URI = "example.com";
    private final static UserId EXAMPLE_USER_ID =
            new UserId(UUID.fromString("027e71c2-f90b-43b9-8dbf-5e7f2da771ae"));

    public List<Website> getSubbedWebsites() {
        return List.of(new Website(new WebsiteId(0L), EXAMPLE_URI, "example",
                EXAMPLE_USER_ID));
    }

    public List<Website> getUnsubbedWebsites() {
        return List.of(new Website(new WebsiteId(1L), EXAMPLE_URI, "example2",
                EXAMPLE_USER_ID));
    }

    public Optional<Website> findWebsite(final WebsiteId id) {
        return switch (id.value().intValue()) {
            case 0 -> Optional.of(new Website(new WebsiteId(0L),
                    EXAMPLE_URI, "example", EXAMPLE_USER_ID));
            case 1 -> Optional.of(new Website(new WebsiteId(1L),
                    EXAMPLE_URI, "example2", EXAMPLE_USER_ID));
            default -> Optional.empty();
        };
    }

    public boolean isSubbed(final WebsiteId id) {
        return id.value() == 0;
    }

    public List<Topic> getSubbedTopics() {
        return List.of(new Topic(new TopicId(0L), "test"));
    }

    public List<Topic> getUnsubbedTopics() {
        return List.of(new Topic(new TopicId(1L), "test2"));
    }

    public Optional<Topic> findTopic(final TopicId id) {
        return switch (id.value().intValue()) {
            case 0 -> Optional.of(new Topic(new TopicId(0L), "test"));
            case 1 -> Optional.of(new Topic(new TopicId(1L), "test2"));
            default -> Optional.empty();
        };
    }

    public boolean isSubbed(final TopicId id) {
        return id.value() == 0;
    }

    public Article getExampleArticle() {
        return new Article(
                new ArticleId(UUID.randomUUID()), "test", EXAMPLE_URI,
                Timestamp.from(Instant.now()), new TopicId(0L), new WebsiteId(0L));
    }
}
