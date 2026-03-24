package task1.repository;

import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import task1.entity.Author;

import java.util.List;
import java.util.UUID;

@Repository
@Profile("book-service")
public interface AuthorRepository extends JpaRepository<Author, UUID> {
    List<Author> findByLastName(String lastName);
}