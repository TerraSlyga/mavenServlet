package org.example.web;

import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.db.CommentDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

@MultipartConfig
public class CommentsServlet extends HttpServlet {
    private final CommentDao dao = new CommentDao();
    private static final Logger log = LoggerFactory.getLogger(CommentsServlet.class);
    private final com.fasterxml.jackson.databind.ObjectMapper om =
            new com.fasterxml.jackson.databind.ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        try {
            var comments = dao.list();
            om.writeValue(resp.getWriter(), comments);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DB error");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");

        String author = req.getParameter("author");
        String text = req.getParameter("text");

        if (author == null || author.isBlank() || author.trim().length() > 64) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid author");
            return;
        }

        if (text == null || text.isBlank() || text.trim().length() > 1000) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid text");
            return;
        }

        try {
            dao.add(author.trim(), text.trim());
            log.info("Новий коментар успішно додано. Автор: {}, довжина тексту: {}", author.trim(), text.trim().length());
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (Exception e) {
            resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DB error");
        }
    }
}