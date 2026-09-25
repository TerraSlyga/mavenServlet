package sumdu.edu.ua.config;

import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.core.port.CommentRepositoryPort;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.persistence.jdbc.DbInit;
import sumdu.edu.ua.persistence.jdbc.JdbcBookRepository;
import sumdu.edu.ua.persistence.jdbc.JdbcCommentRepository;

public class Beans {

    public static void init() {
        DbInit.init(); // schema.sql

        var repo = new JdbcCommentRepository();
        CommentService commentService = new CommentService(repo);
    }

    private static final CatalogRepositoryPort bookRepo = new JdbcBookRepository();
    private static final CommentRepositoryPort commentRepo = new JdbcCommentRepository();

    public static CatalogRepositoryPort getBookRepo() {
        return bookRepo;
    }

    public static CommentRepositoryPort getCommentRepo() {
        return commentRepo;
    }
}