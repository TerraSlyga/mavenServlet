package sumdu.edu.ua.web;

import io.javalin.Javalin;
import io.javalin.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sumdu.edu.ua.config.Beans;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.core.port.CommentRepositoryPort;

import java.util.Map;

public class JavalinBookApp {

    private static final Logger log = LoggerFactory.getLogger(JavalinBookApp.class);

    public record BookRequest(String title, String author, int pubYear) {}
    public record CommentRequest(String author, String text) {}

    public static Javalin createApp() {
        CatalogRepositoryPort bookRepo = Beans.getBookRepo();
        CommentRepositoryPort commentRepo = Beans.getCommentRepo();

        Javalin app = Javalin.create();

        // 1. Middleware: логування HTTP-запитів перед виконанням маршрутів
        app.before(ctx -> {
            log.info("[HTTP] {} {} query=[{}] ip={}",
                    ctx.method(), ctx.path(), ctx.queryString(), ctx.ip());
        });

        // 2. Централізована обробка виключень (Centralized Exception Handling)
        app.exception(IllegalArgumentException.class, (e, ctx) -> {
            log.warn("Validation error: {}", e.getMessage());
            ctx.status(HttpStatus.BAD_REQUEST).json(Map.of(
                    "status", HttpStatus.BAD_REQUEST.getCode(),
                    "error", "Bad Request",
                    "message", e.getMessage() != null ? e.getMessage() : "Invalid request"
            ));
        });

        app.exception(IllegalStateException.class, (e, ctx) -> {
            log.warn("State error: {}", e.getMessage());
            ctx.status(HttpStatus.CONFLICT).json(Map.of(
                    "status", HttpStatus.CONFLICT.getCode(),
                    "error", "Conflict",
                    "message", e.getMessage() != null ? e.getMessage() : "Illegal state"
            ));
        });

        app.exception(Exception.class, (e, ctx) -> {
            log.error("Unhandled error processing request: ", e);
            ctx.status(HttpStatus.INTERNAL_SERVER_ERROR).json(Map.of(
                    "status", HttpStatus.INTERNAL_SERVER_ERROR.getCode(),
                    "error", "Internal Server Error",
                    "message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred"
            ));
        });

        // Кореневий маршрут
        app.get("/", ctx -> {
            ctx.status(HttpStatus.OK).json(Map.of(
                    "app", "Javalin Book App",
                    "version", "1.0",
                    "endpoints", Map.of(
                            "books", "/api/books",
                            "bookDetails", "/api/books/{id}",
                            "bookComments", "/api/books/{bookId}/comments",
                            "deleteComment", "/api/books/{bookId}/comments/{commentId}"
                    )
            ));
        });

        // --- REST-маршрути для каталогу книг ---

        // GET /api/books - отримання списку книг з пагінацією та пошуком через query-параметри
        app.get("/api/books", ctx -> {
            int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
            int size = ctx.queryParamAsClass("size", Integer.class).getOrDefault(10);
            String q = ctx.queryParam("q");

            if (page < 0) {
                throw new IllegalArgumentException("Parameter 'page' cannot be negative");
            }
            if (size <= 0 || size > 100) {
                throw new IllegalArgumentException("Parameter 'size' must be between 1 and 100");
            }

            Page<Book> result = bookRepo.search(q, new PageRequest(page, size));
            ctx.status(HttpStatus.OK).json(result);
        });

        // GET /api/books/{id} - отримання книги за ідентифікатором (path-параметр)
        app.get("/api/books/{id}", ctx -> {
            long id = ctx.pathParamAsClass("id", Long.class).get();
            Book book = bookRepo.findById(id);

            if (book == null) {
                ctx.status(HttpStatus.NOT_FOUND).json(Map.of(
                        "status", HttpStatus.NOT_FOUND.getCode(),
                        "error", "Not Found",
                        "message", "Book with id " + id + " was not found"
                ));
                return;
            }

            ctx.status(HttpStatus.OK).json(book);
        });

        // POST /api/books - створення нової книги (читання тіла запиту JSON, HTTP 201)
        app.post("/api/books", ctx -> {
            BookRequest request = ctx.bodyAsClass(BookRequest.class);

            if (request.title() == null || request.title().isBlank()) {
                throw new IllegalArgumentException("Field 'title' is required and cannot be blank");
            }
            if (request.author() == null || request.author().isBlank()) {
                throw new IllegalArgumentException("Field 'author' is required and cannot be blank");
            }
            if (request.pubYear() <= 0) {
                throw new IllegalArgumentException("Field 'pubYear' must be greater than 0");
            }

            Book savedBook = bookRepo.add(
                    request.title().trim(),
                    request.author().trim(),
                    request.pubYear()
            );

            ctx.status(HttpStatus.CREATED).json(savedBook);
        });

        // --- REST-маршрути для коментарів до книг ---

        // GET /api/books/{bookId}/comments - список коментарів книги (path-параметр + query-параметри)
        app.get("/api/books/{bookId}/comments", ctx -> {
            long bookId = ctx.pathParamAsClass("bookId", Long.class).get();
            int page = ctx.queryParamAsClass("page", Integer.class).getOrDefault(0);
            int size = ctx.queryParamAsClass("size", Integer.class).getOrDefault(20);
            String author = ctx.queryParam("author");

            Book book = bookRepo.findById(bookId);
            if (book == null) {
                ctx.status(HttpStatus.NOT_FOUND).json(Map.of(
                        "status", HttpStatus.NOT_FOUND.getCode(),
                        "error", "Not Found",
                        "message", "Book with id " + bookId + " does not exist"
                ));
                return;
            }

            Page<Comment> comments = commentRepo.list(bookId, author, null, new PageRequest(page, size));
            ctx.status(HttpStatus.OK).json(comments);
        });

        // POST /api/books/{bookId}/comments - додавання коментаря до книги (path-параметр + JSON body)
        app.post("/api/books/{bookId}/comments", ctx -> {
            long bookId = ctx.pathParamAsClass("bookId", Long.class).get();
            CommentRequest request = ctx.bodyAsClass(CommentRequest.class);

            if (request.author() == null || request.author().isBlank()) {
                throw new IllegalArgumentException("Field 'author' is required and cannot be blank");
            }
            if (request.text() == null || request.text().isBlank()) {
                throw new IllegalArgumentException("Field 'text' is required and cannot be blank");
            }

            Book book = bookRepo.findById(bookId);
            if (book == null) {
                ctx.status(HttpStatus.NOT_FOUND).json(Map.of(
                        "status", HttpStatus.NOT_FOUND.getCode(),
                        "error", "Not Found",
                        "message", "Book with id " + bookId + " does not exist"
                ));
                return;
            }

            commentRepo.add(bookId, request.author().trim(), request.text().trim());

            ctx.status(HttpStatus.CREATED).json(Map.of(
                    "status", HttpStatus.CREATED.getCode(),
                    "message", "Comment added successfully",
                    "bookId", bookId,
                    "author", request.author().trim(),
                    "text", request.text().trim()
            ));
        });

        // DELETE /api/books/{bookId}/comments/{commentId} - видалення коментаря за ідентифікатором
        app.delete("/api/books/{bookId}/comments/{commentId}", ctx -> {
            long bookId = ctx.pathParamAsClass("bookId", Long.class).get();
            long commentId = ctx.pathParamAsClass("commentId", Long.class).get();

            commentRepo.delete(bookId, commentId);

            ctx.status(HttpStatus.OK).json(Map.of(
                    "status", HttpStatus.OK.getCode(),
                    "message", "Comment deleted successfully",
                    "bookId", bookId,
                    "commentId", commentId
            ));
        });

        return app;
    }

    public static void main(String[] args) {
        // Ініціалізація бази даних (створення таблиць та тестових даних)
        Beans.init();

        int port = 8080;
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isBlank()) {
            try {
                port = Integer.parseInt(portEnv);
            } catch (NumberFormatException ignored) {}
        }

        Javalin app = createApp();
        app.start(port);
        log.info("JavalinBookApp started on http://localhost:{}", port);
    }
}
