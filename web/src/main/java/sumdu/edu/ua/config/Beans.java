package sumdu.edu.ua.config;

import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.core.port.CommentRepositoryPort;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.persistence.jdbc.DbInit;
import sumdu.edu.ua.persistence.jdbc.JdbcBookRepository;
import sumdu.edu.ua.persistence.jdbc.JdbcCommentRepository;

public class Beans {

    private static final CatalogRepositoryPort bookRepo = new JdbcBookRepository();
    private static final CommentRepositoryPort commentRepo = new JdbcCommentRepository();
    private static final CommentService commentService = new CommentService(commentRepo);

    public static void init() {
        DbInit.init(); // schema.sql
    }

    public static CatalogRepositoryPort getBookRepo() {
        return bookRepo;
    }

    public static CommentRepositoryPort getCommentRepo() {
        return commentRepo;
    }

    public static CommentService getCommentService() {
        return commentService;
    }
}
