SELECT * FROM book_with_authors;
SELECT COUNT(*) FROM book_with_authors;


SELECT * FROM orders_with_status;
SELECT * FROM orders_with_status ORDER BY order_id FETCH FIRST 5 ROWS ONLY;
SELECT COUNT(*) FROM orders_with_status;
