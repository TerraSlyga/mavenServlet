package sumdu.edu.ua.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC конфігурація з активацією анотаційного режиму MVC (@EnableWebMvc).
 * Відповідає за реєстрацію компонентів та налаштування контролерів.
 */
@Configuration
@EnableWebMvc
@ComponentScan(basePackages = "sumdu.edu.ua")
public class WebConfig implements WebMvcConfigurer {
}
