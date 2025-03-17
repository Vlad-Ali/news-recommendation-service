package org.hsse.news.database.website.repositories;

import org.hsse.news.database.entity.WebsiteEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaWebsitesRepository extends JpaRepository<WebsiteEntity, Long> {
    @Query(value = "SELECT websites.website_id, url, description, websites.creator_id FROM websites INNER JOIN user_websites ON websites.website_id = user_websites.website_id\n"
            + "WHERE user_websites.user_id = :user_id", nativeQuery = true)
    List<WebsiteEntity> findSubscribedWebsitesByUserId(@Param("user_id") UUID userId);
    @Query(value = "SELECT w.*\n"
            + "FROM websites w\n"
            + "LEFT JOIN user_websites uw ON w.website_id = uw.website_id AND uw.user_id = :user_id\n"
            + "WHERE uw.website_id IS NULL", nativeQuery = true)
    List<WebsiteEntity> findUnSubscribedWebsitesByUserId(@Param("user_id") UUID userId);
    Optional<WebsiteEntity> findByUrl(String url);
}
