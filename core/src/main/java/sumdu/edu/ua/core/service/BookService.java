package sumdu.edu.ua.core.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;

@Service
public class BookService {

    private final CatalogRepositoryPort bookRepo;

    @Autowired
    public BookService(CatalogRepositoryPort bookRepo) {
        this.bookRepo = bookRepo;
    }

    public Page<Book> search(String q, PageRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("PageRequest cannot be null");
        }
        if (request.getPage() < 0) {
            throw new IllegalArgumentException("Parameter 'page' cannot be negative");
        }
        if (request.getSize() <= 0) {
            throw new IllegalArgumentException("Parameter 'size' must be greater than 0");
        }
        return bookRepo.search(q, request);
    }

    public Book findById(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Book ID must be greater than 0: " + id);
        }
        return bookRepo.findById(id);
    }

    public Book add(String title, String author, int pubYear) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Field 'title' is required and cannot be blank");
        }
        if (author == null || author.isBlank()) {
            throw new IllegalArgumentException("Field 'author' is required and cannot be blank");
        }
        if (pubYear <= 0) {
            throw new IllegalArgumentException("Field 'pubYear' must be greater than 0");
        }
        return bookRepo.add(title.trim(), author.trim(), pubYear);
    }
}
