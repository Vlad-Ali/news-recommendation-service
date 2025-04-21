package org.hsse.news.api.controllers.role;

import org.hsse.news.database.role.RolesService;
import org.hsse.news.database.role.exception.RoleNotFoundException;
import org.hsse.news.database.role.model.RolesDto;
import org.hsse.news.database.role.model.UserRoleDto;
import org.hsse.news.database.user.UserService;
import org.hsse.news.database.user.models.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<UserRoleDto> updateRoles(@RequestBody RolesDto rolesDto){
        final UserId userId = getCurrentUserId();
        final UserRoleDto userRoleDto = rolesService.updateRoles(userId, rolesDto.roles());
        LOG.debug("Successfully update roles for {}", userId);
        return ResponseEntity.ok(userRoleDto);
    }

    @ExceptionHandler(RoleNotFoundException.class)
    public ErrorResponse handleRoleNotFound(RoleNotFoundException ex){
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
