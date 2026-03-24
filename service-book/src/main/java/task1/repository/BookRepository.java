package task1.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import task1.entity.Book;

import java.util.UUID;

@Repository
@Profile("book-service")
public interface BookRepository extends JpaRepository<Book, UUID> {
    @Query("""
            SELECT b FROM Book b
            WHERE LOWER(b.title)
            LIKE LOWER(CONCAT('%', :search, '%'))
            """)
    Page<Book> findByTitleContainingIgnoreCase(@Param("search") String search, Pageable pageable);
}
