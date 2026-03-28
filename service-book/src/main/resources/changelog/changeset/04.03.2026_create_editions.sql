CREATE TABLE editions
(
    id               UUID PRIMARY KEY,
    book_id          UUID NOT NULL REFERENCES books (id) ON DELETE CASCADE,
    isbn             VARCHAR(10) UNIQUE,
    publication_date DATE,
    publisher        VARCHAR(30),
    pages            INTEGER
);