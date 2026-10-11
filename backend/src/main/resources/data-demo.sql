INSERT INTO user (id, nickname, balance, email) VALUES (1, 'Demo Reader', 0, 'demo@example.com');
INSERT INTO user (id, nickname, balance, email) VALUES (2, 'Demo Admin', 0, 'admin@example.com');
INSERT INTO user_auth (id, username, password, identity, user_id, enable) VALUES (1, 'demo', 'demo123', 0, 1, true);
INSERT INTO user_auth (id, username, password, identity, user_id, enable) VALUES (2, 'admin', 'admin123', 1, 2, true);
INSERT INTO book (id, title, author, price, description, inventory, sales, recommended)
VALUES (1, 'Java 21 Virtual Threads', 'Bookstore Demo', 59, 'JWT and virtual thread demonstration', 100, 0, false);
-- Explicit fixture IDs must not collide with subsequently registered users/books.
ALTER TABLE user ALTER COLUMN id RESTART WITH 3;
ALTER TABLE user_auth ALTER COLUMN id RESTART WITH 3;
ALTER TABLE book ALTER COLUMN id RESTART WITH 2;
