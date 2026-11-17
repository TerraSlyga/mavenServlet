package sumdu.edu.ua.core.service;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import sumdu.edu.ua.persistence.entity.BookEntity;
import sumdu.edu.ua.persistence.entity.CommentEntity;
import sumdu.edu.ua.persistence.entity.UserEntity;
import sumdu.edu.ua.persistence.repository.BookRepository;
import sumdu.edu.ua.persistence.repository.CommentRepository;
import sumdu.edu.ua.persistence.repository.UserRepository;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class CoreBusinessLogicTest {

    private BookService bookService;
    private CommentService commentService;
    private UserService userService;

    @BeforeMethod
    public void setUp() {
        BookRepository bookRepo = mock(BookRepository.class);
        UserRepository userRepo = mock(UserRepository.class);
        CommentRepository commentRepo = mock(CommentRepository.class);

        when(bookRepo.findById(1L)).thenReturn(Optional.of(new BookEntity(1L, "Test", "Author", 2020)));
        when(bookRepo.findById(argThat(id -> id != null && id > 1))).thenReturn(Optional.of(new BookEntity(2L, "Test", "Author", 2020)));
        when(bookRepo.save(any(BookEntity.class))).thenAnswer(inv -> {
            BookEntity b = inv.getArgument(0);
            return new BookEntity(1L, b.getTitle(), b.getAuthor(), b.getYear());
        });

        when(userRepo.findById(1L)).thenReturn(Optional.of(new UserEntity(1L, "john_doe", "pass", "ROLE_USER")));
        when(userRepo.findByUsername("Reader")).thenReturn(Optional.of(new UserEntity(1L, "Reader", "pass", "ROLE_USER")));

        when(commentRepo.save(any(CommentEntity.class))).thenAnswer(inv -> inv.getArgument(0));

        bookService = new BookService(bookRepo);
        commentService = new CommentService(commentRepo, bookRepo, userRepo);
        userService = new UserService(userRepo);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBookServiceInvalidId() {
        bookService.findById(-1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testBookServiceZeroId() {
        bookService.findById(0L);
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
        commentService.addComment(-1L, "Author", "Great book");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCommentServiceBlankAuthor() {
        commentService.addComment(1L, "   ", "Great book");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCommentServiceBlankText() {
        commentService.addComment(1L, "Author", "   ");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCommentServiceDeleteInvalidBookId() {
        commentService.delete(0L, 1L);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testCommentServiceDeleteInvalidCommentId() {
        commentService.delete(1L, 0L);
    }

    @Test
    public void testCommentServiceValidAdd() {
        commentService.addComment(1L, "Reader", "Nice!");
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void testUserServiceInvalidId() {
        userService.findById(0L);
    }

    @Test
    public void testUserServiceFindById() {
        UserEntity user = userService.findById(1L);
        org.testng.Assert.assertNotNull(user);
        org.testng.Assert.assertEquals(user.getUsername(), "john_doe");
    }
}
