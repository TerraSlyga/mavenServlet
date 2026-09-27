package sumdu.edu.ua;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import sumdu.edu.ua.config.AppConfig;
import sumdu.edu.ua.config.AppInfoComponent;
import sumdu.edu.ua.config.CustomInfoService;
import sumdu.edu.ua.core.port.CatalogRepositoryPort;
import sumdu.edu.ua.core.port.CommentRepositoryPort;
import sumdu.edu.ua.core.service.BookService;
import sumdu.edu.ua.core.service.CommentService;
import sumdu.edu.ua.persistence.jdbc.DatabaseInitializer;
import sumdu.edu.ua.persistence.jdbc.JdbcBookRepository;
import sumdu.edu.ua.persistence.jdbc.JdbcCommentRepository;
import sumdu.edu.ua.web.BookController;
import sumdu.edu.ua.web.CommentController;
import sumdu.edu.ua.web.RootController;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookService bookService;

    @Autowired
    private CommentService commentService;

    @Autowired
    private CustomInfoService customInfoService;

    @Autowired
    private AppInfoComponent appInfoComponent;

    @Test
    @DisplayName("Перевірка завантаження контексту та наявності всіх типів бінів (@Component, @Service, @Repository, @Configuration, @Bean)")
    void contextLoadsAndAllBeansPresent() {
        assertNotNull(context, "ApplicationContext повинен бути завантажений");

        // 1. @Service біни
        assertTrue(context.containsBean("bookService"), "BookService (@Service) повинен бути в контексті");
        assertTrue(context.containsBean("commentService"), "CommentService (@Service) повинен бути в контексті");
        assertNotNull(context.getBean(BookService.class));
        assertNotNull(context.getBean(CommentService.class));

        // 2. @Repository біни
        assertTrue(context.containsBean("jdbcBookRepository"), "JdbcBookRepository (@Repository) повинен бути в контексті");
        assertTrue(context.containsBean("jdbcCommentRepository"), "JdbcCommentRepository (@Repository) повинен бути в контексті");
        assertNotNull(context.getBean(CatalogRepositoryPort.class));
        assertNotNull(context.getBean(CommentRepositoryPort.class));

        // 3. @Component біни
        assertTrue(context.containsBean("databaseInitializer"), "DatabaseInitializer (@Component) повинен бути в контексті");
        assertTrue(context.containsBean("appInfoComponent"), "AppInfoComponent (@Component) повинен бути в контексті");
        assertNotNull(context.getBean(DatabaseInitializer.class));
        assertNotNull(context.getBean(AppInfoComponent.class));

        // 4. @Configuration клас
        assertTrue(context.containsBean("appConfig"), "AppConfig (@Configuration) повинен бути в контексті");
        assertNotNull(context.getBean(AppConfig.class));

        // 5. Кастомний @Bean
        assertTrue(context.containsBean("customInfoService"), "CustomInfoService (кастомний @Bean) повинен бути в контексті");
        assertNotNull(context.getBean(CustomInfoService.class));

        // 6. Контролери
        assertNotNull(context.getBean(RootController.class));
        assertNotNull(context.getBean(BookController.class));
        assertNotNull(context.getBean(CommentController.class));
    }

    @Test
    @DisplayName("Перевірка ін'єкції параметрів конфігурації з application.properties")
    void verifyConfigurationPropertiesLoaded() {
        assertEquals("Book Catalog Application", customInfoService.getAppName());
        assertEquals("2.0.0", customInfoService.getVersion());
        assertEquals("Welcome to Spring Boot Book Catalog!", customInfoService.getWelcomeMessage());
        assertEquals(100, appInfoComponent.getMaxPageSize());
        assertNotNull(appInfoComponent.getDescription());
    }

    @Test
    @DisplayName("Перевірка взаємодії бінів: додавання книги через BookService та отримання через CatalogRepositoryPort")
    void verifyBeanInteractionBetweenServiceAndRepository() {
        var book = bookService.add("Domain Driven Design", "Eric Evans", 2003);
        assertNotNull(book);
        assertTrue(book.getId() > 0);

        var found = bookService.findById(book.getId());
        assertNotNull(found);
        assertEquals("Domain Driven Design", found.getTitle());
        assertEquals("Eric Evans", found.getAuthor());

        // Перевірка CommentService
        commentService.addComment(book.getId(), "Reader", "Excellent architecture book!");
        var comments = commentService.listComments(book.getId(), null, null, new sumdu.edu.ua.core.domain.PageRequest(0, 10));
        assertFalse(comments.getItems().isEmpty());
    }

    @Test
    @DisplayName("REST API: GET / повертає інформацію про застосунок")
    void testRootEndpoint() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.app").value("Book Catalog Application"))
                .andExpect(jsonPath("$.version").value("2.0.0"))
                .andExpect(jsonPath("$.endpoints.books").value("/api/books"));
    }

    @Test
    @DisplayName("REST API: повний життєвий цикл книги та коментарів")
    void testBookAndCommentsRestEndpoints() throws Exception {
        // 1. Створення нової книги (POST /api/books -> 201)
        String newBookJson = """
                {
                    "title": "Spring Boot in Action",
                    "author": "Craig Walls",
                    "pubYear": 2016
                }
                """;

        String createBookResponse = mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(newBookJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Spring Boot in Action"))
                .andExpect(jsonPath("$.author").value("Craig Walls"))
                .andReturn().getResponse().getContentAsString();

        // Отримуємо ID створеної книги
        Number bookIdNum = com.jayway.jsonpath.JsonPath.read(createBookResponse, "$.id");
        long bookId = bookIdNum.longValue();

        // 2. Отримання книги за ID (GET /api/books/{id} -> 200)
        mockMvc.perform(get("/api/books/" + bookId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(bookId))
                .andExpect(jsonPath("$.title").value("Spring Boot in Action"));

        // 3. Список книг з пагінацією та пошуком (GET /api/books -> 200)
        mockMvc.perform(get("/api/books")
                        .param("q", "Spring")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());

        // 4. Додавання коментаря до книги (POST /api/books/{bookId}/comments -> 201)
        String commentJson = """
                {
                    "author": "Taras",
                    "text": "Must read for Spring developers!"
                }
                """;

        mockMvc.perform(post("/api/books/" + bookId + "/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(commentJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value(bookId))
                .andExpect(jsonPath("$.author").value("Taras"))
                .andExpect(jsonPath("$.text").value("Must read for Spring developers!"));

        // 5. Отримання списку коментарів (GET /api/books/{bookId}/comments -> 200)
        mockMvc.perform(get("/api/books/" + bookId + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].author").value("Taras"));

        // 6. Валідація некоректних даних (POST /api/books з порожніми полями -> 400)
        String invalidBookJson = """
                {
                    "title": "",
                    "author": "",
                    "pubYear": -1
                }
                """;
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBookJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"));

        // 7. Запит неіснуючої книги (GET /api/books/999999 -> 404)
        mockMvc.perform(get("/api/books/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
