package sumdu.edu.ua.persistence.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Db {
    private Db() {}

    // Файлова H2 у папці ./data
    private static final String URL  = "jdbc:h2:file:./data/guest;AUTO_SERVER=TRUE";
    private static final String USER = "sa";
    private static final String PASS = "";

    static {
        try { Class.forName("org.h2.Driver"); } catch (ClassNotFoundException e) { throw new RuntimeException(e); }
    }

    public static Connection get() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }
}
