package sumdu.edu.ua.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.web.dto.BookWithCommentsDto;

import java.util.List;
import java.util.Map;

/**
 * Spring MVC REST контролер для операцій з книгами.
 * Замінює застарілі BooksServlet та BooksApiServlet.
 */
@RestController
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
     * Повертає список книг у форматі JSON.
     * Маршрут: GET /books
     */
    @GetMapping("/books")
    public ResponseEntity<List<Book>> getBooksList(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {

        if (page < 0) {
            throw new IllegalArgumentException("Parameter 'page' cannot be negative");
        }
        if (size <= 0 || size > maxPageSize) {
            throw new IllegalArgumentException("Parameter 'size' must be between 1 and " + maxPageSize);
        }

        Page<Book> result = bookService.search(q, new PageRequest(page, size));
        return ResponseEntity.ok(result.getItems());
    }

    /**
     * Повертає дані однієї книги з коментарями у форматі JSON.
     * Маршрут: GET /books/{id}
     */
    @GetMapping("/books/{id}")
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
     * Пошук книг з пагінацією.
     * Маршрут: GET /api/books
     */
    @GetMapping("/api/books")
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
     * Отримання книги за ID.
     * Маршрут: GET /api/books/{id}
     */
    @GetMapping("/api/books/{id}")
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
     * Створення нової книги.
     * Маршрути: POST /books та POST /api/books
     */
    @PostMapping({"/books", "/api/books"})
    public ResponseEntity<?> createBook(@RequestBody BookRequest request) {
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
