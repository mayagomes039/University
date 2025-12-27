from neo4j import GraphDatabase
import oracledb


connection = oracledb.connect(user="bookstore", password="bookstore", dsn="localhost/orclcdb")

driver = GraphDatabase.driver("bolt://localhost:7687")

cursor = connection.cursor()

driver.execute_query("""
MATCH (b:Book)-[:WRITTEN_BY]->(a:Author)
RETURN b.book_id AS book_id, b.title AS title, a.author_name AS author_name
ORDER BY b.book_id, a.author_name 
""")

driver.execute_query("""
MATCH (o:Order)<-[:FOR_ORDER]-(oh:OrderHistory)
WITH o, oh 
ORDER BY oh.status_date DESC 
WITH o, collect(oh)[0] AS latestHistory 
RETURN
    o.order_id AS order_id,
    o.order_date AS order_date,
    latestHistory.status_value AS status_value
ORDER BY o.order_id           
""")

