package sumdu.edu.ua.core.port;

import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;

import java.time.Instant;

public interface CommentRepositoryPort {
    void add(long bookId, String author, String text);
    Page<Comment> list(long bookId, String author, Instant since, PageRequest request);
    void delete(long bookId, long commentId);

}
