package sumdu.edu.ua.web.dto;

/**
 * DTO для запиту створення коментаря.
 */
public record CreateCommentRequest(
        Long bookId,
        String author,
        String text
) {}
