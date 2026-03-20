package task1.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import task1.entity.Book;

import java.util.UUID;

@Repository
public interface BookRepository extends JpaRepository<Book, UUID> {
    @Query("""
            SELECT b FROM Book b
            WHERE LOWER(b.title)
            LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<Book> findByTitleContainingIgnoreCase(@Param("search") String search, Pageable pageable);
}
