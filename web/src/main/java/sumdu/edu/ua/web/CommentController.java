package sumdu.edu.ua.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.CommentService;

import java.util.Map;

/**
 * REST контролер для коментарів до книг.
 */
@RestController
@RequestMapping("/api/books/{bookId}/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private BookService bookService;

    public record CommentRequest(String author, String text) {}

    @GetMapping
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

    @PostMapping
    public ResponseEntity<?> addComment(
            @PathVariable("bookId") long bookId,
            @RequestBody CommentRequest request) {

        if (request.author() == null || request.author().isBlank()) {
            throw new IllegalArgumentException("Field 'author' is required and cannot be blank");
        }
        if (request.text() == null || request.text().isBlank()) {
            throw new IllegalArgumentException("Field 'text' is required and cannot be blank");
        }

        Book book = bookService.findById(bookId);
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                    "status", HttpStatus.NOT_FOUND.value(),
                    "error", "Not Found",
                    "message", "Book with id " + bookId + " does not exist"
            ));
        }

        commentService.addComment(bookId, request.author().trim(), request.text().trim());

        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "status", HttpStatus.CREATED.value(),
                "message", "Comment added successfully",
                "bookId", bookId,
                "author", request.author().trim(),
                "text", request.text().trim()
        ));
    }

    @DeleteMapping("/{commentId}")
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
}
