package sumdu.edu.ua.web;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import sumdu.edu.ua.config.Beans;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;

import java.io.IOException;
import java.util.List;

public class BooksServlet extends HttpServlet {

    private final CatalogRepositoryPort bookRepo = Beans.getBookRepo();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        try {
            PageRequest pageRequest = new PageRequest(0, 20);

            List<Book> books = bookRepo.search(null, pageRequest).getItems();
            req.setAttribute("books", books);

            req.getRequestDispatcher("/WEB-INF/views/books.jsp").forward(req, resp);

        } catch (Exception e) {
            throw new ServletException("Cannot load books", e);
        }
    }
}