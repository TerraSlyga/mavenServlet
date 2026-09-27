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

    // Демонстрація ін'єкції залежностей через конструктор (Constructor Injection)
    @Autowired
    public BookService(CatalogRepositoryPort bookRepo) {
        this.bookRepo = bookRepo;
    }

    public Page<Book> search(String q, PageRequest request) {
        return bookRepo.search(q, request);
    }

    public Book findById(long id) {
        return bookRepo.findById(id);
    }

    public Book add(String title, String author, int pubYear) {
        return bookRepo.add(title, author, pubYear);
    }
}
