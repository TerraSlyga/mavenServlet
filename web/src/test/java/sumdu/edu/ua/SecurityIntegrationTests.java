package sumdu.edu.ua;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.UserService;
import sumdu.edu.ua.persistence.entity.UserEntity;
import sumdu.edu.ua.persistence.repository.UserRepository;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BookService bookService;

    @MockBean
    private JavaMailSender mailSender;

    @BeforeEach
    void setUpMailSender() {
        when(mailSender.createMimeMessage()).thenReturn(new JavaMailSenderImpl().createMimeMessage());
    }

    // 1. Гості не мають доступу до /books та API
    @Test
    @WithAnonymousUser
    @DisplayName("Гість: GET /books перенаправляє на /login")
    void guestCannotAccessBooksCatalog() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Гість: GET /api/books повертає 401 Unauthorized")
    void guestCannotAccessApiBooksGet() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("Гість: POST /api/books повертає 401 Unauthorized")
    void guestCannotAccessApiBooksPost() throws Exception {
        String bookJson = """
                {"title": "Unauthorized Book", "author": "Hacker", "pubYear": 2024}
                """;
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson))
                .andExpect(status().isUnauthorized());
    }

    // 2. Публічні маршрути доступні всім
    @Test
    @WithAnonymousUser
    @DisplayName("Публічні маршрути: /, /login, /register, /403, /404 доступні без авторизації")
    void publicEndpointsAccessibleToGuest() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register"));

        mockMvc.perform(get("/403"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/403"));

        mockMvc.perform(get("/404"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"));
    }

    // 3. Роль USER: дозволено лише читання /books та коментарів
    @Test
    @WithMockUser(username = "john_doe", roles = {"USER"})
    @DisplayName("USER: успішний доступ до перегляду /books та коментарів")
    void userCanViewBooksAndComments() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books"));

        mockMvc.perform(get("/books/1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/users/1/comments"))
                .andExpect(status().isOk())
                .andExpect(view().name("user-comments"));
    }

    @Test
    @WithMockUser(username = "john_doe", roles = {"USER"})
    @DisplayName("USER: заборона додавання книги через веб-форму (перенаправлення на /403)")
    void userForbiddenToAddBookWeb() throws Exception {
        mockMvc.perform(post("/books/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Forbidden Book")
                        .param("author", "Some Author")
                        .param("pubYear", "2024"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/403"));
    }

    @Test
    @WithMockUser(username = "john_doe", roles = {"USER"})
    @DisplayName("USER: заборона звернення до /api/books (403 Forbidden)")
    void userForbiddenToAccessApiBooks() throws Exception {
        mockMvc.perform(get("/api/books"))
                .andExpect(status().isForbidden());

        String bookJson = """
                {"title": "User Book", "author": "User Author", "pubYear": 2024}
                """;
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson))
                .andExpect(status().isForbidden());
    }

    // 4. Роль ADMIN: повний доступ
    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("ADMIN: повний доступ до додавання книги через веб-форму")
    void adminCanAddBookWeb() throws Exception {
        mockMvc.perform(get("/books/add"))
                .andExpect(status().isOk())
                .andExpect(view().name("book-form"));

        mockMvc.perform(post("/books/add")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Admin Book Web")
                        .param("author", "Admin Author")
                        .param("pubYear", "2024"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("ADMIN: повний доступ до /api/books (створення, отримання, видалення)")
    void adminCanManageApiBooks() throws Exception {
        String bookJson = """
                {"title": "Admin REST Book", "author": "Admin Author", "pubYear": 2024}
                """;
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bookJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber());

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }

    // 5. Форма логіну та логаут
    @Test
    @DisplayName("Логін: успішна аутентифікація перенаправляє на /books")
    void testSuccessfulLogin() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
    }

    @Test
    @DisplayName("Логін: невдала аутентифікація перенаправляє на /login?error=true")
    void testFailedLogin() throws Exception {
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", "admin")
                        .param("password", "wrong_password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("Логаут: успішний вихід перенаправляє на /login?logout")
    void testLogout() throws Exception {
        mockMvc.perform(post("/logout")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    // 6. Реєстрація користувача та Email-підтвердження
    @Test
    @DisplayName("Реєстрація та email-підтвердження: повний життєвий цикл акаунту")
    void testRegistrationAndConfirmationWorkflow() throws Exception {
        String testUser = "reg_test_" + System.currentTimeMillis();

        // 1. Відправка форми реєстрації
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", testUser)
                        .param("password", "Password123!")
                        .param("email", "reg_test@example.com"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered=true"));

        // 2. Перевірка користувача в базі: створено, заблоковано (enabled=false), має код підтвердження
        UserEntity created = userRepository.findByUsername(testUser).orElse(null);
        assertNotNull(created);
        assertEquals("ROLE_USER", created.getRole());
        assertFalse(created.isEnabled(), "Користувач має бути неактивним до підтвердження e-mail");
        assertNotNull(created.getConfirmationCode(), "Код підтвердження має бути згенерований");
        assertTrue(created.getPassword().startsWith("$2a$"), "Пароль має бути захешований BCrypt");

        // 3. Спроба входу непідтвердженого користувача -> помилка
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", testUser)
                        .param("password", "Password123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));

        // 4. Підтвердження акаунту за кодом
        mockMvc.perform(get("/confirm")
                        .param("code", created.getConfirmationCode()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?confirmed=true"));

        // 5. Перевірка статусу користувача після підтвердження: enabled=true, confirmationCode=null
        UserEntity confirmed = userRepository.findByUsername(testUser).orElse(null);
        assertNotNull(confirmed);
        assertTrue(confirmed.isEnabled(), "Користувач має бути активним після підтвердження");
        assertNull(confirmed.getConfirmationCode(), "Код підтвердження має бути очищений");

        // 6. Успішний вхід підтвердженого користувача
        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("username", testUser)
                        .param("password", "Password123!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
    }

    // 7. @PreAuthorize на сервісному рівні
    @Test
    @WithMockUser(username = "user", roles = {"USER"})
    @DisplayName("@PreAuthorize: користувач з роллю USER не може викликати bookService.add")
    void testServicePreAuthorizeUserForbidden() {
        assertThrows(AccessDeniedException.class, () ->
                bookService.add("Forbidden Title", "Author", 2024));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("@PreAuthorize: користувач з роллю ADMIN успішно викликає bookService.add")
    void testServicePreAuthorizeAdminAllowed() {
        var book = bookService.add("Admin Method Security Book", "Sec Author", 2024);
        assertNotNull(book);
        assertEquals("Admin Method Security Book", book.getTitle());
    }

    // 8. Відображення UI елементів за ролями
    @Test
    @WithMockUser(username = "taras_shevchenko", roles = {"USER"})
    @DisplayName("UI: для USER прихована кнопка додавання книги та відображається ім'я користувача")
    void testUiRoleRestrictionsForUser() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("taras_shevchenko")))
                .andExpect(content().string(containsString("Вийти")))
                .andExpect(content().string(not(containsString("/books/add"))))
                .andExpect(content().string(not(containsString("Видалити"))));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("UI: для ADMIN відображаються кнопки додавання та видалення книги")
    void testUiRoleRestrictionsForAdmin() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("admin")))
                .andExpect(content().string(containsString("/books/add")))
                .andExpect(content().string(containsString("Видалити")));
    }
}
