package sumdu.edu.ua.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import sumdu.edu.ua.config.CustomInfoService;

import java.util.Map;

/**
 * REST контролер для кореневого ендпоінта ("/").
 */
@RestController
public class RootController {

    @Autowired
    private CustomInfoService customInfoService;

    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> root() {
        return ResponseEntity.ok(Map.of(
                "app", customInfoService.getAppName(),
                "version", customInfoService.getVersion(),
                "message", customInfoService.getWelcomeMessage(),
                "endpoints", Map.of(
                        "books", "/api/books",
                        "bookDetails", "/api/books/{id}",
                        "bookComments", "/api/books/{bookId}/comments",
                        "deleteComment", "/api/books/{bookId}/comments/{commentId}"
                )
        ));
    }
}
