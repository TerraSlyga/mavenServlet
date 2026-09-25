package sumdu.edu.ua.core.port;

import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;

public interface CatalogRepositoryPort {
    Page<Book> search(String query, PageRequest request);
    Book findById(long id);
    Book add(String title, String author, int pubYear);
}
