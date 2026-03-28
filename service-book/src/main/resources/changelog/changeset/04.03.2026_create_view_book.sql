CREATE VIEW book_details AS
SELECT b.id                               AS book_id,
       b.title,
       b.published_date,
       b.description,
       b.genre,
       a.id                               AS author_id,
       a.first_name || ' ' || a.last_name AS author_full_name
FROM books b
         JOIN authors a ON b.author_id = a.id;