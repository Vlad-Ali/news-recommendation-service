package org.hsse.news.database.role.exception;

import org.hsse.news.database.role.model.Role;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String message) {
        super(message);
    }
    public RoleNotFoundException(Role role){super(role + "not found");}
}
