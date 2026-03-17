CREATE TABLE users
(
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    username   VARCHAR(20)  NOT NULL UNIQUE,
    password   VARCHAR(100) NOT NULL,
    roles      VARCHAR(20)  NOT NULL,
    name       VARCHAR(20)  NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO users (username, password, roles, name)
SELECT 'admin',
       '$2a$10$2s4TsA4Il/MFbTLXS9MxU.bFgPK38FCrqBuDnyzVIK58G1XRNBqAu',
       'ADMIN',
       'admin' WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'admin');

INSERT INTO users (username, password, roles, name)
SELECT 'user',
       '$2a$10$X7U.9z7q7q7q7q7q7q7q7u7q7q7q7q7q7q7q7q7q7q7q7q7q7q7q7q',
       'USER',
       'user' WHERE NOT EXISTS (SELECT 1 FROM users WHERE username = 'user');