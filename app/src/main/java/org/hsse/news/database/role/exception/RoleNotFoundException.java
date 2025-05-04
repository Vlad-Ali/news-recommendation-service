package org.hsse.news.database.role.exception;

import org.hsse.news.database.role.model.Role;

public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(final String message) {
        super(message);
    }
    public RoleNotFoundException(final Role role){super(role + "not found");}
}
