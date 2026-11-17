INSERT INTO users (username, password, role) VALUES
    ('john_doe', 'password123', 'ROLE_USER'),
    ('jane_smith', 'secret456', 'ROLE_ADMIN'),
    ('taras_shevchenko', 'pass789', 'ROLE_USER');

INSERT INTO books (title, author, "year") VALUES
    ('Kobzar', 'Taras Shevchenko', 1840),
    ('Eneida', 'Ivan Kotliarevsky', 1798),
    ('Clean Code', 'Robert C. Martin', 2008),
    ('Design Patterns', 'Erich Gamma et al.', 1994);

INSERT INTO comments (book_id, user_id, author, text, created_at) VALUES
    (1, 1, 'john_doe', 'Неймовірна збірка поезій, основа української культури!', CURRENT_TIMESTAMP),
    (1, 2, 'jane_smith', 'Глибокі патріотичні вірші.', CURRENT_TIMESTAMP),
    (2, 1, 'john_doe', 'Перший твір нової української літератури.', CURRENT_TIMESTAMP),
    (3, 2, 'jane_smith', 'Must read for every software developer.', CURRENT_TIMESTAMP),
    (3, 3, 'taras_shevchenko', 'Чудові поради щодо рефакторингу коду.', CURRENT_TIMESTAMP);
