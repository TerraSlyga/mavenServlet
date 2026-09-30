package sumdu.edu.ua.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.web.dto.CreateCommentRequest;

import java.util.Map;

/**
 * Spring MVC REST контролер для операцій з коментарями (відгуками).
 * Замінює застарілий CommentsServlet.
 */
@RestController
public class CommentController {

    private final CommentService commentService;
    private final BookService bookService;

    public record CommentRequest(String author, String text) {}

    @Autowired
    public CommentController(CommentService commentService, BookService bookService) {
        this.commentService = commentService;
        this.bookService = bookService;
    }

    /**
     * Додавання нового відгуку через POST /comments (JSON формат).
     */
    @PostMapping(value = "/comments", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> addCommentJson(@RequestBody CreateCommentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        if (request.bookId() == null || request.bookId() <= 0) {
            throw new IllegalArgumentException("Field 'bookId' must be greater than 0");
        }
        return processAddComment(request.bookId(), request.author(), request.text());
    }

    /**
     * Додавання нового відгуку через POST /comments (Form URL-Encoded формат).
     */
    @PostMapping(value = "/comments", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> addCommentForm(
            @RequestParam("bookId") Long bookId,
            @RequestParam("author") String author,
            @RequestParam("text") String text) {
        if (bookId == null || bookId <= 0) {
            throw new IllegalArgumentException("Field 'bookId' must be greater than 0");
        }
        return processAddComment(bookId, author, text);
    }

    /**
     * Додавання коментаря для конкретної книги за шляхом /api/books/{bookId}/comments.
     */
    @PostMapping("/api/books/{bookId}/comments")
    public ResponseEntity<?> addCommentForBook(
            @PathVariable("bookId") long bookId,
            @RequestBody CommentRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body cannot be null");
        }
        return processAddComment(bookId, request.author(), request.text());
    }

    /**
     * Отримання списку коментарів для книги за шляхом /api/books/{bookId}/comments.
     */
    @GetMapping("/api/books/{bookId}/comments")
    public ResponseEntity<?> listComments(
            @PathVariable("bookId") long bookId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "author", required = false) String author) {

        Book book = bookService.findById(bookId);
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", HttpStatus.NOT_FOUND.value(),
                    "error", "Not Found",
                    "message", "Book with id " + bookId + " does not exist"
            ));
        }

        Page<Comment> comments = commentService.listComments(bookId, author, null, new PageRequest(page, size));
        return ResponseEntity.ok(comments);
    }

    /**
     * Видалення коментаря за ID.
     */
    @DeleteMapping("/api/books/{bookId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable("bookId") long bookId,
            @PathVariable("commentId") long commentId) {

        commentService.delete(bookId, commentId);

        return ResponseEntity.ok(Map.of(
                "status", HttpStatus.OK.value(),
                "message", "Comment deleted successfully",
                "bookId", bookId,
                "commentId", commentId
        ));
    }

    private ResponseEntity<?> processAddComment(long bookId, String author, String text) {
        Book book = bookService.findById(bookId);
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", HttpStatus.NOT_FOUND.value(),
                    "error", "Not Found",
                    "message", "Book with id " + bookId + " does not exist"
            ));
        }

        commentService.addComment(bookId, author, text);

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", HttpStatus.CREATED.value(),
                "message", "Comment added successfully",
                "bookId", bookId,
                "author", author != null ? author.trim() : "",
                "text", text != null ? text.trim() : ""
        ));
    }
}
