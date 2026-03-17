package task1.mapper;

import task1.dto.BookCreateDto;
import task1.dto.BookResponseDto;
import task1.entity.Book;

public class BookMapper {
    public static Book toEntity(BookCreateDto dto) {
        return Book.builder()
                .title(dto.title())
                .publishedDate(dto.publishedDate())
                .description(dto.description())
                .genre(dto.genre())
                .build();
    }

    public static BookResponseDto toResponse(Book book) {
        String authorName = null;
        if (book.getAuthor() != null) {
            authorName = book.getAuthor().getFirstName() + " " + book.getAuthor().getLastName();
        }

        return BookResponseDto.builder()
                .id(book.getId())
                .authorName(authorName)
                .title(book.getTitle())
                .publishedDate(book.getPublishedDate())
                .description(book.getDescription())
                .genre(book.getGenre())
                .build();
    }
}
