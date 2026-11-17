package sumdu.edu.ua.service;

import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.MimeMessageHelper;
import sumdu.edu.ua.config.EmailFreemarkerConfig;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class EmailTemplateProcessorTest {

    @Test
    void testProcessNewBookEmailRareEdition() {
        EmailFreemarkerConfig config = new EmailFreemarkerConfig();
        EmailTemplateProcessor processor = new EmailTemplateProcessor(config.freemarkerEmailConfig());

        Map<String, Object> model = new HashMap<>();
        model.put("title", "Kobzar");
        model.put("author", "Taras Shevchenko");
        model.put("year", 1840);
        model.put("added", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        String html = processor.process("new_book.ftl", model);
        assertNotNull(html);
        assertTrue(html.contains("Kobzar"));
        assertTrue(html.contains("Taras Shevchenko"));
        assertTrue(html.contains("1840"));
        assertTrue(html.contains("Раритетне видання!"));
        assertTrue(html.contains("BookApp &copy; 2025"));
        assertTrue(html.contains("https://raw.githubusercontent.com/VladPiatachenko/MPF_Labs/lab7-freemaker/web/src/main/resources/static/img/logo.png"));
    }

    @Test
    void testProcessNewBookEmailModernEdition() {
        EmailFreemarkerConfig config = new EmailFreemarkerConfig();
        EmailTemplateProcessor processor = new EmailTemplateProcessor(config.freemarkerEmailConfig());

        Map<String, Object> model = new HashMap<>();
        model.put("title", "Clean Code");
        model.put("author", "Robert Martin");
        model.put("year", 2008);
        model.put("added", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")));

        String html = processor.process("new_book.ftl", model);
        assertNotNull(html);
        assertTrue(html.contains("Clean Code"));
        assertFalse(html.contains("Раритетне видання!"));
    }

    @Test
    void testInternetAddressValidation() {
        MimeMessage message = new MimeMessage(Session.getInstance(new Properties()));
        assertDoesNotThrow(() -> {
            MimeMessageHelper helper = new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED);
            helper.setTo("ВАША_ПОШТА@gmail.com");
        });
    }
}
