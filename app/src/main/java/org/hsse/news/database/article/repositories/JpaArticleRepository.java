package org.hsse.news.database.article.repositories;

import org.hsse.news.database.entity.ArticleEntity;
import org.hsse.news.database.entity.TopicEntity;
import org.hsse.news.database.entity.UserArticlesEntity;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JpaArticleRepository extends JpaRepository<ArticleEntity, UUID> {

    @Override
    @Query("select a from ArticleEntity a")
    @NotNull List<ArticleEntity> findAll();

    @Override
    @Query("select a from ArticleEntity a where a.articleId = :articleId")
    @NotNull Optional<ArticleEntity> findById(@NotNull UUID articleId);

    @Query(
            value = "select * from articles " +
                    "where articles.article_id in (select at.article_id from article_topics at where at.topic_id in (select topic_id from user_topics where user_id = :userId)) AND " +
                    "(select count(*) from user_articles where user_id = :userId and article_id = articles.article_id) = 0 AND " +
                    "website_id in (select website_id from user_websites where user_id = :userId) ",
            nativeQuery = true
    )
    List<ArticleEntity> getAllUnknown(@NotNull UUID userId);

    @Query(value = """
            select articles.article_id, article.title, article.url, article.created_at, article.website_id from articles inner join user_articles ua on articles.article_id = ua.article_id
            where articles.article_id in (select at.article_id from article_topics at where at.topic_id in (select topic_id from user_topics where user_id = :userId)) AND 
            (select count(*) from user_articles where user_id = :userId and article_id = articles.article_id) = 0 AND
            website_id in (select website_id from user_websites where user_id = :userId) 
            group by articles.article_id
            order by sum(ua.grade) desc
            limit 10;
            """, nativeQuery = true)
    List<ArticleEntity> getAllUnknownByLikes(@NotNull UUID userId);

    @Query(value = "select * from topics t where topic_id in (select at.topic_id from article_topics at where at.article_id = :articleId) and" +
            "t.topic_id in (select ut.topic_id from user_topics ut where ut.user_id = :userId)", nativeQuery = true)
    List<TopicEntity> getArticleTopicsForUser(@Param("articleId") UUID articleId, @Param("userId") UUID userId);

    @Query(value = "select * from user_articles ua where ua.user_id = :userId", nativeQuery = true)
    List<UserArticlesEntity> getUserArticles(@NotNull UUID userId);

    @Modifying
    @Query("update ArticleEntity a set a.title = :title, a.url = :url")
    void update(@NotNull UUID id, @NotNull String title, @NotNull String url);

    @Modifying
    @Query("delete from ArticleEntity a where a.articleId = :articleId")
    void delete(@NotNull UUID articleId);

    Optional<ArticleEntity> findByUrl(String url);
}
