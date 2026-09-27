package sumdu.edu.ua.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public CustomInfoService customInfoService(
            @Value("${spring.application.name:Book Catalog Application}") String appName,
            @Value("${app.version:1.0.0}") String appVersion,
            @Value("${app.welcome-message:Welcome to Book Catalog}") String welcomeMessage) {
        return new CustomInfoService(appName, appVersion, welcomeMessage);
    }
}
