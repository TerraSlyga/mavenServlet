package sumdu.edu.ua.persistence.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Entity
@Table(name = "comments")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class CommentEntity {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss").withZone(ZoneId.systemDefault());

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonBackReference("book-comments")
    private BookEntity book;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    @JsonBackReference("user-comments")
    private UserEntity user;

    @Column(length = 64)
    private String author;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public CommentEntity() {
    }

    public CommentEntity(BookEntity book, UserEntity user, String text) {
        this.book = book;
        this.user = user;
        this.author = (user != null) ? user.getUsername() : null;
        this.text = text;
        this.createdAt = Instant.now();
    }

    public CommentEntity(BookEntity book, UserEntity user, String author, String text) {
        this.book = book;
        this.user = user;
        this.author = (author != null && !author.isBlank()) ? author : (user != null ? user.getUsername() : null);
        this.text = text;
        this.createdAt = Instant.now();
    }

    public CommentEntity(Long id, BookEntity book, UserEntity user, String author, String text, Instant createdAt) {
        this.id = id;
        this.book = book;
        this.user = user;
        this.author = (author != null && !author.isBlank()) ? author : (user != null ? user.getUsername() : null);
        this.text = text;
        this.createdAt = (createdAt != null) ? createdAt : Instant.now();
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = Instant.now();
        }
        if (author == null && user != null) {
            author = user.getUsername();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BookEntity getBook() {
        return book;
    }

    public void setBook(BookEntity book) {
        this.book = book;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
        if (this.author == null && user != null) {
            this.author = user.getUsername();
        }
    }

    public String getAuthor() {
        if (author != null && !author.isBlank()) {
            return author;
        }
        return user != null ? user.getUsername() : "";
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCreatedAt() {
        return createdAt != null ? FORMATTER.format(createdAt) : "";
    }

    public Instant getCreatedAtInstant() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Long getBookId() {
        return book != null ? book.getId() : null;
    }

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommentEntity that = (CommentEntity) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(text, that.text) &&
                Objects.equals(createdAt, that.createdAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, text, createdAt);
    }

    @Override
    public String toString() {
        return "CommentEntity{" +
                "id=" + id +
                ", author='" + getAuthor() + '\'' +
                ", text='" + text + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }
}
