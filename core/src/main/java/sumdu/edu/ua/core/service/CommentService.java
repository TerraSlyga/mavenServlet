package sumdu.edu.ua.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.port.CommentRepositoryPort;

import java.time.Duration;
import java.time.Instant;

@Service
public class CommentService {

    private final CommentRepositoryPort repo;

    public CommentService() {
        this.repo = null;
    }

    @Autowired
    public CommentService(CommentRepositoryPort repo) {
        this.repo = repo;
    }

    public void addComment(long bookId, String author, String text) {
        if (bookId <= 0) {
            throw new IllegalArgumentException("Book ID must be greater than 0: " + bookId);
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("Field 'author' is required and cannot be blank");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Field 'text' is required and cannot be blank");
        }
        getRepo().add(bookId, author.trim(), text.trim());
    }

    public Page<Comment> listComments(long bookId, String author, Instant since, PageRequest request) {
        if (bookId <= 0) {
            throw new IllegalArgumentException("Book ID must be greater than 0: " + bookId);
        }
        if (request == null) {
            throw new IllegalArgumentException("PageRequest cannot be null");
        }
        if (request.getPage() < 0) {
            throw new IllegalArgumentException("Parameter 'page' cannot be negative");
        }
        if (request.getSize() <= 0) {
            throw new IllegalArgumentException("Parameter 'size' must be greater than 0");
        }
        return getRepo().list(bookId, author, since, request);
    }

    public void delete(long bookId, long commentId) {
        if (bookId <= 0) {
            throw new IllegalArgumentException("Book ID must be greater than 0: " + bookId);
        }
        if (commentId <= 0) {
            throw new IllegalArgumentException("Comment ID must be greater than 0: " + commentId);
        }
        getRepo().delete(bookId, commentId);
    }

    public void delete(long bookId, long commentId, Instant createdAt) {
        delete(bookId, commentId);
        if (createdAt != null && Duration.between(createdAt, Instant.now()).toHours() > 24) {
            throw new IllegalStateException("Comment too old to delete");
        }
    }

    private CommentRepositoryPort getRepo() {
        if (repo == null) {
            throw new IllegalStateException("CommentRepositoryPort is not injected");
        }
        return repo;
    }
}
