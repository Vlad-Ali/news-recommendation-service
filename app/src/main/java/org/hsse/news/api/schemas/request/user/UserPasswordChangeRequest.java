package org.hsse.news.api.schemas.request.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import org.hsse.news.util.PasswordUtil;
import org.jetbrains.annotations.NotNull;

@Schema(name = "UserPasswordChangeRequest", description = "Запрос на смену пароля")
public record UserPasswordChangeRequest(
        @Schema(description = "Старый пароль", example = "passwordHash")
        @JsonProperty("current_password")
        @NotNull
        String currentPassword,

        @Schema(description = "Новый пароль", example = "T0p1GGP@ssw)rd!")
        @JsonProperty("new_password")
        @NotNull
        String newPassword
) {
    public UserPasswordChangeRequest(
            @JsonProperty("current_password") final @NotNull String currentPassword,
            @JsonProperty("new_password") final @NotNull String newPassword
    ) {
        this.currentPassword = PasswordUtil.hashPassword(currentPassword);
        this.newPassword = PasswordUtil.hashPassword(newPassword);
    }
}
