package org.hsse.news.api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.hsse.news.api.schemas.request.user.UserPasswordChangeRequest;
import org.hsse.news.api.schemas.request.user.UserRegisterRequest;
import org.hsse.news.api.schemas.shared.UserInfo;
import org.hsse.news.api.util.ControllerUtil;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Optional;

@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name="User API", description = "Управление текущим пользователем")
public class UserController {
    private final UserService userService;
    private final JwtService jwtService;

    public UserController(final UserService userService,
                                final JwtService jwtService) {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    @Operation(summary = "Зарегистрировать пользователя")
    @ApiResponse(responseCode = "200",
            description = "Токен для доступа в аккаунт нового пользователя")
    @ApiResponse(responseCode = "409", description = "Аккаунт с таким e-mail уже зарегистрирован")
    public ResponseEntity<String> register(@RequestBody UserRegisterRequest userRegisterRequest,
                                    HttpServletRequest httpServletRequest) {
        ControllerUtil.logRequest(httpServletRequest);

        try {
            final User user = userService.register(
                    new User(userRegisterRequest.email(),
                            userRegisterRequest.password(),
                            userRegisterRequest.username()));

            assert user.id() != null;
            log.debug("Registered user with id = {}", user.id());
            return ResponseEntity.ok(jwtService.generateToken(user.id()));
        } catch (EmailConflictException e) {
            return processEmailConflict(e);
        }
    }

    @PostMapping("/sign-in")
    @Operation(summary = "Войти в существующий аккаунт")
    @ApiResponse(responseCode = "200", description = "Токен для входа в существующий аккаунт")
    @ApiResponse(responseCode = "401",
            description = "Ошибка авторизации")
    public ResponseEntity<String> signIn(@RequestBody AuthenticationCredentials credentials,
                                         HttpServletRequest request) {
        ControllerUtil.logRequest(request);

        log.debug("Attempting to authorize user with email = {}", credentials.email());

        final Optional<UserId> userIdOptional = userService.authenticate(credentials);
        if (userIdOptional.isEmpty()) {
            log.debug("Failed to authorize user with email = {}", credentials.email());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        log.debug("Successfully authorized user with id = {}", userIdOptional.get());
        return ResponseEntity.ok(jwtService.generateToken(userIdOptional.get()));
    }

    @GetMapping
    @Operation(summary = "Получить данные о текущем пользователя (по токену авторизации)")
    @ApiResponse(responseCode = "200", description = "Данные пользователя")
    @ApiResponse(responseCode = "401", description = "Нужно авторизоваться", content = {@Content})
    public ResponseEntity<UserInfo> get(HttpServletRequest httpServletRequest) {
        ControllerUtil.logRequest(httpServletRequest);

        final UserId userId = getCurrentUserId().orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

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
    @Operation(summary = "Изменить данные текущего пользователя")
    @ApiResponse(responseCode = "204", description = "Данные успешно изменены", content = {@Content})
    @ApiResponse(responseCode = "401", description = "Нужно авторизоваться", content = {@Content})
    @ApiResponse(responseCode = "409", description = "Аккаунт с таким e-mail уже зарегистрирован")
    public ResponseEntity<String> update(@RequestBody UserInfo userInfo,
                                          HttpServletRequest httpServletRequest) {
        ControllerUtil.logRequest(httpServletRequest);

        final UserId userId = getCurrentUserId().orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            userService.update(userId, userInfo.email(), userInfo.username());

            log.debug("Successfully updated user with id = {}", userId);
            return ResponseEntity.noContent().build();
        } catch (EmailConflictException e) {
            return processEmailConflict(e);
        }
    }

    @PutMapping("/password")
    @Operation(summary = "Сменить пароль")
    @ApiResponse(responseCode = "204", description = "Пароль успешно изменён", content = {@Content})
    @ApiResponse(responseCode = "401", description = "Нужно авторизоваться", content = {@Content})
    @ApiResponse(responseCode = "208", description = "Новый пароль совпадает со старым")
    @ApiResponse(responseCode = "412", description = "Старый пароль неправильно указан")
    public ResponseEntity<String> changePassword(
            @RequestBody UserPasswordChangeRequest userPasswordChangeRequest,
            HttpServletRequest httpServletRequest) {
        ControllerUtil.logRequest(httpServletRequest);

        final UserId userId = getCurrentUserId().orElse(null);
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            userService.updatePassword(userId,
                    userPasswordChangeRequest.currentPassword(),
                    userPasswordChangeRequest.newPassword());

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

    private Optional<UserId> getCurrentUserId() {
        Object principal = SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        if (principal.equals("anonymousUser")) {
            return Optional.empty();
        }
        return Optional.of((UserId) principal);
    }
}
