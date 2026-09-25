package sumdu.edu.ua.config;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

public class AppInit implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        Beans.init();
        System.out.println("\nApp started at: http://localhost:8080/books\n");
    }
}
