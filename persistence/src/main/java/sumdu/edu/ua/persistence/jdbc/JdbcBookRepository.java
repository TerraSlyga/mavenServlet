package sumdu.edu.ua.persistence.jdbc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.core.domain.Page;
import sumdu.edu.ua.core.domain.PageRequest;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;

import java.sql.*;
import java.util.ArrayList;


public class JdbcBookRepository implements CatalogRepositoryPort {

    private static final Logger log = LoggerFactory.getLogger(JdbcBookRepository.class);

    @Override
    public Page<Book> search(String q, PageRequest request) {
        var items = new ArrayList<Book>();
        long total = 0;

        String sql = "select id, title, author, pub_year from books where 1=1";
        if (q != null && !q.isBlank()) {
            sql += " and (lower(title) like ? or lower(author) like ?)";
        }
        sql += " order by id desc limit ? offset ?";

        try (var c = Db.get();
             var ps = c.prepareStatement(sql)) {

            int i = 1;
            if (q != null && !q.isBlank()) {
                String pattern = "%" + q.toLowerCase() + "%";
                ps.setString(i++, pattern);
                ps.setString(i++, pattern);
            }
            ps.setInt(i++, request.getSize());
            ps.setInt(i, request.getPage() * request.getSize());

            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(new Book(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getInt("pub_year")
                    ));
                }
            }

            // total count
            try (var countPs = c.prepareStatement("select count(*) from books")) {
                try (var rs = countPs.executeQuery()) {
                    if (rs.next()) {
                        total = rs.getLong(1);
                    }
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("DB query error", e);
        }

        return new Page<>(items, request, total);
    }

    @Override
    public Book findById(long id) {
        try (var c = Db.get();
             var ps = c.prepareStatement(
                     "select id, title, author, pub_year from books where id = ?")) {
            ps.setLong(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new Book(
                            rs.getLong("id"),
                            rs.getString("title"),
                            rs.getString("author"),
                            rs.getInt("pub_year")
                    );
                }
            }
            return null;
        } catch (SQLException e) {
            throw new RuntimeException("DB query error", e);
        }
    }

    @Override
    public Book add(String title, String author, int pubYear) {
        try (Connection c = Db.get();
             PreparedStatement ps = c.prepareStatement(
                     "INSERT INTO books(title, author, pub_year) VALUES (?,?,?)",
                     Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setInt(3, pubYear);
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    long id = keys.getLong(1);
                    return new Book(id, title, author, pubYear);
                }
            }
            throw new RuntimeException("Insert succeeded but no ID generated");
        } catch (SQLException e) {
            throw new RuntimeException("DB insert book failed", e);
        }
    }
}