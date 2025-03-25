package org.hsse.news.database.userarticles;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hsse.news.database.article.exceptions.ArticleNotFoundException;
import org.hsse.news.database.article.models.Article;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.article.repositories.ArticleRepository;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Slf4j
@AllArgsConstructor
public class UserArticlesService {
    private final UserArticlesRepository userArticlesRepository;
    private final ArticleRepository articleRepository;

    @Transactional(readOnly = true)
    public Integer getArticleLikeCount(final UUID articleId) {
        return userArticlesRepository.getArticleLikeCount(articleId);
    }

    @Transactional(readOnly = true)
    public Integer getArticleDislikeCount(final UUID articleId) {
        return userArticlesRepository.getArticleDislikeCount(articleId);
    }

    @Transactional()
    public void create(final @NotNull UserArticleDto userArticleDto) {
        final UserArticle userArticle = new UserArticle();

        final Article article = articleRepository.findById(userArticleDto.articleId())
            .orElseThrow(() ->  new ArticleNotFoundException(new ArticleId(userArticleDto.articleId())));

        articleRepository.save(article);

        userArticle.setArticleId(article);
        userArticle.setUserId(userArticleDto.userId());
        userArticle.setGrade(userArticleDto.mark());
        userArticlesRepository.save(userArticle);

        log.debug("Creating user article {}", userArticleDto.articleId());
    }

    @Transactional()
    public void delete(final @NotNull UserArticleDto userArticleDto) {
        userArticlesRepository.deleteByUserIds(
                userArticleDto.userId(),
                userArticleDto.articleId()
        );

        log.debug("Deleting user article {}", userArticleDto.articleId());
    }

    @Transactional()
    public void likeUserArticle(final @NotNull UserArticleDto userArticleDto) {
        userArticlesRepository.likeUserArticle(userArticleDto.userId(), userArticleDto.articleId());

        log.debug("Liking user article {}", userArticleDto.articleId());
    }

    @Transactional()
    public void dislikeUserArticle(final @NotNull UserArticleDto userArticleDto) {
        userArticlesRepository.dislikeUserArticle(userArticleDto.userId(), userArticleDto.articleId());

        log.debug("Disliking user article {}", userArticleDto.articleId());
    }

    @Transactional()
    public void removeMarkFromUserArticle(final @NotNull UserArticleDto userArticleDto) {
        userArticlesRepository.removeMarkFromUserArticle(userArticleDto.userId(), userArticleDto.articleId());

        log.debug("Removing mark from user article {}", userArticleDto.articleId());
    }

}
