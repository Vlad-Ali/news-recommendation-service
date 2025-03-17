package org.hsse.news.database.article;

import lombok.extern.slf4j.Slf4j;
import org.hsse.news.database.article.exceptions.ArticleNotFoundException;
import org.hsse.news.database.article.models.Article;
import org.hsse.news.database.article.models.ArticleDto;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.article.repositories.ArticleRepository;
import org.hsse.news.database.user.models.UserId;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import lombok.AllArgsConstructor;
import org.hsse.news.database.userarticles.UserArticle;
import org.hsse.news.database.userarticles.UserArticleDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@AllArgsConstructor
public class ArticlesService {
    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public List<ArticleDto> getAll() {
        final List<Article> articles = articleRepository.findAll();
        return articles.stream().map(Article::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ArticleDto findById(final ArticleId articleId) {
        Article article =  articleRepository
            .findById(articleId.value())
            .orElseThrow(() -> new ArticleNotFoundException(articleId));
        return Article.toDto(article);
    }

    @Transactional(readOnly = true)
    public List<UserArticleDto> getUserArticles(final UUID userId) {
        List<UserArticle> articles = articleRepository.getUserArticles(userId);
        List<UserArticleDto> dtos = articles.stream().map(UserArticle::toDto).toList();
        return dtos;
    }

    @Transactional()
    public Article create(final ArticleDto articleDto) {
        Article article = new Article();
        article.setUrl(articleDto.url());
        article.setTitle(articleDto.title());
        article.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        article.setTopicId(articleDto.topicId());
        article.setWebsiteId(articleDto.websiteId());
        articleRepository.save(article);

        log.debug("Created article with id = {}", article.getArticleId());
        return article;
    }

    @Transactional()
    public void update(final ArticleId articleId, // NOPMD
                       final String title,
                       final String url,
                       final Timestamp createdAt,
                       final Long topicId,
                       final Long websiteId
    ) {

    }

    @Transactional()
    public void updateTitle(final Article article) {

    }

    @Transactional()
    public void delete(final ArticleId articleId) {
        articleRepository.delete(articleId.value());
    }
}
