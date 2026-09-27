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

    // Демонстрація ін'єкції залежностей через поле (Field Injection)
    @Autowired
    private CommentRepositoryPort repo;

    public CommentService() {
    }

    public CommentService(CommentRepositoryPort repo) {
        this.repo = repo;
    }

    public void addComment(long bookId, String author, String text) {
        repo.add(bookId, author, text);
    }

    public Page<Comment> listComments(long bookId, String author, Instant since, PageRequest request) {
        return repo.list(bookId, author, since, request);
    }

    public void delete(long bookId, long commentId) {
        repo.delete(bookId, commentId);
    }

    public void delete(long bookId, long commentId, Instant createdAt) {
        if (createdAt != null && Duration.between(createdAt, Instant.now()).toHours() > 24) {
            throw new IllegalStateException("Comment too old to delete");
        }
        repo.delete(bookId, commentId);
    }
}
