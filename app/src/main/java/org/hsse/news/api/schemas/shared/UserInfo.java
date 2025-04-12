package org.hsse.news.api.schemas.shared;

import io.swagger.v3.oas.annotations.media.Schema;
import org.jetbrains.annotations.NotNull;

@Schema(name = "UserInfo", description = "Данные о пользователе (без ID и пароля)")
public record UserInfo(
        @Schema(description = "E-mail", example = "ivanov@yandex.ru")
        @NotNull String email,

        @Schema(description = "Имя пользователя", example = "Ivanov52")
        @NotNull String username) {}
