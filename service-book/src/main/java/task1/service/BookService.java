package task1.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import task1.dto.book.BookCreateDto;
import task1.dto.book.BookResponseDto;
import task1.dto.book.BookUpdateDto;
import task1.repository.AuthorRepository;
import task1.generator.BookDataGenerator;
import task1.mapper.BookMapper;
import task1.repository.BookRepository;
import task1.entity.Author;
import task1.entity.Book;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Profile("book-service")
public class BookService {
    private final BookRepository bookRepository;
    private final AuthorRepository authorRepository;
    private final BookDataGenerator generator;
//    private final BookService b; //Self-Injection

    @Transactional
    public BookResponseDto createBook(BookCreateDto dto) {
        Book book = BookMapper.toEntity(dto);
        Author author = authorRepository.findById(dto.authorId())
                .orElseThrow(() -> new RuntimeException("Author not found"));
        book.setAuthor(author);
        Book savedBook = bookRepository.save(book);
        log.info("Book created with id: {}", savedBook.getId());
        return BookMapper.toResponse(savedBook);
    }

    @Transactional(readOnly = true)
    public BookResponseDto getBookById(UUID id) {
        Book book = bookRepository.findById(id)
                .orElseThrow();
        log.info("Found the book with id: {}", id);
        return BookMapper.toResponse(book);
    }

    @Transactional(readOnly = true)
    public Page<BookResponseDto> getAllBooks(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(BookMapper::toResponse);
    }

    public Page<BookResponseDto> searchBooksByTitle(String title, Pageable pageable) {
        return bookRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(BookMapper::toResponse);
    }

    @Transactional
    public BookResponseDto updateBook(UUID id, BookUpdateDto dto) {
        Book book = bookRepository.findById(id)
                .orElseThrow();
        book.setTitle(dto.title());
        book.setPublishedDate(dto.publishedDate());
        book.setDescription(dto.description());
        book.setGenre(dto.genre());

        if (dto.authorId() != null) {
            Author author = authorRepository.findById(dto.authorId())
                    .orElseThrow(() -> new RuntimeException("Author not found"));
            book.setAuthor(author);
        }
//        b.deleteBook(UUID.randomUUID()); //Self-Injection
        Book savedBook = bookRepository.save(book);
        log.info("Book updated with id: {}", savedBook.getId());
        return BookMapper.toResponse(savedBook);
    }

    @Transactional
    public void deleteBook(UUID id) {
        bookRepository.deleteById(id);
        log.info("Book deleted with id: {}", id);
    }

    public List<BookResponseDto> generateBooks(int count) {
        List<Book> books = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            Author author = new Author();
            author.setFirstName(generator.generateFirstName());
            author.setLastName(generator.generateLastName());
            author = authorRepository.save(author);

            Book book = new Book();
            book.setTitle(generator.generateTitle());
            book.setDescription(generator.generateDescription());
            book.setGenre(generator.generateGenre());
            book.setPublishedDate(generator.generatePublishedDate());
            book.setAuthor(author);

            books.add(bookRepository.save(book));
        }

        return books.stream().map(BookMapper::toResponse).toList();
    }
}