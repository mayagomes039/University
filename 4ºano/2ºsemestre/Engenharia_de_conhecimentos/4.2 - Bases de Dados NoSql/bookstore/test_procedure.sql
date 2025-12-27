BEGIN
    update_order_status(1234, 3);
END;
/

SELECT * FROM order_history WHERE order_id = 1234 ORDER BY status_date DESC;
