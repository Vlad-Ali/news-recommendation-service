package org.hsse.news.database.article.repositories;

import org.hsse.news.api.mapper.TopicIdMapper;
import org.hsse.news.api.mapper.WebsiteIdMapper;
import org.hsse.news.database.article.exceptions.ArticleNotFoundException;
import org.hsse.news.database.article.models.Article;
import org.hsse.news.database.article.models.ArticleData;
import org.hsse.news.database.article.models.ArticleId;
import org.hsse.news.database.topic.models.TopicId;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.website.models.WebsiteId;
import org.hsse.news.util.JdbiProvider;
import org.jdbi.v3.core.Jdbi;

import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public final class JdbiArticleRepository implements ArticleRepository {
    private final Jdbi jdbi;

    public JdbiArticleRepository(final @NotNull Jdbi jdbi) {
        this.jdbi = jdbi;
        this.jdbi.registerRowMapper(TopicId.class, new TopicIdMapper());
        this.jdbi.registerRowMapper(WebsiteId.class, new WebsiteIdMapper());
    }

    public JdbiArticleRepository() {
        this(JdbiProvider.get());
    }


    @Override
    public List<ArticleData> findAll() {
        return jdbi.inTransaction(handle ->
            handle.createQuery("select * from articles")
                .map((rs, ctx) -> new ArticleData(
                    rs.getString("title"), // NOPMD
                    rs.getString("url"), // NOPMD
                    rs.getTimestamp("created_at"), // NOPMD
                    rs.getLong("topic_id"), // NOPMD
                    rs.getLong("website_id")) // NOPMD
                )
                .list()
        );
    }

    @Override
    public Optional<ArticleData> findById(final @NotNull ArticleId articleId) {
        return jdbi.inTransaction( handle ->
                handle.createQuery("SELECT * FROM articles WHERE article_id = :article_id")
                        .bind("article_id", articleId.value()) // NOPMD
                        .map(
                            (rs, ctx) -> new ArticleData(
                                rs.getString("title"),
                                rs.getString("url"),
                                rs.getTimestamp("created_at"),
                                rs.getLong("topic_id"),
                                rs.getLong("website_id")
                            )
                        )
                        .findFirst()
        );
    }

    @Override
    public List<ArticleData> getUserArticles(final UserId userId) {
        return jdbi.inTransaction(handle ->
            handle.createQuery("with user_articles as (select * from user_articles\n" +
                    "where user_id = :user_id)\n" +
                    "\n" +
                    "select * from articles\n" +
                    "where article_id in (select article_id from user_articles);")
                .bind("user_id", userId.value())
                .map((rs, ctx) -> new ArticleData(
                    rs.getString("title"),
                    rs.getString("url"),
                    rs.getTimestamp("created_at"),
                    rs.getLong("topic_id"),
                    rs.getLong("website_id")
                ))
                .list()
        );
    }

    @Override
    public @NotNull Article create(final @NotNull Article article) {
        return jdbi.inTransaction(handle -> article.toBuilder()
            .id(handle.createUpdate(
                    "INSERT INTO articles (title, url, created_at, topic_id, website_id) " +
                        "VALUES (:title, :url, :created_at, :topic_id, :website_id)"
                )
                .bind("title", article.getTitle())
                .bind("url", article.getUrl())
                .bind("created_at", article.getCreatedAt())
                .bind("topic_id", article.getTopicId().value())
                .bind("website_id", article.getWebsiteId().value())
                .executeAndReturnGeneratedKeys("article_id")
                .mapTo(ArticleId.class)
                .one())
            .build()
        );
    }

    @Override
    public void update(final @NotNull Article article) {
        if (article.getId() == null) {
            throw new ArticleNotFoundException(null);
        }

        jdbi.useTransaction(handle ->
            handle.createUpdate("UPDATE articles SET title = :title, url = :url, " +
                            "created_at = :created_at, topic_id = :topic_id, website_id = :website_id " +
                            "WHERE article_id = :article_id")
               .bind("title", article.getTitle())
               .bind("url", article.getUrl())
               .bind("created_at", article.getCreatedAt())
               .bind("topic_id", article.getTopicId().value())
               .bind("website_id", article.getWebsiteId().value())
               .bind("article_id", article.getId().value())
               .execute()
        );
    }

    @Override
    public void delete(final @NotNull ArticleId articleId) {
        jdbi.useTransaction(handle ->
            handle.createUpdate("DELETE FROM articles WHERE article_id = :article_id")
                    .bind("article_id", articleId.value())
                    .execute()
        );
    }

//    @Override
//    public List<Article> getAllUnknown(final UserId userId) {
//        final String getQuery = "SELECT * FROM articles " +
//                "WHERE topic_id IN (SELECT topic_id FROM users WHERE user_id = :user_id AND " +
//                ":user_id NOT IN (SELECT article_id FROM user_articles WHERE user_id - :user_id)) AND " +
//                "website_id IN (SELECT website_id FROM users WHERE user_id = :user_id)";
//
//        final List<Article> articles = jdbi.inTransaction(handle ->
//                handle.createQuery(getQuery)
//                        .bind("user_id", userId)
//                        .mapTo(Article.class)
//                        .collectIntoList()
//        );
//
//        jdbi.inTransaction(handle ->
//                handle.createUpdate(
//                        "INSERT INTO user_articles (user_id, article_id) " +
//                                "SELECT user_id, article_id FROM " + getQuery
//                ).execute()
//        );
//
//        return articles;
//    }
}
