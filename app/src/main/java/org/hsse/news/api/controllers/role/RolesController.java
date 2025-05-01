package org.hsse.news.api.controllers.role;

import org.hsse.news.database.role.RolesService;
import org.hsse.news.database.role.exception.RoleNotFoundException;
import org.hsse.news.database.role.model.Role;
import org.hsse.news.database.role.model.RolesDto;
import org.hsse.news.database.role.model.UserRoleDto;
import org.hsse.news.database.user.UserService;
import org.hsse.news.database.user.exceptions.UserNotFoundException;
import org.hsse.news.database.user.models.UserDto;
import org.hsse.news.database.user.models.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@RestController
@RequestMapping("/user/role")
public class RolesController {
    private final RolesService rolesService;
    private final UserService userService;
    private final static Logger LOG = LoggerFactory.getLogger(RolesController.class);

    public RolesController(final RolesService rolesService,final UserService userService) {
        this.rolesService = rolesService;
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserRoleDto> updateRoles(@RequestBody final RolesDto rolesDto){
        final UserId userId = getCurrentUserId();
        final UserRoleDto userRoleDto = rolesService.updateRoles(userId, rolesDto.roles());
        LOG.debug("Successfully update roles for {}", userId);
        return ResponseEntity.ok(userRoleDto);
    }

    @GetMapping
    public ResponseEntity<UserRoleDto> getRoles(){
        final UserId userId = getCurrentUserId();
        final Set<Role> roles = rolesService.getUserRoles(userId);
        final UserDto userDto = userService.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        return ResponseEntity.ok(new UserRoleDto(userDto, roles));
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ErrorResponse handleRoleNotFound(final RoleNotFoundException ex){
        LOG.error("Role not found: {}", ex.getMessage());
        return ErrorResponse.create(ex, HttpStatus.NOT_FOUND, "Role not found");
    }

    private UserId getCurrentUserId() {
        final Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if ("anonymousUser".equals(principal)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Authorization required");
        }
        return (UserId) principal;
    }
}
