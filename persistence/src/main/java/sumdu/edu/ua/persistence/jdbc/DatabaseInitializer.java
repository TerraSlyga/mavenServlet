package sumdu.edu.ua.persistence.jdbc;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class DatabaseInitializer {

    private static final Logger log = LoggerFactory.getLogger(DatabaseInitializer.class);

    @PostConstruct
    public void init() {
        log.info("Initializing database schema via DbInit...");
        DbInit.init();
        log.info("Database schema initialized successfully.");
    }
}
