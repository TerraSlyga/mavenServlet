package sumdu.edu.ua.web.dto;

import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.persistence.entity.BookEntity;

import java.util.List;

/**
 * DTO для представлення книги разом із її коментарями.
 */
public record BookWithCommentsDto(
        long id,
        String title,
        String author,
        int pubYear,
        List<Comment> comments
) {
    public static BookWithCommentsDto of(Book book, List<Comment> comments) {
        return new BookWithCommentsDto(
                book.getId(),
                book.getTitle(),
                book.getAuthor(),
                book.getPubYear(),
                comments != null ? comments : List.of()
        );
    }

    public static BookWithCommentsDto of(BookEntity book, List<Comment> comments) {
        return new BookWithCommentsDto(
                book.getId() != null ? book.getId() : 0L,
                book.getTitle(),
                book.getAuthor(),
                book.getYear(),
                comments != null ? comments : List.of()
        );
    }
}
