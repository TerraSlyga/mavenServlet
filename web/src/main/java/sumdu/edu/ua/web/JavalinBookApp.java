package sumdu.edu.ua.web;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import sumdu.edu.ua.BookApplication;

/**
 * Запуск застосунку на базі Spring Boot Starter.
 * Піднімається через main() з анотацією @SpringBootApplication.
 */
@SpringBootApplication(scanBasePackages = "sumdu.edu.ua")
public class JavalinBookApp {

    public static void main(String[] args) {
        SpringApplication.run(BookApplication.class, args);
    }
}
