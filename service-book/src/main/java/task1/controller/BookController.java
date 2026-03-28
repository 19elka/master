package task1.controller;
/*
к 30.03.2026
энам, dto, config
1 посмотреть минимум 3 мок собеседования, вопросы написать
2 набросать минимальный фронтенд с 2 кнопками и табло погоды (1 страница), деплой в кубере (джаваскрипт, npm, node-js server)
3 добавить в систему и поднять nginx (reverse proxy), фронтенд шлет запросы на него, а он перенаправляет их на бекенд,
4 на бекенде настроить cors чтобы только nginx мог слать запросы
5 попробовать зайти на фронтенд с телефона (192....)
 */

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import task1.dto.book.BookCreateDto;
import task1.dto.book.BookResponseDto;
import task1.dto.book.BookUpdateDto;
import task1.service.BookService;

import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
public class BookController {
    private final BookService bookService;

    @PostMapping
    public BookResponseDto createBook(@Valid @RequestBody BookCreateDto dto) {
        return bookService.createBook(dto);
    }

    @GetMapping("/{id}")
    public BookResponseDto getBookById(@PathVariable UUID id) {
        return bookService.getBookById(id);
    }

    @GetMapping("/public")
    public PagedModel<BookResponseDto> getAllBooks(
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {

        Page<BookResponseDto> page = bookService.getAllBooks(pageable);

        return new PagedModel<>(page);
    }

    @GetMapping("/admin/search")
    public PagedModel<BookResponseDto> searchBooks(
            @RequestParam String title,
            @PageableDefault(size = 10, sort = "title") Pageable pageable) {

        Page<BookResponseDto> page = bookService.searchBooksByTitle(title, pageable);
        return new PagedModel<>(page);
    }

    @PutMapping("/{id}")
    public BookResponseDto updateBook(
            @PathVariable UUID id,
            @Valid @RequestBody BookUpdateDto dto) {
        return bookService.updateBook(id, dto);
    }

    @DeleteMapping("/{id}")
    public void deleteBook(@PathVariable UUID id) {
        bookService.deleteBook(id);
    }

    @PostMapping("/generate/{count}")
    public List<BookResponseDto> generateBooks(@PathVariable int count) {
        return bookService.generateBooks(count);
    }
}