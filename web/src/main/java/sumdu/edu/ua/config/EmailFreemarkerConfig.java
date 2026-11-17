package sumdu.edu.ua.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class EmailFreemarkerConfig {

    @Bean
    public freemarker.template.Configuration freemarkerEmailConfig() {
        freemarker.template.Configuration cfg =
                new freemarker.template.Configuration(
                        freemarker.template.Configuration.VERSION_2_3_32);

        cfg.setClassLoaderForTemplateLoading(
                getClass().getClassLoader(),
                "/mail-templates/"
        );
        cfg.setDefaultEncoding("UTF-8");
        return cfg;
    }
}
