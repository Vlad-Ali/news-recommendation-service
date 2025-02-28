package org.hsse.news.database.user.models;

import io.swagger.v3.oas.annotations.media.Schema;
import org.hsse.news.util.PasswordUtil;
import org.jetbrains.annotations.NotNull;

@Schema(name="AuthenticationCredentials", description = "Данные для аутентификации")
public record AuthenticationCredentials(
        @Schema(description="E-mail", example = "ivanov@yandex.ru")
        @NotNull String email,

        @Schema(description="Пароль", example = "T0p1GGP@ssw)rd!")
        @NotNull String password) {

        public AuthenticationCredentials(final @NotNull String email,final @NotNull String password){
                this.email = email;
                this.password = PasswordUtil.hashPassword(password);
        }
}
