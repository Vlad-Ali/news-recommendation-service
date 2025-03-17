package org.hsse.news.database.userarticles;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface UserArticlesRepository extends JpaRepository<UserArticle, Long> {

    @Query("select count(ua) from UserArticle ua where ua.articleId = :articleId and ua.mark = 1")
    Integer getArticleLikeCount(@Param("articleId") UUID articleId);

    @Query("select count(ua) from UserArticle ua where ua.articleId = :articleId and ua.mark = -1")
    Integer getArticleDislikeCount(@Param("articleId") UUID articleId);

    @Modifying
    @Query("update UserArticle ua set ua.mark = 1 where ua.articleId = :articleId and ua.userId = :userId")
    void likeUserArticle(@Param("userId") UUID userId, @Param("articleId") UUID articleId);

    @Modifying
    @Query("update UserArticle ua set ua.mark = -1 where ua.articleId = :articleId and ua.userId = :userId")
    void dislikeUserArticle(@Param("userId") UUID userId, @Param("articleId") UUID articleId);

    @Modifying
    @Query("update UserArticle ua set ua.mark = 0 where ua.articleId = :articleId and ua.userId = :userId")
    void removeMarkFromUserArticle(@Param("userId") UUID userId, @Param("articleId") UUID articleId);

    @Modifying
    @Query("delete UserArticle ua where ua.userId = :userId and ua.articleId = :articleId")
    void deleteByUserIds(@Param("userId") UUID userId, @Param("articleId") UUID articleId);
}
