CREATE TABLE users
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username   VARCHAR(20)  NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    name       VARCHAR(20)  NOT NULL,
    roles      jsonb        NOT NULL DEFAULT '[]'::jsonb,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);