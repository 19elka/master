CREATE TABLE publishers
(
    id      UUID PRIMARY KEY,
    name    VARCHAR(100) NOT NULL UNIQUE
);