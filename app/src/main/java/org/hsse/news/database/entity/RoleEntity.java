package org.hsse.news.database.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import org.hsse.news.database.role.model.Role;

import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@Table(name = "roles", uniqueConstraints = {@UniqueConstraint(columnNames = {"role"})})
public class RoleEntity {
    @Id
    @Column(name = "role_id", updatable = false, nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long roleId;

    @Column(name = "role")
    @NotNull
    private String role;

    @ManyToMany(mappedBy = "userRoles", fetch = FetchType.EAGER)
    private Set<UserEntity> users = new HashSet<>();

    protected RoleEntity(){}

    public RoleEntity(final Role role){
        this.role = role.name();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof RoleEntity roleEntity)) {
            return false;
        }
        return roleId != null && roleId.equals(roleEntity.roleId);
    }

    @Override
    public int hashCode() {
        return RoleEntity.class.hashCode();
    }


}
