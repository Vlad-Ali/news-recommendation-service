package org.hsse.news.database.user.repositories;


import org.hsse.news.database.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaUsersRepository extends JpaRepository<UserEntity, UUID> {
    @Query(value = "SELECT * FROM users WHERE user_id <> :id AND email = :email", nativeQuery = true)
    Optional<UserEntity> findByEmailAndNotId(@Param("id") UUID id, @Param("email") String email);

    Optional<UserEntity> findByEmail(String email);

    Optional<UserEntity> findByEmailAndPassword(String email, String password);
}
