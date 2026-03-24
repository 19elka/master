package task1.dto.book;

import lombok.Builder;
import task1.entity.Genre;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record BookResponseDto(
        UUID id,
        String authorName,
        String title,
        LocalDate publishedDate,
        String description,
        Genre genre
) {
}