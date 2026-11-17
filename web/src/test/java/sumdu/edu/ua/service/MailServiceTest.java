package sumdu.edu.ua.service;

import jakarta.mail.Address;
import jakarta.mail.Message;
import jakarta.mail.Session;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import sumdu.edu.ua.config.EmailFreemarkerConfig;
import sumdu.edu.ua.core.domain.Book;

import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MailServiceTest {

    private JavaMailSender mailSender;
    private EmailTemplateProcessor templateProcessor;
    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        when(mailSender.createMimeMessage()).thenAnswer(invocation -> new MimeMessage(Session.getInstance(new Properties())));

        EmailFreemarkerConfig config = new EmailFreemarkerConfig();
        templateProcessor = new EmailTemplateProcessor(config.freemarkerEmailConfig());

        mailService = new MailService(mailSender, templateProcessor);
    }

    @Test
    @DisplayName("sendNewBookEmail успішно формує лист через FreeMarker та викликає JavaMailSender")
    void testSendNewBookEmailSuccess() throws Exception {
        Book book = new Book(1L, "Eneida", "Ivan Kotliarevsky", 1798);

        mailService.sendNewBookEmail(book);

        ArgumentCaptor<MimeMessage> captor = ArgumentCaptor.forClass(MimeMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        MimeMessage sentMessage = captor.getValue();
        assertEquals("Нова книга в каталозі", sentMessage.getSubject());

        Address[] recipients = sentMessage.getRecipients(Message.RecipientType.TO);
        assertNotNull(recipients);
        assertEquals(1, recipients.length);
        assertEquals("ВАША_ПОШТА@gmail.com", recipients[0].toString());

        Address[] from = sentMessage.getFrom();
        assertNotNull(from);
        assertEquals(1, from.length);
        assertEquals("bookapp@erlkonig.sumdu.edu.ua", from[0].toString());
    }

    @Test
    @DisplayName("sendNewBookEmail кидає RuntimeException при помилці відправки")
    void testSendNewBookEmailThrowsOnFailure() {
        Book book = new Book(2L, "Clean Code", "Robert Martin", 2008);
        doThrow(new MailSendException("SMTP error")).when(mailSender).send(any(MimeMessage.class));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> mailService.sendNewBookEmail(book));
        assertTrue(exception.getMessage().contains("Email sending failed"));
    }
}
