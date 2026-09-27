package sumdu.edu.ua.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.service.BookService;

import java.util.Map;

/**
 * REST контролер для операцій з каталогом книг.
 */
@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;
    private final int defaultPageSize;
    private final int maxPageSize;

    public record BookRequest(String title, String author, int pubYear) {}

    @Autowired
    public BookController(
            BookService bookService,
            @Value("${app.default-page-size:10}") int defaultPageSize,
            @Value("${app.max-page-size:100}") int maxPageSize) {
        this.bookService = bookService;
        this.defaultPageSize = defaultPageSize;
        this.maxPageSize = maxPageSize;
    }

    @GetMapping
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getBookById(@PathVariable("id") long id) {
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

    @PostMapping
    public ResponseEntity<?> createBook(@RequestBody BookRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("Field 'title' is required and cannot be blank");
        }
        if (request.author() == null || request.author().isBlank()) {
            throw new IllegalArgumentException("Field 'author' is required and cannot be blank");
        }
        if (request.pubYear() <= 0) {
            throw new IllegalArgumentException("Field 'pubYear' must be greater than 0");
        }

        Book savedBook = bookService.add(
                request.title().trim(),
                request.author().trim(),
                request.pubYear()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(savedBook);
    }
}
