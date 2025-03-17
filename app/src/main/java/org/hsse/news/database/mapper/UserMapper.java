package org.hsse.news.database.mapper;

import org.hsse.news.database.entity.UserEntity;
import org.hsse.news.database.user.models.User;
import org.hsse.news.database.user.models.UserId;


import java.util.UUID;

final public class UserMapper {

    private UserMapper(){}

    public static User toUser(final UserEntity userEntity){
        final UUID id = userEntity.getId();
        final String email = userEntity.getEmail();
        final String username = userEntity.getUsername();
        final String password = userEntity.getPassword();
        return new User(new UserId(id), email, password, username);
    }

    public static UserEntity toUserEntity(final User user){
        return new UserEntity(user.email(), user.password(), user.username());
    }
}
