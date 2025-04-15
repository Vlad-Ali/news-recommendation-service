package org.hsse.news.database.article;

import org.hsse.news.database.article.exceptions.ArticleNotFoundException;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.article.models.ArticleTopRecord;
import org.hsse.news.database.article.repositories.JpaArticleRepository;
import org.hsse.news.database.article.repositories.JpaUserArticlesRepository;
import org.hsse.news.database.entity.ArticleEntity;
import org.hsse.news.database.entity.UserArticlesEntity;
import org.hsse.news.database.entity.UserEntity;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.user.repositories.JpaUsersRepository;
import org.hsse.news.dto.RequestUserArticleDto;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class UserArticlesService {
    private final JpaUserArticlesRepository userArticlesRepository;
    private final JpaUsersRepository userRepository;
    private final JpaArticleRepository articleRepository;
    private static final Logger LOG = LoggerFactory.getLogger(UserArticlesService.class);

    public UserArticlesService(final JpaUserArticlesRepository userArticlesRepository, final JpaUsersRepository userRepository, final JpaArticleRepository articleRepository) {
        this.userArticlesRepository = userArticlesRepository;
        this.userRepository = userRepository;
        this.articleRepository = articleRepository;
    }

    @Transactional(readOnly = true)
    public Integer getArticleLikeCount(final UUID articleId) {
        return userArticlesRepository.getArticleLikeCount(articleId);
    }

    @Transactional(readOnly = true)
    public Integer getArticleDislikeCount(final UUID articleId) {
        return userArticlesRepository.getArticleDislikeCount(articleId);
    }

    @Transactional()
    public void create(final @NotNull RequestUserArticleDto createDto) {
        final UserArticlesEntity userArticle = new UserArticlesEntity();

        final ArticleEntity article = articleRepository.findById(createDto.articleId())
                .orElseThrow(() ->  new ArticleNotFoundException(new ArticleId(createDto.articleId())));

        final UserEntity user = userRepository.findById(createDto.userId())
                .orElseThrow(() ->  new UserNotFoundException(String.valueOf(createDto.userId())));

        userArticle.setId(
                new UserArticlesEntity.Id(user, article)
        );
        userArticle.setGrade(createDto.grade());
        userArticlesRepository.save(userArticle);

        LOG.debug("Creating user article {}", userArticle.getId());
    }

    @Transactional()
    public void delete(final @NotNull RequestUserArticleDto requestUserArticleDto) {
        userArticlesRepository.deleteByUserIds(
                requestUserArticleDto.userId(),
                requestUserArticleDto.articleId()
        );

        LOG.debug("Deleting user article {}", requestUserArticleDto.articleId());
    }

    @Transactional()
    public void likeUserArticle(final @NotNull RequestUserArticleDto requestUserArticleDto) {
        final ArticleEntity articleEntity = articleRepository.findById(requestUserArticleDto.articleId()).orElseThrow(() -> new ArticleNotFoundException(new ArticleId(requestUserArticleDto.articleId())));
        final UserEntity userEntity = userRepository.findById(requestUserArticleDto.userId()).orElseThrow(() -> new UserNotFoundException(new UserId(requestUserArticleDto.userId())));
        final UserArticlesEntity userArticlesEntity = userArticlesRepository.findById(new UserArticlesEntity.Id(userEntity, articleEntity)).orElseThrow(() -> new ArticleNotFoundException(new ArticleId(requestUserArticleDto.articleId())));
        userArticlesRepository.likeUserArticle(requestUserArticleDto.userId(), requestUserArticleDto.articleId());
        LOG.debug("Liking user article {}", requestUserArticleDto.articleId());

    }

    @Transactional()
    public void dislikeUserArticle(final @NotNull RequestUserArticleDto requestUserArticleDto) {
        final ArticleEntity articleEntity = articleRepository.findById(requestUserArticleDto.articleId()).orElseThrow(() -> new ArticleNotFoundException(new ArticleId(requestUserArticleDto.articleId())));
        final UserEntity userEntity = userRepository.findById(requestUserArticleDto.userId()).orElseThrow(() -> new UserNotFoundException(new UserId(requestUserArticleDto.userId())));
        final UserArticlesEntity userArticlesEntity = userArticlesRepository.findById(new UserArticlesEntity.Id(userEntity, articleEntity)).orElseThrow(() -> new ArticleNotFoundException(new ArticleId(requestUserArticleDto.articleId())));
        userArticlesRepository.dislikeUserArticle(requestUserArticleDto.userId(), requestUserArticleDto.articleId());
        LOG.debug("Disliking user article {}", requestUserArticleDto.articleId());

    }

    @Transactional()
    public void removeMarkFromUserArticle(final @NotNull RequestUserArticleDto requestUserArticleDto) {
        final ArticleEntity articleEntity = articleRepository.findById(requestUserArticleDto.articleId()).orElseThrow(() -> new ArticleNotFoundException(new ArticleId(requestUserArticleDto.articleId())));
        userArticlesRepository.removeMarkFromUserArticle(requestUserArticleDto.userId(), requestUserArticleDto.articleId());
        LOG.debug("Removing mark from user article {}", requestUserArticleDto.articleId());
    }

    @Transactional(readOnly = true)
    public boolean isLikedArticle(final @NotNull ArticleId articleId,final @NotNull UserId userId){
        final ArticleEntity articleEntity = articleRepository.findById(articleId.value()).orElseThrow(() -> new ArticleNotFoundException(articleId));
        final UserEntity userEntity = userRepository.findById(userId.value()).orElseThrow(() -> new UserNotFoundException(userId));
        final UserArticlesEntity userArticlesEntity = userArticlesRepository.findById(new UserArticlesEntity.Id(userEntity, articleEntity)).orElseThrow(() -> new ArticleNotFoundException(articleId));
        return userArticlesEntity.getGrade().equals(1);
    }

    @Transactional(readOnly = true)
    public boolean isDislikedArticle(final @NotNull ArticleId articleId,final @NotNull UserId userId){
        final ArticleEntity articleEntity = articleRepository.findById(articleId.value()).orElseThrow(() -> new ArticleNotFoundException(articleId));
        final UserEntity userEntity = userRepository.findById(userId.value()).orElseThrow(() -> new UserNotFoundException(userId));
        final UserArticlesEntity userArticlesEntity = userArticlesRepository.findById(new UserArticlesEntity.Id(userEntity, articleEntity)).orElseThrow(() -> new ArticleNotFoundException(articleId));
        return userArticlesEntity.getGrade().equals(-1);
    }

    @Transactional(readOnly = true)
    public List<ArticleTopRecord> findTopKArticles(final int limit) {
        return userArticlesRepository.findTopKArticle(limit);
    }
}
