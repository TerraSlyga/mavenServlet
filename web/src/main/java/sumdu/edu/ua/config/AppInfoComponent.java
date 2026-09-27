package sumdu.edu.ua.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


@Component
public class AppInfoComponent {

    @Value("${app.description:REST API for Book Catalog}")
    private String description;

    @Value("${app.max-page-size:100}")
    private int maxPageSize;

    public String getDescription() {
        return description;
    }

    public int getMaxPageSize() {
        return maxPageSize;
    }
}
