package sumdu.edu.ua;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Головний клас Spring Boot застосунку.
 * Підтримує як автономний запуск (через main), так і розгортання у WAR-контейнері
 * з автоматичною реєстрацією DispatcherServlet.
 */
@SpringBootApplication
public class BookApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(BookApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(BookApplication.class, args);
    }
}
