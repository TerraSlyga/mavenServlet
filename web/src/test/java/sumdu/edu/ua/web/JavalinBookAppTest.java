package sumdu.edu.ua.web;

import io.javalin.Javalin;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import sumdu.edu.ua.config.Beans;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;

class JavalinBookAppTest {

    private static Javalin app;
    private static int port;
    private static HttpClient client;

    @BeforeAll
    static void setUp() {
        Beans.init();
        app = JavalinBookApp.createApp();
        app.start(0);
        port = app.port();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void tearDown() {
        if (app != null) {
            app.stop();
        }
    }

    private String getBaseUrl() {
        return "http://localhost:" + port;
    }

    @Test
    void testRootEndpoint() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Javalin Book App"));
    }

    @Test
    void testGetBooksList() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/api/books?page=0&size=5"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("items"));
    }

    @Test
    void testCreateBookAndGetById() throws Exception {
        String bookJson = """
                {
                    "title": "Clean Architecture",
                    "author": "Robert C. Martin",
                    "pubYear": 2017
                }
                """;

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/api/books"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(bookJson))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(201, postResponse.statusCode());
        assertTrue(postResponse.body().contains("Clean Architecture"));

        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/api/books/1"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());
        assertTrue(getResponse.body().contains("id"));
    }

    @Test
    void testCreateBookValidationError() throws Exception {
        String invalidJson = """
                {
                    "title": "",
                    "author": "Someone",
                    "pubYear": 2020
                }
                """;

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/api/books"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(invalidJson))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());

        assertEquals(400, postResponse.statusCode());
        assertTrue(postResponse.body().contains("Field 'title' is required"));
    }

    @Test
    void testCommentsEndpoints() throws Exception {
        // Create comment for book 1
        String commentJson = """
                {
                    "author": "Alice",
                    "text": "Great book!"
                }
                """;

        HttpRequest postRequest = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/api/books/1/comments"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(commentJson))
                .build();

        HttpResponse<String> postResponse = client.send(postRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, postResponse.statusCode());
        assertTrue(postResponse.body().contains("Comment added successfully"));

        // Get comments for book 1
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/api/books/1/comments?page=0&size=10"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, getResponse.statusCode());
        assertTrue(getResponse.body().contains("Alice"));

        // Delete comment
        HttpRequest deleteRequest = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/api/books/1/comments/1"))
                .DELETE()
                .build();

        HttpResponse<String> deleteResponse = client.send(deleteRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, deleteResponse.statusCode());
        assertTrue(deleteResponse.body().contains("Comment deleted successfully"));
    }

    @Test
    void testBookNotFound() throws Exception {
        HttpRequest getRequest = HttpRequest.newBuilder()
                .uri(URI.create(getBaseUrl() + "/api/books/999999"))
                .GET()
                .build();

        HttpResponse<String> getResponse = client.send(getRequest, HttpResponse.BodyHandlers.ofString());
        assertEquals(404, getResponse.statusCode());
        assertTrue(getResponse.body().contains("not found"));
    }
}
