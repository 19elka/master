package task1.controller;
/*
1. написать рест апи на получение списка книг с пагинацией (pageable), протестировать сортировку, разбиение по страницам
2* сделать поиск по частичному названию книге в этом же эндпоинте
напистаь полноценный crud - книги должны храниться в БД, использовать jpa together with pageable
create, update, read 1 id and 2 search with пагинацией, delete
05.03.2026
1. индексы и транзакции, субд стр-ра, блокировки субд, ACID, SQL и NSQL повторить
2. изучить все GenerationType
3. литкод без кода, без джава кода, решение придумай!
4. ручной маппинг написать, частичное совпадение при поиске и регистронезависимый в репозитории
5. добавить к книгам доп поля, на этих полях должны срабатывать триггеры (поле isbn было неизменяемым либо null, либо
финальное значение, добавить составной индекс на автора и название книги
6. несколько таблиц: авторы, издания. Настроить связи через внешние ключи
7. написать представление view или MATERIALIZED view  (дто книги с данными джойнами из других таблиц)
8. создать эндпоинт который генерирует N книг (сколько книг) (создание новых книг), протестировать EXPLAIN ANALYZE
к 09.03.2026
1. миграции, маппер, PagedModel, литкод дописать
2. аннотация транзакции изучить, spring security создать фильтр чейн из http достать заголовок авторизейшн в нем jwt,
из токена достать учетку при помощи b64 (сайт jwt.io) и сравнить с апликейшн ямал, если нет учетку -то ошибка
3. настроить авторизация эндпоинта все эндпоинты авторизация, и сравнить что прописано в конфигах.
к 12.03.2026
1 литкод сделать блок-схему с полным алгоритмом бинарного поиска по задаче (ПРОДУМАТЬ ВСЁ)
2 добавить валидацию токена по подписи и по времени
3 сделать эндпоинт логина выдает токен (необходимо сверять хэши паролей с тем, что в БД)
4 перенести юзеров из ямал в БД
5 реализовать хэширование паролей с помощью bcrypt
6 сделать эндпоинт для регистр пользователей
7 донастроить авторизацию эндпоинтов для разных ролей (всем, юзер, админ) + логирование
7.1. актуатор и сваггер должны работать без токена
7.2. настроить актуатор, маппер
к 16.03.2026
1 литкод доделать алгоритм и написать сам код, таблицу юзеры, AuthService энам и билдер, SecurityConfig, TABLE users
2 повторить функц интерфейсы
3 написать докер файл для сервиса, запустить сервис через докер образ
4 написать скрипты .sh для билда докер образа и для старта
5 скачать и запустить локальный minikube кластер
6 написать деплоймент манифест для сервиса в кубернетисе (minikube)
7 развернуть свой докер образ в кубернетисе и достучаться через постман до сервиса
к 19.03.2026
1 проштудировать функц. интерфейсы
2 проиниициализировать гитпроект и залить на гитхаб
3 все новые домашки оформлять в отдельной девелоп ветке в несколькиих коммитах и выставлять пул реквест в мастер.
4 TABLE init, entity User (энам), JwtAuthenticationFilter (advice)
отладить запуск кубернетис. изучить детально все манифесты, быть готовой ответить по каждой строке
5 поднять кафку локально в докере
6 вынести из кубера постгрес на локальную машину в докере
7 настроить соединение микросервиса в кубере с постгресом и кафкой (микросервис при старте вычитывает кафку и пишет в лог)
8 наполнить кафку данными о юзерах и их ролях
9 установить оффсет эксплорер (кафка ui), в нем добавить в топик 3 записи
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
import task1.dto.BookCreateDto;
import task1.dto.BookResponseDto;
import task1.dto.BookUpdateDto;
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