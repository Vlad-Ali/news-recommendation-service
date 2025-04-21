package org.hsse.news.database.role;

import org.hsse.news.database.entity.RoleEntity;
import org.hsse.news.database.entity.UserEntity;
import org.hsse.news.database.role.exception.RoleNotFoundException;
import org.hsse.news.database.role.model.Role;
import org.hsse.news.database.role.model.UserRoleDto;
import org.hsse.news.database.role.repository.JpaRolesRepository;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
import org.hsse.news.database.user.models.UserId;
import org.hsse.news.database.user.repositories.JpaUsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class RolesService {
    private final JpaRolesRepository roleRepository;
    private final JpaUsersRepository usersRepository;
    private static final Logger LOG = LoggerFactory.getLogger(RolesService.class);

    public RolesService(JpaRolesRepository roleRepository, JpaUsersRepository usersRepository) {
        this.roleRepository = roleRepository;
        this.usersRepository = usersRepository;
    }

    public UserRoleDto updateRoles(final UserId userId,final Set<Role> roles){
        final UserEntity userEntity = usersRepository.findById(userId.value()).orElseThrow(() -> new UserNotFoundException(userId));
        userEntity.getRoles().clear();
        for (final Role role : roles){
            final RoleEntity roleEntity = roleRepository.findByRole(role.name()).orElseThrow(() -> new RoleNotFoundException(role+" not found"));
            userEntity.assignRole(roleEntity);
        }
        return new UserRoleDto(userEntity.toUserDto(), roles);
    }

    public Set<Role> getUserRoles(final UserId userId){
        final UserEntity userEntity = usersRepository.findById(userId.value()).orElseThrow(() -> new UserNotFoundException(userId));
        return userEntity.getRoles();
    }

}
