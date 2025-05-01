package org.hsse.news.database.role.model;

import org.hsse.news.database.user.models.UserDto;

import java.util.Set;

public record UserRoleDto(UserDto user, Set<Role> roles) {}
