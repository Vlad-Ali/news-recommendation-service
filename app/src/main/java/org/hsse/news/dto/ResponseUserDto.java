package org.hsse.news.dto;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hsse.news.database.entity.UserEntity;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

@Builder(toBuilder = true)
@Data
@NoArgsConstructor
public class ResponseUserDto {

    private @NotNull UUID id;
    private @NotNull String email;
    private @NotNull String password;
    private @NotNull String username;
    private @NotNull Long chatId;

    public ResponseUserDto(final @NotNull UUID id,final @NotNull String email,final @NotNull String password,final @NotNull String username,final @NotNull Long chatId){
        this.id = id;
        this.email = email;
        this.password = password;
        this.username = username;
        this.chatId = chatId;
    }

    public static ResponseUserDto fromUserEntity(final @NotNull UserEntity userEntity) {
        return new ResponseUserDto(
                userEntity.getId(),
                userEntity.getEmail(),
                userEntity.getPassword(),
                userEntity.getUsername(),
                userEntity.getChatId()
        );
    }
}
