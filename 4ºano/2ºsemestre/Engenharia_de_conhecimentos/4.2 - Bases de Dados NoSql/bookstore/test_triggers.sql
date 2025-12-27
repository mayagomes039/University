-- Ficheiro de teste para triggers

-- TRIGGER 1
-- Testar com um email inválido
INSERT INTO customer (customer_id, email) VALUES (1, 'invalidemail.com'); -- Deve falhar

SELECT MAX(customer_id) AS max_id FROM customer;
-- Testar com um email válido
INSERT INTO customer (customer_id, email) VALUES (2001, 'valid@email.com'); -- Deve passar



-- TRIGGER 2 - não está a dar

-- Permitir NULL temporariamente
ALTER TABLE order_history MODIFY history_id NULL;
SELECT MAX(order_id) AS max_id FROM cust_order;

-- Testar trigger de order_history
INSERT INTO cust_order (order_id, customer_id) VALUES (7551, 2);
SELECT * FROM order_history WHERE order_id = 7551;

-- Repor o estado da tabela
DELETE FROM order_history WHERE order_id = 7551 AND history_id IS NULL;
ALTER TABLE order_history MODIFY history_id NOT NULL;



-- TRIGGER 3
SELECT MAX(book_id) AS max_book_id FROM book;

-- Testar trigger de prevenção de deleção
INSERT INTO book (book_id, title) VALUES (11128, 'Sample Book');

SELECT MAX(line_id) FROM order_line;
CREATE SEQUENCE order_line_seq START WITH 15351 INCREMENT BY 1;
INSERT INTO order_line (line_id, book_id, order_id) VALUES (order_line_seq.NEXTVAL, 11128, 7550);

DELETE FROM book WHERE book_id = 11128; -- Deve falhar
