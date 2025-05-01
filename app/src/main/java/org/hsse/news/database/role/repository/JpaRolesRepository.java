package org.hsse.news.database.role.repository;

import org.hsse.news.database.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JpaRolesRepository extends JpaRepository<RoleEntity, Long> {
    Optional<RoleEntity> findByRole(String role);
}
