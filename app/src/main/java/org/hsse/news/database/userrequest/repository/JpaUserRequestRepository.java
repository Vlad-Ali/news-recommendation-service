package org.hsse.news.database.userrequest.repository;

import org.hsse.news.database.entity.UserRequestEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUserRequestRepository extends JpaRepository<UserRequestEntity, Long> {
    @Query(value = """
            SELECT max(request_time) FROM user_requests
            WHERE user_id = :userId
            """, nativeQuery = true)
    Optional<Instant> getLastRequestTimeByUserId(@Param("userId") UUID userId);

    @Query(value = """
            SELECT * FROM user_requests
            WHERE user_id = :userId AND url = :url
            """, nativeQuery = true)
    Optional<UserRequestEntity> getRequestByUserIdAndUrl(@Param("userId") UUID userId, @Param("url") String url);

    @Query(value = """
            SELECT url FROM user_requests
            GROUP BY url
            ORDER BY count(*) DESC
            LIMIT 5
            """, nativeQuery = true)
    List<String> getTopOfRequests();

    @Query(value = """
            SELECT count(*) FROM user_requests
            WHERE url = :url
            """, nativeQuery = true)
    Optional<Integer> getCountOfRequest(@Param("url") String url);

    @Modifying
    @Query(value = """
            DELETE FROM user_requests
            WHERE url = :url
            """, nativeQuery = true)
    void deleteAllByUrl(@Param("url") String url);
}
