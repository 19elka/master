package task1.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import task1.entity.Genre;

import java.time.LocalDate;
import java.util.UUID;

public record BookCreateDto(
        @NotNull
        UUID authorId,
        @NotBlank(message = "Title cannot be blank")
        String title,
        LocalDate publishedDate,
        String description,
        @NotNull
        Genre genre
) {}