package org.hsse.news.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hsse.news.api.schemas.request.user.UserPasswordChangeRequest;
import org.hsse.news.api.schemas.request.user.UserRegisterRequest;
import org.hsse.news.api.schemas.shared.UserInfo;
import org.hsse.news.database.jwt.JwtService;
import org.hsse.news.database.user.UserService;
import org.hsse.news.database.user.exceptions.EmailConflictException;
import org.hsse.news.database.user.exceptions.InvalidCurrentPasswordException;
import org.hsse.news.database.user.exceptions.SameNewPasswordException;
import org.hsse.news.database.user.models.AuthenticationCredentials;
import org.hsse.news.database.user.models.User;
import org.hsse.news.database.user.models.UserId;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@RestController
@RequestMapping("/user")
@AllArgsConstructor
@Slf4j
@Tag(name = "User API", description = "Управление текущим пользователем")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/register")
    @Operation(summary = "Зарегистрировать пользователя")
    @ApiResponse(responseCode = "200",
            description = "Токен для доступа в аккаунт нового пользователя")
    public ResponseEntity<String> register(@RequestBody UserRegisterRequest userRegisterRequest,
                                           HttpServletRequest httpServletRequest) {
        final User user = userService.register(
                new User(userRegisterRequest.email(),
                        userRegisterRequest.password(),
                        userRegisterRequest.username()));

        assert user.id() != null;
        log.debug("Registered user with id = {}", user.id());
        return ResponseEntity.ok(jwtService.generateToken(user.id()));
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Войти в существующий аккаунт")
    @ApiResponse(responseCode = "200", description = "Токен для входа в существующий аккаунт")
    public ResponseEntity<String> signIn(@RequestBody AuthenticationCredentials credentials,
                                         HttpServletRequest request) {
        log.debug("Attempting to authorize user with email = {}", credentials.email());

        final Optional<UserId> userIdOptional = userService.authenticate(credentials);
        if (userIdOptional.isEmpty()) {
            log.debug("Failed to authorize user with email = {}", credentials.email());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        log.debug("Successfully authorized user with id = {}", userIdOptional.get());
        return ResponseEntity.ok(jwtService.generateToken(userIdOptional.get()));
    }

    @GetMapping
    @Operation(summary = "Получить данные о текущем пользователя (по токену авторизации)")
    @ApiResponse(responseCode = "200", description = "Данные пользователя")
    public ResponseEntity<UserInfo> get(HttpServletRequest httpServletRequest) {
        final UserId userId = getCurrentUserId();
        final Optional<User> userOptional = userService.findById(userId);
        if (userOptional.isEmpty()) {
            log.warn("User not found for id = {}", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User from authorization token not found");
        }

        log.debug("Successfully found user by id = {}", userOptional.get().id());

        return ResponseEntity.ok(new UserInfo(
                userOptional.get().email(),
                userOptional.get().username()
        ));
    }

    @PutMapping
    @Operation(summary = "Изменить данные текущего пользователя")
    @ApiResponse(responseCode = "204", description = "Данные успешно изменены")
    public ResponseEntity<Void> update(@RequestBody UserInfo userInfo,
                                       HttpServletRequest httpServletRequest) {
        final UserId userId = getCurrentUserId();
        userService.update(userId, userInfo.email(), userInfo.username());

        log.debug("Successfully updated user with id = {}", userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password")
    @Operation(summary = "Сменить пароль")
    @ApiResponse(responseCode = "204", description = "Пароль успешно изменён")
    public ResponseEntity<Void> changePassword(
            @RequestBody UserPasswordChangeRequest userPasswordChangeRequest,
            HttpServletRequest httpServletRequest) {
        final UserId userId = getCurrentUserId();
        userService.updatePassword(userId,
                userPasswordChangeRequest.currentPassword(),
                userPasswordChangeRequest.newPassword());

        log.debug("Successfully updated password for user with id = {}", userId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(EmailConflictException.class)
    private ErrorResponse handleEmailConflict(final EmailConflictException e) {
        log.debug("Email conflict: {}", e.getMessage());
        return ErrorResponse.create(e, HttpStatus.CONFLICT, "Email already exists");
    }

    @ExceptionHandler(SameNewPasswordException.class)
    private ErrorResponse handleSameNewPassword(final SameNewPasswordException e) {
        log.debug("Same new password for user with id = {}", getCurrentUserId());
        return ErrorResponse.create(e, HttpStatus.ALREADY_REPORTED,
                "Valid current password, new password matches it");
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    private ErrorResponse handleInvalidCurrentPassword(final InvalidCurrentPasswordException e) {
        log.debug("Invalid current password for user with id = {}", getCurrentUserId());
        return ErrorResponse.create(e, HttpStatus.PRECONDITION_FAILED,
                "Invalid current password");
    }

    private UserId getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (principal.equals("anonymousUser")) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Authorization required");
        }
        return (UserId) principal;
    }
}
