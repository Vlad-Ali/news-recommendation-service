package org.hsse.news.database.article.repositories;

import org.hsse.news.database.article.models.ArticleTopRecord;
import org.hsse.news.database.entity.UserArticlesEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface JpaUserArticlesRepository extends JpaRepository<UserArticlesEntity, UserArticlesEntity.Id> {

    @Query("select count(ua) from UserArticlesEntity ua where ua.id.articleId = :articleId and ua.grade = 1")
    Integer getArticleLikeCount(@Param("articleId") UUID articleId); // NOPMD

    @Query("select count(ua) from UserArticlesEntity ua where ua.id.articleId = :articleId and ua.grade = -1")
    Integer getArticleDislikeCount(@Param("articleId") UUID articleId);

    @Modifying
    @Query("update UserArticlesEntity ua set ua.grade = 1 where ua.id.articleId = :articleId and ua.id.userId = :userId")
    void likeUserArticle(@Param("userId") UUID userId, @Param("articleId") UUID articleId); // NOPMD

    @Modifying
    @Query("update UserArticlesEntity ua set ua.grade = -1 where ua.id.articleId = :articleId and ua.id.userId = :userId")
    void dislikeUserArticle(@Param("userId") UUID userId, @Param("articleId") UUID articleId);

    @Modifying
    @Query("update UserArticlesEntity ua set ua.grade = 0 where ua.id.articleId = :articleId and ua.id.userId = :userId")
    void removeMarkFromUserArticle(@Param("userId") UUID userId, @Param("articleId") UUID articleId);

    @Modifying
    @Query("delete UserArticlesEntity ua where ua.id.userId = :userId and ua.id.articleId = :articleId")
    void deleteByUserIds(@Param("userId") UUID userId, @Param("articleId") UUID articleId);

    @Query("""
            select new org.hsse.news.database.article.models.ArticleTopRecord(ua.id.articleId, sum(ua.grade))
            from UserArticlesEntity ua
            group by ua.id.articleId
            order by sum(ua.grade)
            desc limit :limit
            """)
    List<ArticleTopRecord> findTopKArticle(@Param("limit") int limit);
}
