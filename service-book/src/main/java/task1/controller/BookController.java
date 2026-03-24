package task1.controller;
/*
к 23.03.2026
1 exception, у кафки есть KRaft,(*Jenkins и TeamCity)
роли хранить в таблице юзеров в колонке формате JSONB, в джава кода работать как со списком строк
2 написать микросервис B - вызывается микросервисом А по gRPC (если не получится то Рест), и идет на АПИ яндекса за погодой
3 ответ яндекса микросервис B кладет в кафку
4 микросервис А слушает кафку и сохраняет ответы в Редис
5 микросервис А имеет 2 эндпоинта, которые вызываются из постмана: 1) запросить обновление погоды;
 2) посмотреть погоду из редис (в одном миникубе 2 пода с микросервисами)
6 посмотреть мок собес и подготовить вопросы по его содержанию
к 26.03.2026
1 разделить проект: из монолитной к модульной (сервис-ориентированной) архит-ре. Использовать java 9 modules or maven/gradle modules.
проект в Идея должен состоять из модулей, которые билдятся в 3 отдельных джарника
 */

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
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
@Profile("book-service")
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