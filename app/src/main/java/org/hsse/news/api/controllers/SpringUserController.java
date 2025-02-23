package org.hsse.news.api.controllers;

import lombok.extern.slf4j.Slf4j;
import org.hsse.news.api.authorizers.Authorizer;
import org.hsse.news.api.schemas.request.user.UserPasswordChangeRequest;
import org.hsse.news.api.schemas.request.user.UserRegisterRequest;
import org.hsse.news.api.schemas.shared.UserInfo;
import org.hsse.news.api.util.ControllerUtil;
import org.hsse.news.database.user.UserService;
import org.hsse.news.database.user.exceptions.EmailConflictException;
import org.hsse.news.database.user.exceptions.InvalidCurrentPasswordException;
import org.hsse.news.database.user.exceptions.SameNewPasswordException;
import org.hsse.news.database.user.models.User;
import org.hsse.news.database.user.models.UserId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Optional;

@RestController
@RequestMapping("/users")
@Slf4j
public class SpringUserController {
    private final UserService userService;

    public SpringUserController(final UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    ResponseEntity<String> register(@RequestBody UserRegisterRequest request) {
        ControllerUtil.logRequest(request, path);

        try {
            final User user = userService.register(
                    new User(request.email(), request.password(), request.username()));
            log.debug("Registered user with id = {}", user.id());
            return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentServletMapping()
                    .path("/{id}").buildAndExpand(user.id()).toUri()).build();
        } catch (EmailConflictException e) {
            return processEmailConflict(e);
        }
    }

    @GetMapping
    private ResponseEntity<UserInfo> get() {
        ControllerUtil.logRequest(request, path);

        final UserId userId = (UserId) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        final Optional<User> userOptional = userService.findById(userId);
        if (userOptional.isEmpty()) {
            log.warn("User not found for id = {}", userId);
            return ResponseEntity.notFound().build();
        }

        log.debug("Successfully found user by id = {}", userOptional.get().id());

        return ResponseEntity.ok(new UserInfo(
                userOptional.get().email(),
                userOptional.get().username()
        ));
    }

    @PutMapping
    private ResponseEntity<String> update(@RequestBody UserInfo userInfo) {
        ControllerUtil.logRequest(request, path);

        final UserId userId = (UserId) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        try {
            userService.update(userId, userInfo.email(), userInfo.username());

            log.debug("Successfully updated user with id = {}", userId);
            return ResponseEntity.noContent().build();
        } catch (EmailConflictException e) {
            return processEmailConflict(e);
        }
    }

    @PutMapping("/password")
    private ResponseEntity<String> changePassword(@RequestBody UserPasswordChangeRequest request) {
        ControllerUtil.logRequest(request, path);

        final UserId userId = (UserId) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();

        try {
            userService.updatePassword(userId, request.currentPassword(), request.newPassword());

            log.debug("Successfully updated password for user with id = {}", userId);
            return ResponseEntity.noContent().build();
        } catch (SameNewPasswordException e) {
            log.debug("Same new password for user with id = {}", userId);
            return ResponseEntity.status(HttpStatus.ALREADY_REPORTED)
                    .body("Valid current password, new password matches it");
        } catch (InvalidCurrentPasswordException e) {
            log.debug("Invalid current password for user with id = {}", userId);
            return ResponseEntity.status(HttpStatus.PRECONDITION_FAILED)
                    .body("Invalid current password");
        }
    }

    private ResponseEntity<String> processEmailConflict(final EmailConflictException e) {
        log.debug("Email conflict: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body("Email already exists");
    }
}
