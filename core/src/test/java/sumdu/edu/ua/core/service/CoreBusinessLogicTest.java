package sumdu.edu.ua.core.service;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.core.port.CommentRepositoryPort;

import java.time.Instant;
import java.util.List;

import static org.testng.Assert.*;

public class CoreBusinessLogicTest {

    private BookService bookService;
    private CommentService commentService;

    @BeforeMethod
    public void setUp() {
        CatalogRepositoryPort dummyBookRepo = new CatalogRepositoryPort() {
            @Override
            public Page<Book> search(String q, PageRequest request) {
                return new Page<>(List.of(new Book(1L, "Test", "Author", 2020)), request, 1);
            }

            @Override
            public Book findById(long id) {
                return new Book(id, "Test", "Author", 2020);
            }

            @Override
            public Book add(String title, String author, int pubYear) {
                return new Book(1L, title, author, pubYear);
            }
        };

        CommentRepositoryPort dummyCommentRepo = new CommentRepositoryPort() {
            @Override
            public void add(long bookId, String author, String text) {
            }

            @Override
            public Page<Comment> list(long bookId, String author, Instant since, PageRequest request) {
                return new Page<>(List.of(new Comment(1L, bookId, author, "Text", Instant.now())), request, 1);
            }

            @Override
            public void delete(long bookId, long commentId) {
            }
        };

        bookService = new BookService(dummyBookRepo);
        commentService = new CommentService(dummyCommentRepo);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBookServiceInvalidId() {
        bookService.findById(-1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBookServiceZeroId() {
        bookService.findById(0);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBookServiceBlankTitle() {
        bookService.add("   ", "Author", 2020);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBookServiceBlankAuthor() {
        bookService.add("Title", "  ", 2020);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBookServiceInvalidYear() {
        bookService.add("Title", "Author", -5);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCommentServiceInvalidBookId() {
        commentService.addComment(-1, "Author", "Great book");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCommentServiceBlankAuthor() {
        commentService.addComment(1, "   ", "Great book");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCommentServiceBlankText() {
        commentService.addComment(1, "Author", "   ");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCommentServiceDeleteInvalidBookId() {
        commentService.delete(0, 1);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCommentServiceDeleteInvalidCommentId() {
        commentService.delete(1, 0);
    }

    @Test
    public void testCommentServiceValidAdd() {
        commentService.addComment(1, "Reader", "Nice!");
    }
}
