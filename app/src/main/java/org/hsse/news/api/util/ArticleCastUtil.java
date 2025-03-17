package org.hsse.news.api.util;

import org.hsse.news.api.schemas.response.article.ArticleResponse;
import org.hsse.news.database.article.models.Article;
import org.hsse.news.database.topic.TopicService;
import org.hsse.news.database.website.WebsiteService;
import org.hsse.news.database.website.exceptions.WebsiteNotFoundException;

import java.util.ArrayList;
import java.util.List;

public final class ArticleCastUtil {
    private final TopicService topicService;
    private final WebsiteService websiteService;

    public ArticleCastUtil(final TopicService topicService, final WebsiteService websiteService) {
        this.topicService = topicService;
        this.websiteService = websiteService;
    }
}
