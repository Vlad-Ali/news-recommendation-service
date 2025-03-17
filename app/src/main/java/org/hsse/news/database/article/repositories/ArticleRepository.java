package org.hsse.news.database.article.repositories;

import org.hsse.news.database.article.models.Article;
import org.hsse.news.database.article.models.ArticleDto;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.userarticles.UserArticle;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ArticleRepository extends JpaRepository<Article, UUID> {

    @Query("select a from Article a")
    @NotNull List<Article> findAll();

    @Query("select a from Article a where a.articleId = :articleId")
    @NotNull Optional<Article> findById(@NotNull UUID articleId);

    @Query("select ua from UserArticle ua where ua.userId = :userId")
    List<UserArticle> getUserArticles(UUID userId);

    @Modifying
    @Query("update Article a set a.title = :title, a.url = :url")
    void update(@NotNull UUID id, @NotNull String title, @NotNull String url);

    @Modifying
    @Query("delete from Article a where a.articleId = :articleId")
    void delete(@NotNull UUID articleId);
}
