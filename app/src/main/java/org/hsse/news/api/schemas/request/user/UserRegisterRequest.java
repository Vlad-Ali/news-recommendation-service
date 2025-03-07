package org.hsse.news.api.schemas.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;

@Schema(name = "UserRegisterRequest", description = "Запрос на регистрацию пользователя")
public record UserRegisterRequest(
        @Schema(description = "E-mail", example = "ivanov@yandex.ru")
        @NotNull
        String email,

        @Schema(description = "Пароль", example = "T0p1GGP@ssw)rd!")
        @NotNull
        String password,

        @Schema(description = "Имя пользователя", example = "Ivanov52")
        @NotNull
        String username
) {}
