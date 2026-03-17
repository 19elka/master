CREATE TABLE books
(
    id             UUID PRIMARY KEY,
    title          VARCHAR(200) NOT NULL,
    published_date DATE,
    description    VARCHAR(2000),
    genre          VARCHAR(20) NOT NULL,
    author_id      UUID         NOT NULL REFERENCES authors(id) ON DELETE CASCADE
);