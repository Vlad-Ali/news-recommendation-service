package org.hsse.news.database.website.repositories;

import org.hsse.news.api.schemas.shared.WebsiteInfo;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.website.exceptions.WebsiteAlreadyExistsException;
import org.hsse.news.database.website.exceptions.WebsiteNotFoundException;
import org.hsse.news.database.website.models.Website;
import org.hsse.news.database.website.models.WebsiteId;
import org.hsse.news.util.JdbiProvider;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.UnableToExecuteStatementException;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbiWebsiteRepository implements WebsiteRepository {
    private final Jdbi jdbi;

    public JdbiWebsiteRepository(final Jdbi jdbi) { this.jdbi = jdbi; }

    public JdbiWebsiteRepository() {
        this(JdbiProvider.get());
    }

    @Override
    public Optional<WebsiteInfo> findById(final @NotNull WebsiteId websiteId) {
        return jdbi.inTransaction(handle ->
                handle.createQuery("SELECT * FROM websites WHERE website_id = :website_id")
                        .bind("website_id", websiteId.value()) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                        .map((rs, ctx) -> new WebsiteInfo(rs.getLong("website_id"), rs.getString("url"), rs.getString("description")))// NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                        .findFirst()
        );
    }

    @Override
    public @NotNull Website create(final @NotNull Website website) {
        return jdbi.inTransaction(handle -> {
            try {
                return website.initializeWithId(handle.createUpdate(
                                "INSERT INTO websites (url, description, creator_id) " +
                                        "VALUES (:url, :description, :creator_id)"
                        )
                        .bind("url", website.url()) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                        .bind("description", website.description()) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                        .bind("creator_id", website.creatorId().value()) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                        .executeAndReturnGeneratedKeys("website_id") // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                        .mapTo(WebsiteId.class)
                        .one()
                );
            } catch (UnableToExecuteStatementException e) {
                throw new WebsiteAlreadyExistsException(website.id(), website.url()); // NOPMD - suppressed PreserveStackTrace - irrelevant
            }
        });
    }

    @Override
    public @NotNull List<Website> getAll() {
        return jdbi.inTransaction(handle ->
                handle.createQuery("SELECT * FROM websites WHERE creator_id IS NULL")
                        .mapTo(Website.class)
                        .list()
        );
    }

    @Override
    public @NotNull List<WebsiteInfo> findSubscribedWebsitesByUserId(final @NotNull UserId creatorId) {
    return jdbi.inTransaction(
        handle ->
            handle
                .createQuery(
                    "SELECT websites.website_id, url, description FROM websites INNER JOIN user_websites ON websites.website_id = user_websites.website_id\n"
                        + "WHERE user_websites.user_id = :user_id")
                .bind(
                    "user_id", // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                    creatorId.value()) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                .map((rs, ctx) -> new WebsiteInfo(rs.getLong("website_id"), rs.getString("url"), rs.getString("description")))
                .list());
    }

    @Override
    public @NotNull List<WebsiteInfo> findUnSubscribedWebsitesByUserId(final @NotNull UserId creatorId) {
    return jdbi.inTransaction(
        handle ->
            handle
                .createQuery(
                    "SELECT w.*\n"
                        + "FROM websites w\n"
                        + "LEFT JOIN user_websites uw ON w.website_id = uw.website_id AND uw.user_id = :user_id\n"
                        + "WHERE uw.website_id IS NULL")
                .bind(
                    "user_id",
                    creatorId.value()) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                .map(
                    (rs, ctx) ->
                        new WebsiteInfo(
                            rs.getLong("website_id"),
                            rs.getString("url"),
                            rs.getString("description")))
                .list());
    }

    @Override
    public void update(final @NotNull Website website) {
        if (website.id() == null) {
            throw new WebsiteNotFoundException("Website with id = null does not exist");
        }

        jdbi.useTransaction(handle -> {
            try {
                handle.createUpdate("UPDATE websites SET url = :url, description = :description WHERE website_id = :website_id")
                        .bind("url", website.url()) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                        .bind("description", website.description()) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                        .bind("website_id", website.id().value()) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                        .execute();
            } catch (UnableToExecuteStatementException e) {
                throw new WebsiteAlreadyExistsException(website.id(), website.url()); // NOPMD - suppressed PreserveStackTrace - irrelevant
            }
        });
    }

    @Override
    public void delete(final @NotNull WebsiteId websiteId, final @NotNull UserId creatorId) {
        jdbi.useTransaction(handle ->
                handle.createUpdate("DELETE FROM websites WHERE website_id = :website_id AND creator_id = :creator_id")
                        .bind("website_id", websiteId.value()) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                        .bind("creator_id", creatorId.value()) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                        .execute()
        );
    }

    @Override
    public void updateSubscribedWebsites(final @NotNull List<WebsiteId> websites, final @NotNull UserId userId) {
        jdbi.useTransaction(handle -> {
            handle.createUpdate("DELETE FROM user_websites WHERE user_id = :user_id")
                    .bind("user_id", userId.value()) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                    .execute();
            for (final WebsiteId website : websites) {
                handle.createUpdate("INSERT INTO user_websites (user_id, website_id) VALUES (:user_id, :website_id)")
                        .bind("user_id", userId.value()) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                        .bind("website_id", website.value()) // NOPMD - suppressed AvoidDuplicateLiterals - irrelevant
                        .execute();
            }
        });
    }
}
