CREATE
MATERIALIZED VIEW book_summary AS
SELECT b.id,
       b.title,
       b.genre,
       COUNT(e.id)                            AS editions_count,
       MIN(e.publication_date)                AS first_edition,
       MAX(e.publication_date)                AS last_edition,
       STRING_AGG(DISTINCT a.last_name, ', ') AS authors
FROM books b
         JOIN authors a ON b.author_id = a.id
         LEFT JOIN editions e ON b.id = e.book_id
GROUP BY b.id, b.title, b.genre;