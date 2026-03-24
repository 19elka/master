CREATE UNIQUE INDEX IF NOT EXISTS idx_books_author_title_unique
    ON books (author_id, title);