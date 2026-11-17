package sumdu.edu.ua.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.persistence.entity.BookEntity;
import sumdu.edu.ua.persistence.repository.BookRepository;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class BookService {

    private final BookRepository bookRepository;

    @Autowired
    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<BookEntity> findAll() {
        return bookRepository.findAll();
    }

    public BookEntity findById(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Book ID must be greater than 0: " + id);
        }
        return bookRepository.findById(id).orElse(null);
    }

    public Book findBookById(long id) {
        BookEntity entity = findById(id);
        if (entity == null) {
            return null;
        }
        return new Book(entity.getId(), entity.getTitle(), entity.getAuthor(), entity.getYear());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public BookEntity save(BookEntity book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }
        validateBook(book.getTitle(), book.getAuthor(), book.getYear());
        return bookRepository.save(book);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Book ID must be greater than 0: " + id);
        }
        bookRepository.deleteById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Book add(String title, String author, int pubYear) {
        validateBook(title, author, pubYear);
        BookEntity entity = new BookEntity(title.trim(), author.trim(), pubYear);
        BookEntity saved = bookRepository.save(entity);
        return new Book(saved.getId(), saved.getTitle(), saved.getAuthor(), saved.getYear());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public BookEntity addEntity(String title, String author, int pubYear) {
        validateBook(title, author, pubYear);
        BookEntity entity = new BookEntity(title.trim(), author.trim(), pubYear);
        return bookRepository.save(entity);
    }

    public Page<Book> search(String q, sumdu.edu.ua.core.domain.PageRequest request) {
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
        var entityPage = (q != null && !q.isBlank())
                ? bookRepository.search(q.trim(), springPageable)
                : bookRepository.findAll(springPageable);

        var domainBooks = entityPage.getContent().stream()
                .map(e -> new Book(e.getId(), e.getTitle(), e.getAuthor(), e.getYear()))
                .toList();

        return new Page<>(domainBooks, request, entityPage.getTotalElements());
    }

    private void validateBook(String title, String author, int pubYear) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Field 'title' is required and cannot be blank");
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("Field 'author' is required and cannot be blank");
        }
        if (pubYear <= 0) {
            throw new IllegalArgumentException("Field 'pubYear' must be greater than 0");
        }
    }
}
