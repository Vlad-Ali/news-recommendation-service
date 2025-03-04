package org.hsse.news.database.article;

import org.hsse.news.database.article.exceptions.ArticleNotFoundException;
import org.hsse.news.database.article.models.Article;
import org.hsse.news.database.article.models.ArticleData;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.article.repositories.ArticleRepository;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.util.TransactionManager;
import org.hsse.news.database.website.models.WebsiteId;

import java.sql.Timestamp;
import java.util.List;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public final class ArticlesService {
    private final ArticleRepository articleRepository;
    private final TransactionManager transactionManager;

    public ArticleData findById(final ArticleId articleId) {
        return articleRepository
            .findById(articleId)
            .orElseThrow(() -> new ArticleNotFoundException(articleId));
    }

    public List<Article> getAllUnknown(final UserId userId) {
        return articleRepository.getAllUnknown(userId);
    }

    public Article create(final Article article) {
        return articleRepository.create(article);
    }

    public void update(final ArticleId articleId, // NOPMD
                       final String title,
                       final String url,
                       final Timestamp createdAt,
                       final TopicId topicId,
                       final WebsiteId websiteId
    ) {
        articleRepository.update(
            new Article(articleId, title, url, createdAt, topicId, websiteId)
        );
    }

    public void updateTitle(final Article article) {

    }

    public void delete(final ArticleId articleId) {
        articleRepository.delete(articleId);
    }
}
