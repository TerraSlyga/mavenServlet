package sumdu.edu.ua.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sumdu.edu.ua.core.domain.Comment;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.persistence.entity.BookEntity;
import sumdu.edu.ua.persistence.entity.CommentEntity;
import sumdu.edu.ua.persistence.entity.UserEntity;
import sumdu.edu.ua.persistence.repository.BookRepository;
import sumdu.edu.ua.persistence.repository.CommentRepository;
import sumdu.edu.ua.persistence.repository.UserRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CommentService {

    private final CommentRepository commentRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    @Autowired
    public CommentService(CommentRepository commentRepository,
                          BookRepository bookRepository,
                          UserRepository userRepository) {
        this.commentRepository = commentRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
    }

    public List<CommentEntity> getCommentsByUser(Long userId) {
        if (userId == null || userId <= 0) {
            throw new IllegalArgumentException("User ID must be greater than 0: " + userId);
        }
        return commentRepository.findByUserId(userId);
    }

    public List<CommentEntity> findByBookId(Long bookId) {
        if (bookId == null || bookId <= 0) {
            throw new IllegalArgumentException("Book ID must be greater than 0: " + bookId);
        }
        return commentRepository.findByBookId(bookId);
    }

    public CommentEntity findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Comment ID must be greater than 0: " + id);
        }
        return commentRepository.findById(id).orElse(null);
    }

    public List<CommentEntity> findAll() {
        return commentRepository.findAll();
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional
    public CommentEntity save(CommentEntity comment) {
        if (comment == null) {
            throw new IllegalArgumentException("Comment cannot be null");
        }
        return commentRepository.save(comment);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional
    public CommentEntity addComment(long bookId, String author, String text) {
        validateComment(bookId, author, text);

        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book with id " + bookId + " does not exist"));

        UserEntity user = null;
        if (author != null && !author.isBlank()) {
            user = userRepository.findByUsername(author.trim()).orElse(null);
        }

        CommentEntity comment = new CommentEntity(book, user, author.trim(), text.trim());
        return commentRepository.save(comment);
    }

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @Transactional
    public CommentEntity addComment(Long bookId, Long userId, String text) {
        if (bookId == null || bookId <= 0) {
            throw new IllegalArgumentException("Book ID must be greater than 0: " + bookId);
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Field 'text' is required and cannot be blank");
        }

        BookEntity book = bookRepository.findById(bookId)
                .orElseThrow(() -> new IllegalArgumentException("Book with id " + bookId + " does not exist"));

        UserEntity user = null;
        if (userId != null && userId > 0) {
            user = userRepository.findById(userId).orElse(null);
        }

        String author = (user != null) ? user.getUsername() : "Anonymous";
        CommentEntity comment = new CommentEntity(book, user, author, text.trim());
        return commentRepository.save(comment);
    }

    public Page<Comment> listComments(long bookId, String author, Instant since, sumdu.edu.ua.core.domain.PageRequest request) {
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

        var springPageable = PageRequest.of(request.getPage(), request.getSize(), Sort.by("id").descending());
        var entityPage = commentRepository.findByBookId(bookId, springPageable);

        var domainComments = entityPage.getContent().stream()
                .filter(c -> author == null || author.isBlank() || (c.getAuthor() != null && c.getAuthor().equalsIgnoreCase(author.trim())))
                .filter(c -> since == null || (c.getCreatedAtInstant() != null && c.getCreatedAtInstant().isAfter(since)))
                .map(c -> new Comment(
                        c.getId(),
                        c.getBook().getId(),
                        c.getAuthor(),
                        c.getText(),
                        c.getCreatedAtInstant()
                ))
                .toList();

        return new Page<>(domainComments, request, entityPage.getTotalElements());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(long bookId, long commentId) {
        if (bookId <= 0) {
            throw new IllegalArgumentException("Book ID must be greater than 0: " + bookId);
        }
        if (commentId <= 0) {
            throw new IllegalArgumentException("Comment ID must be greater than 0: " + commentId);
        }
        CommentEntity comment = commentRepository.findById(commentId).orElse(null);
        if (comment != null && comment.getBook() != null && comment.getBook().getId().equals(bookId)) {
            commentRepository.delete(comment);
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Comment ID must be greater than 0: " + id);
        }
        commentRepository.deleteById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(long bookId, long commentId, Instant createdAt) {
        delete(bookId, commentId);
        if (createdAt != null && Duration.between(createdAt, Instant.now()).toHours() > 24) {
            throw new IllegalStateException("Comment too old to delete");
        }
    }

    private void validateComment(long bookId, String author, String text) {
        if (bookId <= 0) {
            throw new IllegalArgumentException("Book ID must be greater than 0: " + bookId);
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("Field 'author' is required and cannot be blank");
        }
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Field 'text' is required and cannot be blank");
        }
    }
}
