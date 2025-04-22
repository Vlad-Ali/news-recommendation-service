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
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RolesService {
    private final JpaRolesRepository roleRepository;
    private final JpaUsersRepository usersRepository;
    private static final Logger LOG = LoggerFactory.getLogger(RolesService.class);

    public RolesService(final JpaRolesRepository roleRepository,final JpaUsersRepository usersRepository) {
        this.roleRepository = roleRepository;
        this.usersRepository = usersRepository;
    }

    public UserRoleDto updateRoles(final UserId userId,final Set<Role> roles){
        LOG.debug("Method updateRoles called");
        final UserEntity userEntity = usersRepository.findById(userId.value()).orElseThrow(() -> new UserNotFoundException(userId));
        userEntity.getUserRoles().clear();
        for (final Role role : roles){
            final RoleEntity roleEntity = roleRepository.findByRole(role.name()).orElseThrow(() -> new RoleNotFoundException(role+" not found"));
            userEntity.assignRole(roleEntity);
        }
        usersRepository.save(userEntity);
        return new UserRoleDto(userEntity.toUserDto(), roles);
    }

    public Set<Role> getUserRoles(final UserId userId){
        final UserEntity userEntity = usersRepository.findById(userId.value()).orElseThrow(() -> new UserNotFoundException(userId));
        return userEntity.getRoles();
    }

}
