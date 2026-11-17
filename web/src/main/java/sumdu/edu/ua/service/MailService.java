package sumdu.edu.ua.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import sumdu.edu.ua.core.domain.Book;
import sumdu.edu.ua.persistence.entity.BookEntity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final JavaMailSender mailSender;
    private final EmailTemplateProcessor templateProcessor;

    public MailService(JavaMailSender mailSender,
                       EmailTemplateProcessor templateProcessor) {
        this.mailSender = mailSender;
        this.templateProcessor = templateProcessor;
    }

    public void sendNewBookEmail(BookEntity book) {
        if (book == null) return;
        sendNewBookEmail(new Book(book.getId() != null ? book.getId() : 0L, book.getTitle(), book.getAuthor(), book.getYear()));
    }

    public void sendNewBookEmail(Book book) {
        log.info("Підготовка e-mail сповіщення для книги: '{}' (автор: {}, рік: {})",
                book.getTitle(), book.getAuthor(), book.getYear());

        if (book.getYear() < 2000) {
            log.info("Книга '{}' визначена як раритетне видання (рік: {} < 2000)", book.getTitle(), book.getYear());
        }

        Map<String, Object> model = new HashMap<>();
        model.put("title", book.getTitle());
        model.put("author", book.getAuthor());
        model.put("year", book.getYear());
        model.put("added", LocalDateTime.now().format(DATE_FORMATTER));

        String html = templateProcessor.process("new_book.ftl", model);

        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED);

            String recipient = "ВАША_ПОШТА@gmail.com";
            helper.setTo(recipient);
            helper.setSubject("Нова книга в каталозі");
            helper.setText(html, true);
            helper.setFrom("bookapp@erlkonig.sumdu.edu.ua");

            log.info("Відправлення HTML-листа через SMTP на адресу: {}", recipient);
            mailSender.send(message);
            log.info("E-mail успішно надіслано на адресу: {}", recipient);

        } catch (Exception e) {
            log.error("Помилка надсилання e-mail через SMTP: {}", e.getMessage(), e);
            throw new RuntimeException("Email sending failed", e);
        }
    }
}
