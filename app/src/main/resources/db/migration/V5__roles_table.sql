CREATE TABLE IF NOT EXISTS roles(
    role_id bigserial PRIMARY KEY,
    role text NOT NULL UNIQUE
);

CREATE index IF NOT EXISTS index_roles_role_id ON roles (role_id);

CREATE TABLE IF NOT EXISTS user_roles(
     user_id uuid NOT NULL REFERENCES users (user_id) ON DELETE CASCADE,
     role_id bigserial NOT NULL REFERENCES roles (role_id) ON DELETE CASCADE,
     PRIMARY KEY (user_id, role_id)
);

CREATE index IF NOT EXISTS index_user_roles_user_id on user_roles (user_id);