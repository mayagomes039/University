from neo4j import GraphDatabase

driver = GraphDatabase.driver("bolt://localhost:7687")

indexes = [
    "CREATE INDEX idx_book_title IF NOT EXISTS FOR (b:Book) ON b.title",
    "CREATE INDEX idx_customer_email IF NOT EXISTS FOR (c:Customer) ON c.email",
    "CREATE INDEX idx_order_date IF NOT EXISTS FOR (o:Order) ON o.order_date"
]

for index in indexes:
    driver.execute_query(index, database="bookstore")





