CREATE TABLE book_publishers
(
    book_id      UUID REFERENCES books (id) ON DELETE CASCADE,
    publisher_id UUID REFERENCES publishers (id) ON DELETE CASCADE,
    PRIMARY KEY (book_id, publisher_id)
);