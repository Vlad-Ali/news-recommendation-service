package org.hsse.news.database.mapper;

import org.hsse.news.database.entity.UserEntity;
import org.hsse.news.database.user.models.UserDto;
import org.hsse.news.database.user.models.UserId;


import java.util.UUID;

final public class UserMapper {

    private UserMapper(){}

    public static UserDto toUser(final UserEntity userEntity){
        final UUID id = userEntity.getId();
        final String email = userEntity.getEmail();
        final String username = userEntity.getUsername();
        final String password = userEntity.getPassword();
        return new UserDto(new UserId(id), email, password, username);
    }

    public static UserEntity toUserEntity(final UserDto userDto){
        return new UserEntity(userDto.email(), userDto.password(), userDto.username());
    }
}
