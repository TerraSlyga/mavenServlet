package sumdu.edu.ua.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.web.dto.BookWithCommentsDto;

import java.util.Map;

/**
 * Spring MVC контролер для операцій з книгами.
 * Повертає імена представлень Thymeleaf для веб-інтерфейсу
 * та підтримує REST ендпоінти.
 */
@Controller
public class BookController {

    private final BookService bookService;
    private final CommentService commentService;
    private final int defaultPageSize;
    private final int maxPageSize;

    public record BookRequest(String title, String author, int pubYear) {}

    @Autowired
    public BookController(
            BookService bookService,
            CommentService commentService,
            @Value("${app.default-page-size:10}") int defaultPageSize,
            @Value("${app.max-page-size:100}") int maxPageSize) {
        this.bookService = bookService;
        this.commentService = commentService;
        this.defaultPageSize = defaultPageSize;
        this.maxPageSize = maxPageSize;
    }

    /**
     * Відображення списку книг через HTML-шаблон Thymeleaf.
     * Маршрут: GET /books
     */
    @GetMapping("/books")
    public String getBooksList(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "50") int size,
            Model model) {

        if (page < 0) {
            throw new IllegalArgumentException("Parameter 'page' cannot be negative");
        }
        if (size <= 0 || size > maxPageSize) {
            throw new IllegalArgumentException("Parameter 'size' must be between 1 and " + maxPageSize);
        }

        Page<Book> result = bookService.search(q, new PageRequest(page, size));
        model.addAttribute("books", result.getItems());
        return "books";
    }

    /**
     * Відображення форми додавання книги через HTML-шаблон Thymeleaf.
     * Маршрут: GET /books/add
     */
    @GetMapping("/books/add")
    public String showAddBookForm(Model model) {
        model.addAttribute("book", new Book());
        return "book-form";
    }

    /**
     * Обробка форми додавання книги.
     * Маршрут: POST /books/add
     */
    @PostMapping("/books/add")
    public String addBook(@ModelAttribute("book") Book book) {
        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new IllegalArgumentException("Field 'title' is required and cannot be blank");
        }
        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new IllegalArgumentException("Field 'author' is required and cannot be blank");
        }
        if (book.getPubYear() < 0) {
            throw new IllegalArgumentException("Field 'pubYear' cannot be negative");
        }

        bookService.add(book.getTitle().trim(), book.getAuthor().trim(), book.getPubYear());
        return "redirect:/books";
    }

    /**
     * Обробка форми додавання книги через POST /books (form-urlencoded).
     */
    @PostMapping(value = "/books", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public String addBookFormSubmit(@ModelAttribute("book") Book book) {
        return addBook(book);
    }

    /**
     * Повертає дані однієї книги з коментарями у форматі JSON.
     * Маршрут: GET /books/{id}
     */
    @GetMapping("/books/{id}")
    @ResponseBody
    public ResponseEntity<?> getBookWithComments(@PathVariable("id") long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Book ID must be greater than 0");
        }

        Book book = bookService.findById(id);
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", HttpStatus.NOT_FOUND.value(),
                    "error", "Not Found",
                    "message", "Book with id " + id + " was not found"
            ));
        }

        Page<Comment> commentsPage = commentService.listComments(id, null, null, new PageRequest(0, 100));
        return ResponseEntity.ok(BookWithCommentsDto.of(book, commentsPage.getItems()));
    }

    /**
     * Пошук книг з пагінацією (REST API).
     * Маршрут: GET /api/books
     */
    @GetMapping("/api/books")
    @ResponseBody
    public ResponseEntity<Page<Book>> searchBooks(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", required = false) Integer size) {

        int pageSize = (size != null) ? size : defaultPageSize;

        if (page < 0) {
            throw new IllegalArgumentException("Parameter 'page' cannot be negative");
        }
        if (pageSize <= 0 || pageSize > maxPageSize) {
            throw new IllegalArgumentException("Parameter 'size' must be between 1 and " + maxPageSize);
        }

        Page<Book> result = bookService.search(q, new PageRequest(page, pageSize));
        return ResponseEntity.ok(result);
    }

    /**
     * Отримання книги за ID (REST API).
     * Маршрут: GET /api/books/{id}
     */
    @GetMapping("/api/books/{id}")
    @ResponseBody
    public ResponseEntity<?> getBookById(@PathVariable("id") long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Book ID must be greater than 0");
        }

        Book book = bookService.findById(id);
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", HttpStatus.NOT_FOUND.value(),
                    "error", "Not Found",
                    "message", "Book with id " + id + " was not found"
            ));
        }
        return ResponseEntity.ok(book);
    }

    /**
     * Створення нової книги через JSON REST API.
     * Маршрути: POST /books та POST /api/books
     */
    @PostMapping(value = {"/books", "/api/books"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public ResponseEntity<?> createBookJson(@RequestBody BookRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }

        Book savedBook = bookService.add(
                request.title(),
                request.author(),
                request.pubYear()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }
}
