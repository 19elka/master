package task1.dto;

import task1.entity.Genre;

import java.time.LocalDate;
import java.util.UUID;

public record BookUpdateDto(
        UUID id,
        UUID authorId,
        String title,
        LocalDate publishedDate,
        String description,
        Genre genre
) {
}