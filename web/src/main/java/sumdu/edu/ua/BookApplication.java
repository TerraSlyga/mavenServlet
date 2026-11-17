package sumdu.edu.ua;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Головний клас Spring Boot застосунку.
 * Підтримує як автономний запуск (через main), так і розгортання у WAR-контейнері
 * з автоматичною реєстрацією DispatcherServlet.
 */
@SpringBootApplication
@EntityScan(basePackages = "sumdu.edu.ua.persistence.entity")
@EnableJpaRepositories(basePackages = "sumdu.edu.ua.persistence.repository")
public class BookApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder builder) {
        return builder.sources(BookApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(BookApplication.class, args);
    }
}
