CREATE TABLE user_roles
(
    user_id UUID REFERENCES users (id) ON DELETE CASCADE,
    roles   VARCHAR(20) NOT NULL,
    PRIMARY KEY (user_id, roles)
);