-- ==================================================
-- GRAVITY BOOKSTORE - QUERIES COM MEDIÇÃO DE TEMPO
-- ==================================================

SET SERVEROUTPUT ON;

-- QUERY 1: Listar todos os livros com seus autores e editoras

DECLARE
    v_start NUMBER;
    v_end   NUMBER;
    v_elapsed_ms NUMBER;
BEGIN
    v_start := DBMS_UTILITY.GET_TIME;

    FOR rec IN (
        SELECT 
            b.title AS "Título do Livro",
            LISTAGG(a.author_name, ', ') WITHIN GROUP (ORDER BY a.author_name) AS "Autores",
            p.publisher_name AS "Editora",
            bl.language_name AS "Idioma",
            b.num_pages AS "Páginas",
            b.publication_date AS "Data de Publicação"
        FROM book b
        JOIN book_author ba ON b.book_id = ba.book_id
        JOIN author a ON ba.author_id = a.author_id
        JOIN publisher p ON b.publisher_id = p.publisher_id
        JOIN book_language bl ON b.language_id = bl.language_id
        GROUP BY b.book_id, b.title, p.publisher_name, bl.language_name, b.num_pages, b.publication_date
        ORDER BY b.title
    ) LOOP
        NULL; 
    END LOOP;

    v_end := DBMS_UTILITY.GET_TIME;
    v_elapsed_ms := (v_end - v_start) * 10;

    DBMS_OUTPUT.PUT_LINE('Tempo decorrido para a QUERY 1: ' || v_elapsed_ms || ' ms');
END;
/

-- QUERY 2: Top 10 clientes com mais encomendas

DECLARE
    v_start NUMBER;
    v_end   NUMBER;
    v_elapsed_ms NUMBER;
BEGIN
    v_start := DBMS_UTILITY.GET_TIME;

    FOR rec IN (
        SELECT 
            c.first_name || ' ' || c.last_name AS "Nome do Cliente",
            c.email AS "Email",
            COUNT(co.order_id) AS "Número de Encomendas",
            SUM(ol.price) AS "Valor Total Gasto"
        FROM customer c
        JOIN cust_order co ON c.customer_id = co.customer_id
        JOIN order_line ol ON co.order_id = ol.order_id
        GROUP BY c.customer_id, c.first_name, c.last_name, c.email
        ORDER BY COUNT(co.order_id) DESC, SUM(ol.price) DESC
        FETCH FIRST 10 ROWS ONLY

    ) LOOP
        NULL; 
    END LOOP;

    v_end := DBMS_UTILITY.GET_TIME;
    v_elapsed_ms := (v_end - v_start) * 10;

    DBMS_OUTPUT.PUT_LINE('Tempo decorrido para a QUERY 2: ' || v_elapsed_ms || ' ms');
END;
/

-- QUERY 3: Livros mais vendidos por quantidade

DECLARE
    v_start NUMBER;
    v_end   NUMBER;
    v_elapsed_ms NUMBER;
BEGIN
    v_start := DBMS_UTILITY.GET_TIME;

    FOR rec IN (
        SELECT 
            b.title AS "Título do Livro",
            LISTAGG(a.author_name, ', ') WITHIN GROUP (ORDER BY a.author_name) AS "Autores",
            COUNT(*) AS "Quantidade Total Vendida",
            AVG(ol.price) AS "Preço Médio",
            COUNT(DISTINCT co.customer_id) AS "Clientes Únicos"
        FROM book b
        JOIN book_author ba ON b.book_id = ba.book_id
        JOIN author a ON ba.author_id = a.author_id
        JOIN order_line ol ON b.book_id = ol.book_id
        JOIN cust_order co ON ol.order_id = co.order_id
        GROUP BY b.book_id, b.title
        ORDER BY COUNT(*) DESC
        FETCH FIRST 15 ROWS ONLY
    ) LOOP
        NULL; 
    END LOOP;

    v_end := DBMS_UTILITY.GET_TIME;
    v_elapsed_ms := (v_end - v_start) * 10;

    DBMS_OUTPUT.PUT_LINE('Tempo decorrido para a QUERY 3: ' || v_elapsed_ms || ' ms');
END;
/

-- QUERY 4: Análise de encomendas por país

DECLARE
    v_start NUMBER;
    v_end   NUMBER;
    v_elapsed_ms NUMBER;
BEGIN
    v_start := DBMS_UTILITY.GET_TIME;

    FOR rec IN (
        SELECT 
            co.country_name AS "País",
            COUNT(DISTINCT c.customer_id) AS "Clientes Únicos",
            COUNT(cust_o.order_id) AS "Total de Encomendas",
            SUM(ol.price) AS "Receita Total",
            AVG(ol.price) AS "Valor Médio por Encomenda"
        FROM country co
        JOIN address a ON co.country_id = a.country_id
        JOIN customer_address ca ON a.address_id = ca.address_id
        JOIN customer c ON ca.customer_id = c.customer_id
        JOIN cust_order cust_o ON c.customer_id = cust_o.customer_id
        JOIN order_line ol ON cust_o.order_id = ol.order_id
        GROUP BY co.country_id, co.country_name
        ORDER BY SUM(ol.price) DESC
    ) LOOP
        NULL;
    END LOOP;

    v_end := DBMS_UTILITY.GET_TIME;
    v_elapsed_ms := (v_end - v_start) * 10;

    DBMS_OUTPUT.PUT_LINE('Tempo decorrido para a QUERY 4: ' || v_elapsed_ms || ' ms');
END;
/

-- QUERY 5: Estado atual das encomendas

DECLARE
    v_start NUMBER;
    v_end   NUMBER;
    v_elapsed_ms NUMBER;
BEGIN
    v_start := DBMS_UTILITY.GET_TIME;

    FOR rec IN (
        SELECT 
            os.status_value AS "Estado da Encomenda",
            COUNT(*) AS "Número de Encomendas",
            ROUND(COUNT(*) * 100.0 / SUM(COUNT(*)) OVER(), 2) AS "Percentagem"
        FROM cust_order co
        JOIN order_history oh ON co.order_id = oh.order_id
        JOIN order_status os ON oh.status_id = os.status_id
        WHERE oh.status_date = (
            SELECT MAX(oh2.status_date)
            FROM order_history oh2
            WHERE oh2.order_id = oh.order_id
        )
        GROUP BY os.status_id, os.status_value
        ORDER BY COUNT(*) DESC
    ) LOOP
        NULL;
    END LOOP;

    v_end := DBMS_UTILITY.GET_TIME;
    v_elapsed_ms := (v_end - v_start) * 10;

    DBMS_OUTPUT.PUT_LINE('Tempo decorrido para a QUERY 5: ' || v_elapsed_ms || ' ms');
END;
/

-- QUERY 6: Autores mais produtivos

DECLARE
    v_start NUMBER;
    v_end   NUMBER;
    v_elapsed_ms NUMBER;
BEGIN
    v_start := DBMS_UTILITY.GET_TIME;

    FOR rec IN (
        SELECT 
            a.author_name AS "Nome do Autor",
            COUNT(DISTINCT b.book_id) AS "Número de Livros",
            COUNT(DISTINCT p.publisher_id) AS "Editoras Diferentes",
            MIN(b.publication_date) AS "Primeira Publicação",
            MAX(b.publication_date) AS "Última Publicação",
            COUNT(ol.line_id) AS "Total de Livros Vendidos"
        FROM author a
        JOIN book_author ba ON a.author_id = ba.author_id
        JOIN book b ON ba.book_id = b.book_id
        JOIN publisher p ON b.publisher_id = p.publisher_id
        LEFT JOIN order_line ol ON b.book_id = ol.book_id
        GROUP BY a.author_id, a.author_name
        ORDER BY COUNT(DISTINCT b.book_id) DESC
    ) LOOP
        NULL; 
    END LOOP;

    v_end := DBMS_UTILITY.GET_TIME;
    v_elapsed_ms := (v_end - v_start) * 10;

    DBMS_OUTPUT.PUT_LINE('Tempo decorrido para a QUERY 6: ' || v_elapsed_ms || ' ms');
END;
/

-- QUERY 7: Análise temporal de vendas (por mês)

DECLARE
    v_start NUMBER;
    v_end   NUMBER;
    v_elapsed_ms NUMBER;
BEGIN
    v_start := DBMS_UTILITY.GET_TIME;

    FOR rec IN (
        SELECT 
            EXTRACT(YEAR FROM co.order_date) AS "Ano",
            EXTRACT(MONTH FROM co.order_date) AS "Mês",
            TO_CHAR(co.order_date, 'Month') AS "Nome do Mês",
            COUNT(DISTINCT co.order_id) AS "Encomendas",
            COUNT(ol.line_id) AS "Livros Vendidos",
            SUM(ol.price) AS "Receita Total"
        FROM cust_order co
        JOIN order_line ol ON co.order_id = ol.order_id
        GROUP BY EXTRACT(YEAR FROM co.order_date), EXTRACT(MONTH FROM co.order_date), TO_CHAR(co.order_date, 'Month')
        ORDER BY EXTRACT(YEAR FROM co.order_date) DESC, EXTRACT(MONTH FROM co.order_date) DESC
    ) LOOP
        NULL;
    END LOOP;

    v_end := DBMS_UTILITY.GET_TIME;
    v_elapsed_ms := (v_end - v_start) * 10;

    DBMS_OUTPUT.PUT_LINE('Tempo decorrido para a QUERY 7: ' || v_elapsed_ms || ' ms');
END;
/

-- QUERY 8: Clientes com endereços múltiplos

DECLARE
    v_start NUMBER;
    v_end   NUMBER;
    v_elapsed_ms NUMBER;
BEGIN
    v_start := DBMS_UTILITY.GET_TIME;

    FOR rec IN (
        SELECT 
            c.first_name || ' ' || c.last_name AS "Nome do Cliente",
            c.email AS "Email",
            COUNT(ca.address_id) AS "Número de Endereços",
            LISTAGG(
                a.street_number || ' ' || a.street_name || ', ' || co.country_name || ' (' || ast.address_status || ')',
                '; '
            ) WITHIN GROUP (ORDER BY a.address_id) AS "Todos os Endereços"
        FROM customer c
        JOIN customer_address ca ON c.customer_id = ca.customer_id
        JOIN address a ON ca.address_id = a.address_id
        JOIN country co ON a.country_id = co.country_id
        JOIN address_status ast ON ca.status_id = ast.status_id
        GROUP BY c.customer_id, c.first_name, c.last_name, c.email
        HAVING COUNT(ca.address_id) > 1
        ORDER BY COUNT(ca.address_id) DESC
    ) LOOP
        NULL;
    END LOOP;

    v_end := DBMS_UTILITY.GET_TIME;
    v_elapsed_ms := (v_end - v_start) * 10;

    DBMS_OUTPUT.PUT_LINE('Tempo decorrido para a QUERY 8: ' || v_elapsed_ms || ' ms');
END;
/

-- QUERY 9: Livros por idioma e editora

DECLARE
    v_start NUMBER;
    v_end   NUMBER;
    v_elapsed_ms NUMBER;
BEGIN
    v_start := DBMS_UTILITY.GET_TIME;

    FOR rec IN (
        SELECT 
            bl.language_name AS "Idioma",
            p.publisher_name AS "Editora",
            COUNT(b.book_id) AS "Número de Livros",
            AVG(b.num_pages) AS "Média de Páginas",
            MIN(b.publication_date) AS "Livro Mais Antigo",
            MAX(b.publication_date) AS "Livro Mais Recente"
        FROM book b
        JOIN book_language bl ON b.language_id = bl.language_id
        JOIN publisher p ON b.publisher_id = p.publisher_id
        GROUP BY bl.language_id, bl.language_name, p.publisher_id, p.publisher_name
        ORDER BY bl.language_name, COUNT(b.book_id) DESC
    ) LOOP
        NULL;
    END LOOP;

    v_end := DBMS_UTILITY.GET_TIME;
    v_elapsed_ms := (v_end - v_start) * 10;

    DBMS_OUTPUT.PUT_LINE('Tempo decorrido para a QUERY 9: ' || v_elapsed_ms || ' ms');
END;
/

-- QUERY 10: Métodos de envio mais utilizados

DECLARE
    v_start NUMBER;
    v_end   NUMBER;
    v_elapsed_ms NUMBER;
BEGIN
    v_start := DBMS_UTILITY.GET_TIME;

    FOR rec IN (
        SELECT 
            sm.method_name AS "Método de Envio",
            COUNT(co.order_id) AS "Número de Utilizações",
            ROUND(COUNT(co.order_id) * 100.0 / SUM(COUNT(co.order_id)) OVER(), 2) AS "Percentagem de Uso",
            AVG(ol.price) AS "Valor Médio das Encomendas",
            SUM(ol.price) AS "Receita Total"
        FROM shipping_method sm
        JOIN cust_order co ON sm.method_id = co.shipping_method_id
        JOIN order_line ol ON co.order_id = ol.order_id
        GROUP BY sm.method_id, sm.method_name
        ORDER BY COUNT(co.order_id) DESC
    ) LOOP
        NULL;  
    END LOOP;

    v_end := DBMS_UTILITY.GET_TIME;
    v_elapsed_ms := (v_end - v_start) * 10;

    DBMS_OUTPUT.PUT_LINE('Tempo decorrido para a QUERY 10: ' || v_elapsed_ms || ' ms');
END;
/

-- QUERY 11: Livros nunca vendidos

DECLARE
    v_start NUMBER;
    v_end   NUMBER;
    v_elapsed_ms NUMBER;
BEGIN
    v_start := DBMS_UTILITY.GET_TIME;

    FOR rec IN (
        SELECT 
            b.title AS "Título do Livro",
            LISTAGG(a.author_name, ', ') WITHIN GROUP (ORDER BY a.author_name) AS "Autores",
            p.publisher_name AS "Editora",
            bl.language_name AS "Idioma",
            b.publication_date AS "Data de Publicação",
            TRUNC(SYSDATE - b.publication_date) AS "Dias Desde Publicação"
        FROM book b
        JOIN book_author ba ON b.book_id = ba.book_id
        JOIN author a ON ba.author_id = a.author_id
        JOIN publisher p ON b.publisher_id = p.publisher_id
        JOIN book_language bl ON b.language_id = bl.language_id
        LEFT JOIN order_line ol ON b.book_id = ol.book_id
        WHERE ol.book_id IS NULL
        GROUP BY b.book_id, b.title, p.publisher_name, bl.language_name, b.publication_date
        ORDER BY b.publication_date DESC
    ) LOOP
        NULL; 
    END LOOP;

    v_end := DBMS_UTILITY.GET_TIME;
    v_elapsed_ms := (v_end - v_start) * 10;

    DBMS_OUTPUT.PUT_LINE('Tempo decorrido para a QUERY 12: ' || v_elapsed_ms || ' ms');
END;
/

-- QUERY 12: Análise de clientes por fidelidade

DECLARE
    v_start NUMBER;
    v_end   NUMBER;
    v_elapsed_ms NUMBER;
BEGIN
    v_start := DBMS_UTILITY.GET_TIME;

    FOR rec IN (
        WITH customer_stats AS (
            SELECT 
                c.customer_id,
                c.first_name || ' ' || c.last_name AS customer_name,
                c.email,
                COUNT(co.order_id) AS total_orders,
                SUM(ol.price) AS total_spent,
                MIN(co.order_date) AS first_order,
                MAX(co.order_date) AS last_order,
                TRUNC(MAX(co.order_date) - MIN(co.order_date)) AS customer_lifespan_days
            FROM customer c
            JOIN cust_order co ON c.customer_id = co.customer_id
            JOIN order_line ol ON co.order_id = ol.order_id
            GROUP BY c.customer_id, c.first_name, c.last_name, c.email
        )
        SELECT 
            customer_name AS "Nome do Cliente",
            email AS "Email",
            total_orders AS "Total de Encomendas",
            total_spent AS "Total Gasto",
            CASE 
                WHEN total_orders >= 10 AND total_spent >= 500 THEN 'VIP'
                WHEN total_orders >= 5 OR total_spent >= 200 THEN 'Fiel'
                WHEN total_orders >= 2 THEN 'Regular'
                ELSE 'Novo'
            END AS "Categoria de Cliente",
            first_order AS "Primeira Encomenda",
            last_order AS "Última Encomenda",
            customer_lifespan_days AS "Dias Como Cliente"
        FROM customer_stats
        ORDER BY total_spent DESC
    ) LOOP
        NULL; 
    END LOOP;

    v_end := DBMS_UTILITY.GET_TIME;
    v_elapsed_ms := (v_end - v_start) * 10;

    DBMS_OUTPUT.PUT_LINE('Tempo decorrido para a QUERY 13: ' || v_elapsed_ms || ' ms');
END;
/

-- QUERY 13: Relatório de performance por editora
SET SERVEROUTPUT ON;
DECLARE
    v_start NUMBER;
    v_end   NUMBER;
    v_elapsed_ms NUMBER;
BEGIN
    v_start := DBMS_UTILITY.GET_TIME;

    FOR rec IN (
        SELECT 
            p.publisher_name AS "Editora",
            COUNT(DISTINCT b.book_id) AS "Livros no Catálogo",
            COUNT(DISTINCT ba.author_id) AS "Autores Únicos",
            COUNT(ol.line_id) AS "Total de Linhas Vendidas",
            COALESCE(SUM(ol.price), 0) AS "Receita Total",
            CASE 
                WHEN COUNT(DISTINCT b.book_id) > 0 
                THEN ROUND(COUNT(ol.line_id) / COUNT(DISTINCT b.book_id), 2)
                ELSE 0
            END AS "Média de Vendas por Livro",
            COUNT(DISTINCT bl.language_id) AS "Idiomas Publicados"
        FROM publisher p
        JOIN book b ON p.publisher_id = b.publisher_id
        JOIN book_author ba ON b.book_id = ba.book_id
        JOIN book_language bl ON b.language_id = bl.language_id
        LEFT JOIN order_line ol ON b.book_id = ol.book_id
        GROUP BY p.publisher_id, p.publisher_name
        ORDER BY COALESCE(SUM(ol.price), 0) DESC
    ) LOOP
        NULL; 
    END LOOP;

    v_end := DBMS_UTILITY.GET_TIME;
    v_elapsed_ms := (v_end - v_start) * 10;

    DBMS_OUTPUT.PUT_LINE('Tempo decorrido para a QUERY 14: ' || v_elapsed_ms || ' ms');
END;
/

