package org.hsse.news.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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

    private static final Logger LOG = LoggerFactory.getLogger(UserController.class);
    private UserService userService;
    private JwtService jwtService;

    @PostMapping("/register")
    @Operation(summary = "Зарегистрировать пользователя")
    @ApiResponse(responseCode = "200",
            description = "Токен для доступа в аккаунт нового пользователя")
    public ResponseEntity<String> register(
            final @RequestBody UserRegisterRequest userRegisterRequest) {
        final UserDto userDto = userService.register(
                new UserDto(userRegisterRequest.email(),
                        userRegisterRequest.password(),
                        userRegisterRequest.username(), (long) (Math.random() * Long.MAX_VALUE)));
        LOG.debug("Registered user with id = {}", userDto.id());
        return ResponseEntity.ok(jwtService.generateToken(userDto.id()));
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Войти в существующий аккаунт")
    @ApiResponse(responseCode = "200", description = "Токен для входа в существующий аккаунт")
    public ResponseEntity<String> signIn(
            final @RequestBody AuthenticationCredentials credentials) {
        LOG.debug("Attempting to authorize user with email = {}", credentials.email());

        final Optional<UserId> userIdOptional = userService.authenticate(credentials);
        if (userIdOptional.isEmpty()) {
            LOG.debug("Failed to authorize user with email = {}", credentials.email());
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        LOG.debug("Successfully authorized user with id = {}", userIdOptional.get());
        return ResponseEntity.ok(jwtService.generateToken(userIdOptional.get()));
    }


    @GetMapping
    @Operation(summary = "Получить данные о текущем пользователя (по токену авторизации)")
    @ApiResponse(responseCode = "200", description = "Данные пользователя")
    public ResponseEntity<UserInfo> get() {
        final UserId userId = getCurrentUserId();
        final Optional<UserDto> userOptional = userService.findById(userId);
        if (userOptional.isEmpty()) {
            LOG.warn("User not found for id = {}", userId);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,
                    "User from authorization token not found");
        }

        LOG.debug("Successfully found user by id = {}", userOptional.get().id());

        return ResponseEntity.ok(new UserInfo(
                userOptional.get().email(),
                userOptional.get().username()
        ));
    }

    @PutMapping
    @Operation(summary = "Изменить данные текущего пользователя")
    @ApiResponse(responseCode = "204", description = "Данные успешно изменены")
    public ResponseEntity<Void> update(final @RequestBody UserInfo userInfo) {
        final UserId userId = getCurrentUserId();
        userService.update(userId, userInfo.email(), userInfo.username());

        LOG.debug("Successfully updated user with id = {}", userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/password")
    @Operation(summary = "Сменить пароль")
    @ApiResponse(responseCode = "204", description = "Пароль успешно изменён")
    public ResponseEntity<Void> changePassword(
            final @RequestBody UserPasswordChangeRequest userPasswordChangeRequest) {
        final UserId userId = getCurrentUserId();
        userService.updatePassword(userId,
                userPasswordChangeRequest.currentPassword(),
                userPasswordChangeRequest.newPassword());

        LOG.debug("Successfully updated password for user with id = {}", userId);
        return ResponseEntity.noContent().build();
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ErrorResponse handleResponseStatus(final ResponseStatusException ex){
        LOG.debug("Response status: {}", ex.getMessage());
        return ErrorResponse.create(ex, ex.getStatusCode(), ex.getMessage());
    }

    @ExceptionHandler(EmailConflictException.class)
    public ErrorResponse handleEmailConflict(final EmailConflictException e) {
        LOG.debug("Email conflict: {}", e.getMessage());
        return ErrorResponse.create(e, HttpStatus.CONFLICT, "Email already exists");
    }

    @ExceptionHandler(SameNewPasswordException.class)
    public ErrorResponse handleSameNewPassword(final SameNewPasswordException e) {
        LOG.debug("Same new password for user with id = {}", getCurrentUserId());
        return ErrorResponse.create(e, HttpStatus.ALREADY_REPORTED,
                "Valid current password, new password matches it");
    }

    @ExceptionHandler(InvalidCurrentPasswordException.class)
    public ErrorResponse handleInvalidCurrentPassword(final InvalidCurrentPasswordException e) {
        LOG.debug("Invalid current password for user with id = {}", getCurrentUserId());
        return ErrorResponse.create(e, HttpStatus.PRECONDITION_FAILED,
                "Invalid current password");
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
